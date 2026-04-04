package com.manoj.trip.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class AuthRequest {
    @Schema(description = "Unique user name of the user", example = "Brokie", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty
    private String username;
    @Schema(description = "Password of the user", example = "UYzWx1&hyI", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty
    private String password;
}
