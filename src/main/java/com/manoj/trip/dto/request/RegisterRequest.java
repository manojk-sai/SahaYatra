package com.manoj.trip.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class RegisterRequest {
    @Schema(description = "Unique user name of the user", example = "Brokie", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty
    private String username;
    @Schema(description = "Password of the user", example = "UYzWx1&hyI", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty
    private String password;
    @Schema(description = "Full name of the user", example = "Brookie Samson", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String fullName;
    @Schema(description = "City of the user", example = "Cincinnati", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String city;
    @Schema(description = "Zipcode of the user", example = "45220", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty
    private String zipCode;
}
