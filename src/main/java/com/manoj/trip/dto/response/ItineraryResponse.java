package com.manoj.trip.dto.response;

import com.manoj.trip.enums.StopCategory;
import com.manoj.trip.enums.StopStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class ItineraryResponse {

    private String     tripId;
    private String     tripTitle;
    private LocalDate startDate;
    private LocalDate  endDate;

    /** Ordered list of days. Days without a date are grouped under an "Unscheduled" entry. */
    private List<DayPlan> days;

    /** Grand total of all stop estimatedCosts. */
    private BigDecimal totalEstimatedCost;

    /** Breakdown by category: { "FOOD": 120.00, "ACCOMMODATION": 450.00, ... } */
    private Map<StopCategory, BigDecimal> costByCategory;

    /** Total number of confirmed stops across all days. */
    private int confirmedStopCount;

    // ── Nested ─────────────────────────────────────────

    @Data
    @Builder
    public static class DayPlan {

        /**
         * The visit date for this group, or null for stops with no visitDate.
         * Null group is always last in the list.
         */
        private LocalDate       date;

        /** Human-readable label: "Day 1 — Mon 2 Jun" or "Unscheduled" */
        private String          label;

        private List<StopItem>  stops;

        private BigDecimal      dailyCost;
    }

    @Data
    @Builder
    public static class StopItem {
        private String       stopId;
        private String       name;
        private String       location;
        private StopCategory category;
        private StopStatus status;
        private int          visitOrder;
        private Integer      durationHours;
        private BigDecimal   estimatedCost;
        private String       notes;
        private boolean      mustVisit;

        /** Embedded weather snapshot — null if not yet fetched by backend. */
        private WeatherInfo  weather;

        private int approveCount;
        private int rejectCount;
    }

    @Data
    @Builder
    public static class WeatherInfo {
        private Double temperature;
        private String condition;
        private Integer humidity;
    }
}