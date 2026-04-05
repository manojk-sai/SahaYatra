package com.manoj.trip.model;

import com.manoj.trip.enums.StopCategory;
import com.manoj.trip.enums.StopStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Stop {
    private String id;                  // UUID generated in service layer
    private String name;
    private String location;            // City / address
    private StopCategory category;      // ACCOMMODATION, FOOD, ACTIVITY, TRANSPORT, OTHER
    private StopStatus status;          // PROPOSED | CONFIRMED | REJECTED
    private int visitOrder;             // Position in the route (0-based)
    private LocalDate visitDate;
    private Integer durationHours;
    private BigDecimal estimatedCost;
    private String notes;
    private boolean mustVisit;
}
