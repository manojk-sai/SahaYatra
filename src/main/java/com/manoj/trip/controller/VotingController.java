package com.manoj.trip.controller;

import com.manoj.trip.dto.request.CastVoteRequest;
import com.manoj.trip.dto.request.UpdateStopStatusRequest;
import com.manoj.trip.dto.response.VoteTallyResponse;
import com.manoj.trip.service.VotingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/trips/{tripId}/stops/{stopId}")
@RequiredArgsConstructor
public class VotingController {
    private final VotingService votingService;

    /**
     * Cast or replace a vote on a stop.
     *
     * POST /trips/{tripId}/stops/{stopId}/vote
     * Body: { "type": "APPROVE" | "REJECT" | "ABSTAIN" }
     *
     * Rules enforced in service:
     *  - Caller must be an active trip member
     *  - mustVisit stops cannot be voted on
     *  - Already-resolved stops reject new votes
     *  - One vote per user — re-voting replaces the previous vote
     *
     * Returns the full tally after the vote is recorded.
     */
    @PostMapping("/vote")
    public ResponseEntity<VoteTallyResponse> castVote(
            @PathVariable String tripId,
            @PathVariable String stopId,
            @Valid @RequestBody CastVoteRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {

        VoteTallyResponse tally = votingService.castVote(
                tripId, stopId, userDetails.getUsername(), req);

        return ResponseEntity.ok(tally);
    }

    /**
     * Get the current vote tally for a stop.
     *
     * GET /trips/{tripId}/stops/{stopId}/votes
     *
     * Returns:
     *  - approve / reject / abstain counts
     *  - total votes cast, pending votes, member count
     *  - whether the calling user has voted and what they voted
     *  - full list of individual vote entries
     */
    @GetMapping("/votes")
    public ResponseEntity<VoteTallyResponse> getVoteTally(
            @PathVariable String tripId,
            @PathVariable String stopId,
            @AuthenticationPrincipal UserDetails userDetails) {

        VoteTallyResponse tally = votingService.getTally(
                tripId, stopId, userDetails.getUsername());

        return ResponseEntity.ok(tally);
    }

    /**
     * Organizer manually overrides a stop's status.
     *
     * PATCH /trips/{tripId}/stops/{stopId}/status
     * Body: { "status": "CONFIRMED" | "REJECTED" }
     *
     * Intended for votingMode = ORGANIZER (advisory votes).
     * Can also override an auto-resolved status.
     * Only the ORGANIZER role can call this endpoint.
     */
    @PatchMapping("/status")
    public ResponseEntity<VoteTallyResponse> overrideStatus(
            @PathVariable String tripId,
            @PathVariable String stopId,
            @Valid @RequestBody UpdateStopStatusRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {

        VoteTallyResponse tally = votingService.overrideStopStatus(
                tripId, stopId, userDetails.getUsername(), req);

        return ResponseEntity.ok(tally);
    }
}
