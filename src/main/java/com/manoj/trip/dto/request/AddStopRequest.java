package com.manoj.trip.dto.request;

import com.manoj.trip.enums.StopCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AddStopRequest {
    @NotBlank(message = "Stop name is required")
    private String name;
    @NotNull(message = "City is required")
    private String location;
    @NotNull(message = "Category is required")
    private StopCategory category;
    private LocalDate visitDate;
    private Integer durationHours;
    private BigDecimal estimatedCost;
    private String notes;
    private boolean mustVisit = false;
}
