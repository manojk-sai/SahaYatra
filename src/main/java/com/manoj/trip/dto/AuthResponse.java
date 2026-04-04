package com.manoj.trip.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.Instant;

@Data
public class AuthResponse {
    @Schema(description = "JWT identifier", example = "eyJzdWIiOiJ0b3VyIiwiaWF0IjoxNzc1MzMwODIwLCJleHAiOjE3NzUzMzQ0MjB9.80uA5oKN9XIOMJfvaLNWLj_uq4sgm5WdrwC6_LnHUSs", accessMode = Schema.AccessMode.READ_ONLY)
    private String token;
    @Schema(description = "Expiration Date & Time", example = "2026-04-05T06:06:13.968229900Z", accessMode = Schema.AccessMode.READ_ONLY)
    private Instant expiresBy;
}
