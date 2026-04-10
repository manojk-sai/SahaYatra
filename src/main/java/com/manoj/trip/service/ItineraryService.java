package com.manoj.trip.service;

import com.manoj.trip.dto.response.ItineraryResponse;
import com.manoj.trip.enums.StopCategory;
import com.manoj.trip.enums.StopStatus;
import com.manoj.trip.enums.VoteType;
import com.manoj.trip.model.Stop;
import com.manoj.trip.model.Trip;
import com.manoj.trip.model.Vote;
import com.manoj.trip.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItineraryService {

    private static final DateTimeFormatter DAY_FMT =
            DateTimeFormatter.ofPattern("EEE d MMM");

    private final TripRepository tripRepository;

    // ── Public API ─────────────────────────────────────────────────────────

    /**
     * Build a full day-by-day itinerary for a trip.
     * Any active member can request this.
     *
     * @param tripId     the trip to build the itinerary for
     * @param requesterId must be an active trip member
     */
    public ItineraryResponse buildItinerary(String tripId, String requesterId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found: " + tripId));

        boolean isMember = trip.getMembers().stream()
                .anyMatch(m -> m.getUserId().equals(requesterId) && m.isActive());
        if (!isMember) throw new RuntimeException("Access denied: not a trip member");

        List<Stop> allStops = trip.getStops() == null ? List.of() : trip.getStops();

        // ── Group stops by visitDate ──────────────────────────────────────
        // Stops with null visitDate go into a special "Unscheduled" bucket (null key)
        Map<LocalDate, List<Stop>> byDate = allStops.stream()
                .sorted(Comparator.comparingInt(Stop::getVisitOrder))
                .collect(Collectors.groupingBy(
                        s -> s.getVisitDate() != null ? s.getVisitDate() : LocalDate.MAX,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        // Sort buckets: dated ascending, then the unscheduled (LocalDate.MAX) bucket last
        List<Map.Entry<LocalDate, List<Stop>>> sortedEntries = new ArrayList<>(byDate.entrySet());
        sortedEntries.sort(Map.Entry.comparingByKey());

        // ── Build DayPlan list ────────────────────────────────────────────
        int dayNumber = 1;
        List<ItineraryResponse.DayPlan> days = new ArrayList<>();

        for (Map.Entry<LocalDate, List<Stop>> entry : sortedEntries) {
            LocalDate date     = entry.getKey().equals(LocalDate.MAX) ? null : entry.getKey();
            List<Stop> stops   = entry.getValue();

            BigDecimal dailyCost = stops.stream()
                    .map(s -> s.getEstimatedCost() != null ? s.getEstimatedCost() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            String label = date == null
                    ? "Unscheduled"
                    : "Day " + dayNumber + " — " + date.format(DAY_FMT);

            List<ItineraryResponse.StopItem> stopItems = stops.stream()
                    .map(this::toStopItem)
                    .collect(Collectors.toList());

            days.add(ItineraryResponse.DayPlan.builder()
                    .date(date)
                    .label(label)
                    .stops(stopItems)
                    .dailyCost(dailyCost)
                    .build());

            if (date != null) dayNumber++;
        }

        // ── Grand total & category breakdown ─────────────────────────────
        BigDecimal totalCost = allStops.stream()
                .map(s -> s.getEstimatedCost() != null ? s.getEstimatedCost() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<StopCategory, BigDecimal> costByCategory = allStops.stream()
                .filter(s -> s.getEstimatedCost() != null && s.getCategory() != null)
                .collect(Collectors.groupingBy(
                        Stop::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, Stop::getEstimatedCost, BigDecimal::add)
                ));

        long confirmedCount = allStops.stream()
                .filter(s -> s.getStatus() == StopStatus.CONFIRMED || s.isMustVisit())
                .count();

        return ItineraryResponse.builder()
                .tripId(trip.getId())
                .tripTitle(trip.getTitle())
                .startDate(trip.getStartDate())
                .endDate(trip.getEndDate())
                .days(days)
                .totalEstimatedCost(totalCost)
                .costByCategory(costByCategory)
                .confirmedStopCount((int) confirmedCount)
                .build();
    }

    // ── Mapper ─────────────────────────────────────────────────────────────

    private ItineraryResponse.StopItem toStopItem(Stop s) {
        List<Vote> votes =
                s.getVotes() == null ? Collections.emptyList() : s.getVotes();

        int approveCount = (int) votes.stream().filter(v -> v.getVoteType() == VoteType.APPROVE).count();
        int rejectCount  = (int) votes.stream().filter(v -> v.getVoteType() == VoteType.REJECT).count();

        return ItineraryResponse.StopItem.builder()
                .stopId(s.getId())
                .name(s.getName())
                .location(s.getLocation())
                .category(s.getCategory())
                .status(s.getStatus())
                .visitOrder(s.getVisitOrder())
                .durationHours(s.getDurationHours())
                .estimatedCost(s.getEstimatedCost())
                .notes(s.getNotes())
                .mustVisit(s.isMustVisit())
                .approveCount(approveCount)
                .rejectCount(rejectCount)
                .build();
    }
}