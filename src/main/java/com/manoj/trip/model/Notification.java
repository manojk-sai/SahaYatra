package com.manoj.trip.model;

import com.manoj.trip.enums.NotificationType;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Notification {
    String id;
    String tripId;
    String tripTitle;
    String message;
    NotificationType type;
    @Builder.Default
    Boolean isRead = false;
    LocalDateTime createdAt;
}
