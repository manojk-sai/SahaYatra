package com.manoj.trip.model;

import com.manoj.trip.enums.MemberRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TripMembership {
    private String userId;          // Reference to users collection
    private MemberRole role;        // ORGANIZER | CONTRIBUTOR | VIEWER
    private LocalDateTime joinedAt;
    private String inviteToken;     // UUID token used during invite flow
    private boolean active;         // false = left the trip (soft-delete)
}
