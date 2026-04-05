package com.manoj.trip.dto.response;

import com.manoj.trip.enums.*;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class TripResponse {
    private String id;
    private String title;
    private String description;
    private TripStatus status;
    private TripVisibility visibility;
    private boolean locked;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal budgetCap;
    private List<StopResponse> stops;
    private List<MemberResponse> members;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    public static class StopResponse {
        private String id;
        private String name;
        private String location;
        private StopCategory category;
        private StopStatus status;
        private int visitOrder;
        private LocalDate visitDate;
        private Integer durationHours;
        private BigDecimal estimatedCost;
        private String notes;
        private boolean mustVisit;
    }

    @Data
    @Builder
    public static class MemberResponse {
        private String userId;
        private String displayName;
        private String email;
        private MemberRole role;
        private LocalDateTime joinedAt;
        private boolean active;
    }
}
