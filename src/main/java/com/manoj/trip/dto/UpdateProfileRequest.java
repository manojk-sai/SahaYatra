package com.manoj.trip.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    @Schema(description = "Full name of the user", example = "Brookie Samson", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String fullName;

    @Schema(description = "City of the user", example = "Cincinnati", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String city;

    @Schema(description = "Zipcode of the user", example = "45220", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String zipCode;
}