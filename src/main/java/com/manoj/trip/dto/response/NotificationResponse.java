package com.manoj.trip.dto.response;

import com.manoj.trip.enums.NotificationType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class NotificationResponse {

    private String           id;
    private NotificationType type;
    private String           message;
    private String           tripId;
    private String           tripTitle;
    private boolean          read;
    private LocalDateTime createdAt;

    // ── Wrapper for the list endpoint ──────────────────

    @Data
    @Builder
    public static class NotificationListResponse {
        private List<NotificationResponse> notifications;
        private int  total;
        private int  unreadCount;
    }
}
