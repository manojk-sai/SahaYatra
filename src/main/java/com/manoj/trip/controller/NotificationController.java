package com.manoj.trip.controller;

import com.manoj.trip.dto.response.NotificationResponse;
import com.manoj.trip.model.Notification;
import com.manoj.trip.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Phase 5 notification endpoints.
 *
 * All routes require a valid JWT.
 * The userId is the authenticated principal username (MongoDB _id stored in JWT).
 */
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * GET /notifications
     * Returns all notifications for the current user, newest first.
     * Optionally filter to unread: GET /notifications?unread=true
     */
    @GetMapping
    public ResponseEntity<NotificationResponse.NotificationListResponse> getNotifications(
            @RequestParam(defaultValue = "false") boolean unread,
            @AuthenticationPrincipal UserDetails userDetails) {

        String userId = userDetails.getUsername();

        List<Notification> raw = unread
                ? notificationService.getUnread(userId)
                : notificationService.getNotifications(userId);

        List<NotificationResponse> mapped = raw.stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        long unreadCount = notificationService.getUnread(userId).size();

        return ResponseEntity.ok(
                NotificationResponse.NotificationListResponse.builder()
                        .notifications(mapped)
                        .total(mapped.size())
                        .unreadCount((int) unreadCount)
                        .build()
        );
    }

    /**
     * PATCH /notifications/{notificationId}/read
     * Marks a single notification as read.
     */
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> markRead(
            @PathVariable String notificationId,
            @AuthenticationPrincipal UserDetails userDetails) {

        notificationService.markRead(userDetails.getUsername(), notificationId);
        return ResponseEntity.noContent().build();
    }

    /**
     * PATCH /notifications/read-all
     * Marks every notification as read for the current user.
     */
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllRead(
            @AuthenticationPrincipal UserDetails userDetails) {

        notificationService.markAllRead(userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    // ── Mapper ─────────────────────────────────────────────────────────────

    private NotificationResponse toDto(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .type(n.getType())
                .message(n.getMessage())
                .tripId(n.getTripId())
                .tripTitle(n.getTripTitle())
                .read(n.getIsRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
