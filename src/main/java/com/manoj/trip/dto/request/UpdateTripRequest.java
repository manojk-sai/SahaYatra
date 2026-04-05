package com.manoj.trip.dto.request;

import com.manoj.trip.enums.TripVisibility;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateTripRequest {
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal budgetCap;
    private TripVisibility visibility;
}
