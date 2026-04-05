package com.manoj.trip.service;

import com.manoj.trip.aop.RequiresTripRole;
import com.manoj.trip.dto.request.AddStopRequest;
import com.manoj.trip.dto.request.AddTripRequest;
import com.manoj.trip.dto.request.UpdateTripRequest;
import com.manoj.trip.dto.response.TripResponse;
import com.manoj.trip.enums.MemberRole;
import com.manoj.trip.enums.StopStatus;
import com.manoj.trip.enums.TripStatus;
import com.manoj.trip.enums.TripVisibility;
import com.manoj.trip.model.Stop;
import com.manoj.trip.model.Trip;
import com.manoj.trip.model.TripMembership;
import com.manoj.trip.repository.TripRepository;
import com.manoj.trip.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TripService {
    private final TripRepository tripRepository;
    private final MongoTemplate mongoTemplate;
    private final UserRepository userRepository;

    public TripResponse createTrip(AddTripRequest request, String organizerId) {
        Trip trip = Trip.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .budgetCap(request.getBudgetCap())
                .visibility(request.getVisibility() != null ? request.getVisibility() : TripVisibility.INVITE_ONLY)
                .locked(false)
                .status(TripStatus.PLANNING)
                .build();

        //Add Creator automatically as Organizer
        TripMembership organizer = TripMembership.builder()
                                    .userId(organizerId)
                                    .role(MemberRole.ORGANIZER)
                                    .active(true)
                                    .joinedAt(LocalDateTime.now())
                                    .build();
        trip.getMembers().add(organizer);

        Trip savedTrip = tripRepository.save(trip);
        return toResponse(savedTrip);
    }

    public TripResponse findTripById(String tripId, String userId) {
        Trip trip = findTripOrThrow(tripId);
        assertMember(trip, userId);
        return toResponse(trip);
    }

    public  List<TripResponse> getMyTrips(String userId) {
        return tripRepository.findByMembersUserId(userId)
                .stream()
                .map(trip -> toResponse(trip)).collect(Collectors.toList());
    }

    public List<TripResponse> getPublicTrips() {
        return tripRepository.findByVisibility(TripVisibility.PUBLIC)
                .stream()
                .map(trip -> toResponse(trip)).collect(Collectors.toList());
    }

    @RequiresTripRole(MemberRole.ORGANIZER)
    public TripResponse updateTrip(String tripId, UpdateTripRequest request, String userId) {
        Trip trip = findTripOrThrow(tripId);
        assertNotLocked(trip);

        if(request.getTitle() != null) trip.setTitle(request.getTitle());
        if(request.getDescription() != null) trip.setDescription(request.getDescription());
        if(request.getStartDate() != null) trip.setStartDate(request.getStartDate());
        if(request.getEndDate() != null) trip.setEndDate(request.getEndDate());
        if(request.getBudgetCap() != null) trip.setBudgetCap(request.getBudgetCap());
        if(request.getVisibility() != null) trip.setVisibility(request.getVisibility());

        return toResponse(tripRepository.save(trip));
    }

    @RequiresTripRole(MemberRole.ORGANIZER)
    public TripResponse advanceStatus(String tripId, String userId) {
        Trip trip = findTripOrThrow(tripId);
        TripStatus next = nextStatus(trip.getStatus());
        trip.setStatus(next);
        if (next == TripStatus.CONFIRMED) trip.setLocked(true);
        return toResponse(tripRepository.save(trip));
    }

    @RequiresTripRole(MemberRole.ORGANIZER)
    public TripResponse lockTrip(String tripId, String userId) {
        Trip trip = findTripOrThrow(tripId);
        trip.setLocked(true);
        return toResponse(tripRepository.save(trip));
    }

    @RequiresTripRole(MemberRole.CONTRIBUTOR)
    public TripResponse addStop(String tripId, String userId, AddStopRequest request) {
        Trip trip = findTripOrThrow(tripId);
        assertNotLocked(trip);

        Stop stop = Stop.builder()
                .name(request.getName())
                .location(request.getLocation())
                .category(request.getCategory())
                .status(StopStatus.PROPOSED)
                .visitOrder(trip.getStops().size())
                .visitDate(request.getVisitDate())
                .durationHours(request.getDurationHours())
                .estimatedCost(request.getEstimatedCost())
                .notes(request.getNotes())
                .mustVisit(request.isMustVisit())
                .build();

        trip.getStops().add(stop);
        return toResponse(tripRepository.save(trip));
    }

    @RequiresTripRole(MemberRole.CONTRIBUTOR)
    public TripResponse removeStop(String tripId, String userId, String stopId) {
        Trip trip = findTripOrThrow(tripId);
        assertNotLocked(trip);

        boolean removed = trip.getStops().removeIf(s -> s.getId().equals(stopId));
        if (!removed) throw new RuntimeException("Stop not found: " + stopId);

        //Reorder remaining stops
        for (int i = 0; i < trip.getStops().size(); i++) {
            trip.getStops().get(i).setVisitOrder(i);
        }

        return toResponse(tripRepository.save(trip));
    }

    @RequiresTripRole(MemberRole.CONTRIBUTOR)
    public TripResponse reorderStops(String tripId, String userId, List<String> orderedStopIds) {
        Trip trip = findTripOrThrow(tripId);
        assertNotLocked(trip);

        for (int i = 0; i < orderedStopIds.size(); i++) {
            final int idx = i;
            final String stopId = orderedStopIds.get(i);
            trip.getStops().stream()
                    .filter(s -> s.getId().equals(stopId))
                    .findFirst()
                    .ifPresent(s -> s.setVisitOrder(idx));
        }

        trip.getStops().sort((a, b) -> Integer.compare(a.getVisitOrder(), b.getVisitOrder()));
        return toResponse(tripRepository.save(trip));
    }

    @RequiresTripRole(MemberRole.ORGANIZER)
    public String inviteMember(String tripId, String userId, String inviteeId, MemberRole role) {
        Trip trip = findTripOrThrow(tripId);
        //Check if invitee is present in the system
        boolean validUsername = userRepository.existsByUsername(inviteeId);
        if (!validUsername) throw new RuntimeException("Invitee user does not exist");

        //Check if invitee is already a member
        boolean alreadyMember = trip.getMembers().stream()
                .anyMatch(m -> m.getUserId().equals(inviteeId));
        if (alreadyMember) throw new RuntimeException("User is already a member of the trip");

        String token = UUID.randomUUID().toString();

        TripMembership membership = TripMembership.builder()
                .userId(inviteeId)
                .role(role != null ? role : MemberRole.CONTRIBUTOR)
                .inviteToken(token)
                .active(false) // Will become active when invitee accepts
                .joinedAt(LocalDateTime.now())
                .build();
        trip.getMembers().add(membership);
        tripRepository.save(trip);
        return token;
    }

    public TripResponse acceptInvite(String userId, String token) {
        Trip trip = tripRepository.findByPendingInviteToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid Token"));

        trip.getMembers().stream()
                .filter(m -> token.equals(m.getInviteToken()) && m.getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Token doesn't match any pending invite for this user"))
                .setActive(true); // Activate membership

        trip.getMembers().stream()
                .filter(m -> token.equals(m.getInviteToken()))
                .findFirst()
                .ifPresent(m -> {m.setInviteToken(null); m.setJoinedAt(LocalDateTime.now());}); // Clear token after acceptance

        return toResponse(tripRepository.save(trip));
    }

    public void leaveTrip(String tripId, String userId) {
        Trip trip = findTripOrThrow(tripId);

        trip.getMembers().stream()
                .filter(m -> m.getUserId().equals(userId) && m.isActive())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Not an active member"))
                .setActive(false);

        tripRepository.save(trip);
    }

    @RequiresTripRole(MemberRole.ORGANIZER)
    public TripResponse transferOwnership(String tripId, String currentOrganizerId, String newOrganizerId) {
        Trip trip = findTripOrThrow(tripId);

        trip.getMembers().stream()
                .filter(m -> m.getUserId().equals(currentOrganizerId))
                .findFirst()
                .ifPresent(m -> m.setRole(MemberRole.CONTRIBUTOR));

        trip.getMembers().stream()
                .filter(m -> m.getUserId().equals(newOrganizerId) && m.isActive())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("New organizer must be an active member"))
                .setRole(MemberRole.ORGANIZER);

        return toResponse(tripRepository.save(trip));
    }

    private TripResponse toResponse(Trip trip) {
        List<TripResponse.StopResponse> stopResponses = trip.getStops().stream()
                .sorted((a, b) -> Integer.compare(a.getVisitOrder(), b.getVisitOrder()))
                .map(s -> TripResponse.StopResponse.builder()
                        .id(s.getId())
                        .name(s.getName())
                        .location(s.getLocation())
                        .category(s.getCategory())
                        .status(s.getStatus())
                        .visitOrder(s.getVisitOrder())
                        .visitDate(s.getVisitDate())
                        .durationHours(s.getDurationHours())
                        .estimatedCost(s.getEstimatedCost())
                        .notes(s.getNotes())
                        .mustVisit(s.isMustVisit())
                        .build())
                .collect(Collectors.toList());

        List<TripResponse.MemberResponse> memberResponses = trip.getMembers().stream()
                .map(m -> TripResponse.MemberResponse.builder()
                        .userId(m.getUserId())
                        .role(m.getRole())
                        .joinedAt(m.getJoinedAt())
                        .active(m.isActive())
                        .build())
                .collect(Collectors.toList());

        return TripResponse.builder()
                .id(trip.getId())
                .title(trip.getTitle())
                .description(trip.getDescription())
                .status(trip.getStatus())
                .visibility(trip.getVisibility())
                .locked(trip.isLocked())
                .startDate(trip.getStartDate())
                .endDate(trip.getEndDate())
                .budgetCap(trip.getBudgetCap())
                .stops(stopResponses)
                .members(memberResponses)
                .build();
    }

    private Trip findTripOrThrow(String tripId) {
        return tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found: " + tripId));
    }

    private void assertMember(Trip trip, String userId) {
        boolean isMember = trip.getMembers().stream()
                .anyMatch(m -> m.getUserId().equals(userId) && m.isActive());
        if (!isMember) throw new RuntimeException("Access denied: not a trip member");
    }

    private void assertNotLocked(Trip trip) {
        if (trip.isLocked()) throw new RuntimeException("Trip is locked and cannot be modified");
    }

    private TripStatus nextStatus(TripStatus current) {
        return switch (current) {
            case PLANNING    -> TripStatus.CONFIRMED;
            case CONFIRMED   -> TripStatus.IN_PROGRESS;
            case IN_PROGRESS -> TripStatus.COMPLETED;
            case COMPLETED   -> TripStatus.ARCHIVED;
            case ARCHIVED    -> throw new RuntimeException("Trip is already archived");
        };
    }
}
