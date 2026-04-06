package com.manoj.trip.service;

import com.manoj.trip.dto.request.CastVoteRequest;
import com.manoj.trip.dto.request.UpdateStopStatusRequest;
import com.manoj.trip.dto.response.VoteTallyResponse;
import com.manoj.trip.enums.MemberRole;
import com.manoj.trip.enums.StopStatus;
import com.manoj.trip.enums.VoteType;
import com.manoj.trip.enums.VotingMode;
import com.manoj.trip.model.Stop;
import com.manoj.trip.model.Trip;
import com.manoj.trip.model.Vote;
import com.manoj.trip.repository.TripRepository;
import com.manoj.trip.service.voting.VotingStrategy;
import com.manoj.trip.service.voting.VotingStrategyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VotingService {
    private final TripRepository tripRepository;
    private final VotingStrategyFactory votingStrategyFactory;
    private final MongoTemplate mongoTemplate;
    /**
     * Casts or replaces a vote on a stop.
     *
     * Flow:
     *  1. Load the trip and locate the target stop.
     *  2. Enforce: user must be an active member.
     *  3. Enforce: mustVisit stops are not voteable.
     *  4. Enforce: already-CONFIRMED / REJECTED stops cannot receive new votes.
     *  5. Use MongoTemplate $pull to remove any existing vote by this user.
     *  6. Use MongoTemplate $push to append the new vote atomically.
     *  7. Reload the trip, run the strategy, and conditionally update the stop status.
     *
     * @return the updated VoteTallyResponse
     */
    public VoteTallyResponse castVote(String tripId, String stopId,
                                      String userId, CastVoteRequest req) {

        Trip trip = findTripOrThrow(tripId);
        assertActiveMember(trip, userId);
        Stop stop = findStopOrThrow(trip, stopId);

        if (stop.isMustVisit()) {
            throw new RuntimeException("Must-visit stops cannot be voted on");
        }
        if (stop.getStatus() == StopStatus.CONFIRMED || stop.getStatus() == StopStatus.REJECTED) {
            throw new RuntimeException("Stop is already " + stop.getStatus() + " — voting is closed");
        }

        // ── Step 5: Remove any existing vote by this user ($pull) ──────────
        Query tripQuery = Query.query(Criteria.where("_id").is(tripId));
        // ArrayFilters for nested array targeting
        Update pullWithFilter =
                new Update()
                        .filterArray(Criteria.where("s._id").is(stopId))
                        .pull("stops.$[s].votes", new org.bson.Document("userId", userId));
        mongoTemplate.updateFirst(tripQuery, pullWithFilter, Trip.class);
        // ── Step 6: Push the new vote ($push) ──────────────────────────────
        Vote newVote = Vote.builder()
                .userId(userId)
                .voteType(req.getVoteType())
                .castAt(LocalDateTime.now())
                .build();

        Update pushUpdate = new Update()
                .filterArray(Criteria.where("s._id").is(stopId))
                .push("stops.$[s].votes", newVote);
        mongoTemplate.updateFirst(tripQuery, pushUpdate, Trip.class);
        // ── Step 7: Reload → run strategy → conditionally update status ────
        Trip reloaded = findTripOrThrow(tripId);
        Stop reloadedStop = findStopOrThrow(reloaded, stopId);

        List<Vote> currentVotes = reloadedStop.getVotes() == null ? reloadedStop.getVotes() : new ArrayList<>();

        int activeMemberCount = (int) reloaded.getMembers().stream()
                .filter(m -> m.isActive())
                .count();
        VotingMode mode = reloaded.getVotingMode();
        if(mode == null){
            throw new IllegalStateException("Trip " + tripId + " has null voting mode");
        }

        VotingStrategy strategy = votingStrategyFactory.forMode(reloaded.getVotingMode());
        if(strategy == null){
            throw new IllegalStateException("No voting strategy found for mode: " + mode);
        }
        StopStatus resolved = strategy.resolve(currentVotes, activeMemberCount);

        // Only update status if the strategy resolved to CONFIRMED or REJECTED
        if (resolved == StopStatus.CONFIRMED || resolved == StopStatus.REJECTED) {
            Update statusUpdate = new Update()
                    .filterArray(Criteria.where("stop.id").is(stopId))
                    .set("stops.$[stop].status", resolved);
            mongoTemplate.updateFirst(tripQuery, statusUpdate, Trip.class);
            // Reload once more to reflect the status update in the tally
            reloadedStop.setStatus(resolved);
        }

        return buildTally(reloaded, reloadedStop, userId, activeMemberCount);
    }
    /**
     * Returns the full vote tally for a stop.
     * Any active trip member can call this.
     */
    public VoteTallyResponse getTally(String tripId, String stopId, String userId) {
        Trip trip = findTripOrThrow(tripId);
        assertActiveMember(trip, userId);

        Stop stop = findStopOrThrow(trip, stopId);

        int activeMemberCount = (int) trip.getMembers().stream()
                .filter(m -> m.isActive())
                .count();

        return buildTally(trip, stop, userId, activeMemberCount);
    }

    /**
     * Allows the organizer to manually set a stop's status.
     * Used when votingMode = ORGANIZER (advisory votes only).
     * Also usable to override any auto-resolved status.
     */
    public VoteTallyResponse overrideStopStatus(String tripId, String stopId,
                                                String userId, UpdateStopStatusRequest req) {
        Trip trip = findTripOrThrow(tripId);
        assertOrganizer(trip, userId);

        // Validate target status — organizer can only set CONFIRMED or REJECTED
        if (req.getStatus() == StopStatus.PROPOSED) {
            throw new RuntimeException("Cannot manually set a stop back to PROPOSED");
        }

        Query tripQuery  = Query.query(Criteria.where("_id").is(tripId));
        Update statusUpd = new Update()
                .filterArray(Criteria.where("stop.id").is(stopId))
                .set("stops.$[stop].status", req.getStatus());

        mongoTemplate.updateFirst(tripQuery, statusUpd, Trip.class);

        Trip reloaded = findTripOrThrow(tripId);
        Stop reloadedStop = findStopOrThrow(reloaded, stopId);
        int activeMemberCount = (int) reloaded.getMembers().stream()
                .filter(m -> m.isActive()).count();

        return buildTally(reloaded, reloadedStop, userId, activeMemberCount);
    }

    private VoteTallyResponse buildTally(Trip trip, Stop stop,
                                         String currentUserId, int activeMemberCount) {

        List<Vote> votes = stop.getVotes() == null ? Collections.emptyList() : stop.getVotes();

        long approveCount = votes.stream().filter(v -> v.getVoteType() == VoteType.APPROVE).count();
        long rejectCount  = votes.stream().filter(v -> v.getVoteType() == VoteType.REJECT).count();
        long abstainCount = votes.stream().filter(v -> v.getVoteType() == VoteType.ABSTAIN).count();

        Vote myVoteEntry = votes.stream()
                .filter(v -> v.getUserId().equals(currentUserId))
                .findFirst()
                .orElse(null);

        List<VoteTallyResponse.VoteEntry> entries = votes.stream()
                .map(v -> VoteTallyResponse.VoteEntry.builder()
                        .userId(v.getUserId())
                        .voteType(v.getVoteType())
                        .castAt(v.getCastAt())
                        .build())
                .collect(Collectors.toList());

        return VoteTallyResponse.builder()
                .stopId(stop.getId())
                .stopName(stop.getName())
                .stopStatus(stop.getStatus())
                .approveCount((int) approveCount)
                .rejectCount((int) rejectCount)
                .abstainCount((int) abstainCount)
                .totalVotes(votes.size())
                .memberCount(activeMemberCount)
                .pendingCount(activeMemberCount - votes.size())
                .hasVoted(myVoteEntry != null)
                .voteType(myVoteEntry != null ? myVoteEntry.getVoteType() : null)
                .votes(entries)
                .build();
    }

    private Trip findTripOrThrow(String tripId) {
        return tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found: " + tripId));
    }

    private Stop findStopOrThrow(Trip trip, String stopId) {
        return trip.getStops().stream()
                .filter(s -> s.getId().equals(stopId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Stop not found: " + stopId));
    }

    private void assertActiveMember(Trip trip, String userId) {
        boolean isMember = trip.getMembers().stream()
                .anyMatch(m -> m.getUserId().equals(userId) && m.isActive());
        if (!isMember) throw new RuntimeException("User is not an active trip member");
    }

    private void assertOrganizer(Trip trip, String userId) {
        boolean isOrganizer = trip.getMembers().stream()
                .anyMatch(m -> m.getUserId().equals(userId)
                        && m.isActive()
                        && m.getRole() == MemberRole.ORGANIZER);
        if (!isOrganizer) throw new RuntimeException("Only the organizer can perform this action");
    }
}
