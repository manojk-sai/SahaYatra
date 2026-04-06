package com.manoj.trip.dto.request;

import com.manoj.trip.enums.StopStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateStopStatusRequest {
        @NotNull(message = "Status is required")
        private StopStatus status; // Expected values: "CONFIRMED", "REJECTED", "PENDING"
}
