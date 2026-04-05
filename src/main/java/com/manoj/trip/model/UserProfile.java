package com.manoj.trip.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {
    @Schema(description = "Full name of the user", example = "Brookie Samson", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String fullName;
    @Schema(description = "City of the user", example = "Cincinnati", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String city;
    @Schema(description = "Zipcode of the user", example = "45220", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty
    private String zipCode;
}
