package com.manoj.trip.controller;

import com.manoj.trip.dto.response.ItineraryResponse;
import com.manoj.trip.service.ItineraryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/trips/{tripId}")
@RequiredArgsConstructor
public class ItineraryController {

    private final ItineraryService itineraryService;

    /**
     * GET /trips/{tripId}/itinerary
     *
     * Any active trip member can call this endpoint.
     * Response includes:
     *  - Stops grouped by visitDate into DayPlan objects
     *  - Daily cost subtotals
     *  - Grand total estimated cost
     *  - Cost breakdown by category (FOOD, ACCOMMODATION, etc.)
     *  - Count of confirmed/must-visit stops
     */
    @GetMapping("/itinerary")
    public ResponseEntity<ItineraryResponse> getItinerary(
            @PathVariable String tripId,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                itineraryService.buildItinerary(tripId, userDetails.getUsername()));
    }

}
