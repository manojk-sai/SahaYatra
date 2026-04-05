package com.manoj.trip.dto.request;

import com.manoj.trip.enums.TripVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AddTripRequest {
    @NotBlank(message = "Title is required")
    private String title;
    private String description;
    @NotNull(message = "Start date is required")
    private LocalDate startDate;
    @NotNull(message = "End date is required")
    private LocalDate endDate;
    private BigDecimal budgetCap;
    private TripVisibility visibility = TripVisibility.INVITE_ONLY;
}
