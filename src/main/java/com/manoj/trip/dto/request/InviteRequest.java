package com.manoj.trip.dto.request;

import com.manoj.trip.enums.MemberRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InviteRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email")
    private String email;
    private MemberRole role = MemberRole.CONTRIBUTOR;
}
