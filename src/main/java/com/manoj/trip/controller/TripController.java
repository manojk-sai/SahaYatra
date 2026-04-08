package com.manoj.trip.controller;

import com.manoj.trip.dto.request.AddStopRequest;
import com.manoj.trip.dto.request.AddTripRequest;
import com.manoj.trip.dto.request.UpdateTripRequest;
import com.manoj.trip.dto.response.TripResponse;
import com.manoj.trip.enums.MemberRole;
import com.manoj.trip.model.WeatherSnapshot;
import com.manoj.trip.service.TripService;
import com.manoj.trip.service.WeatherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/trips")
@RequiredArgsConstructor
public class TripController {
    private final TripService tripService;
    private final WeatherService weatherService;

    @PostMapping
    public ResponseEntity<TripResponse> createTrip(
            @Valid @RequestBody AddTripRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {

        String userId = userDetails.getUsername(); // userId stored as username in JWT
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tripService.createTrip(req, userId));
    }

    @GetMapping("/{tripId}")
    public ResponseEntity<TripResponse> getTrip(
            @PathVariable String tripId,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                tripService.findTripById(tripId, userDetails.getUsername()));
    }

    @GetMapping("/my")
    public ResponseEntity<List<TripResponse>> getMyTrips(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                tripService.getMyTrips(userDetails.getUsername()));
    }

    @GetMapping("/public")
    public ResponseEntity<List<TripResponse>> getPublicTrips() {
        return ResponseEntity.ok(tripService.getPublicTrips());
    }

    @PatchMapping("/{tripId}")
    public ResponseEntity<TripResponse> updateTrip(
            @PathVariable String tripId,
            @RequestBody UpdateTripRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                tripService.updateTrip(tripId, req, userDetails.getUsername()));
    }

    @PostMapping("/{tripId}/advance")
    public ResponseEntity<TripResponse> advanceTrip(
            @PathVariable String tripId,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                tripService.advanceStatus(tripId, userDetails.getUsername()));
    }

    @PostMapping("/{tripId}/lock")
    public ResponseEntity<TripResponse> lockTrip(
            @PathVariable String tripId,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                tripService.lockTrip(tripId, userDetails.getUsername()));
    }

    @PostMapping("/{tripId}/stops")
    public ResponseEntity<TripResponse> addStop(
            @PathVariable String tripId,
            @RequestBody AddStopRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.status(HttpStatus.CREATED)
                        .body(tripService.addStop(tripId, userDetails.getUsername(), request));
    }

    @DeleteMapping("/{tripId}/stops/{stopId}")
    public ResponseEntity<TripResponse> deleteStop(
            @PathVariable String tripId,
            @PathVariable String stopId,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                tripService.removeStop(tripId, stopId, userDetails.getUsername()));
    }

    @PostMapping("/{tripId}/stops/reorder")
    public ResponseEntity<TripResponse> reorderStops(
            @PathVariable String tripId,
            @RequestBody List<String> orderedStopIds,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                tripService.reorderStops(tripId, userDetails.getUsername(), orderedStopIds));
    }

    /**
     * Organizer invites a user.
     * Body: { "userId": "...", "role": "CONTRIBUTOR" }
     * Returns: { "inviteToken": "..." }  — share this token out-of-band (email etc.)
     */
    @PostMapping("/{tripId}/invite")
    public ResponseEntity<Map<String, String>> inviteMember(
            @PathVariable String tripId,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {

        String inviteeUserId = body.get("userId");
        MemberRole role = body.containsKey("role")
                ? MemberRole.valueOf(body.get("role"))
                : MemberRole.CONTRIBUTOR;

        String token = tripService.inviteMember(
                tripId, userDetails.getUsername(), inviteeUserId, role);

        return ResponseEntity.ok(Map.of("inviteToken", token));
    }

    /**
     * Invitee accepts using token.
     * POST /trips/join?token=<uuid>
     */
    @PostMapping("/join")
    public ResponseEntity<TripResponse> acceptInvite(
            @RequestParam String token,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                tripService.acceptInvite(userDetails.getUsername(), token));
    }

    @PostMapping("/{tripId}/leave")
    public ResponseEntity<Void> leaveTrip(
            @PathVariable String tripId,
            @AuthenticationPrincipal UserDetails userDetails) {

        tripService.leaveTrip(tripId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{tripId}/transfer")
    public ResponseEntity<TripResponse> transferOwnership(
            @PathVariable String tripId,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                tripService.transferOwnership(
                        tripId,
                        userDetails.getUsername(),
                        body.get("newOrganizerId")));
    }

    @GetMapping("/stops/{stopId}/weather")
    public ResponseEntity<WeatherSnapshot> getWeatherForStop(
            @PathVariable String stopId) {

        return ResponseEntity.ok(
                weatherService.getWeatherSnapshotForStop(stopId));
    }

}
