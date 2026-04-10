package com.manoj.trip.service;

import com.manoj.trip.enums.NotificationType;
import com.manoj.trip.model.Notification;
import com.manoj.trip.model.User;
import com.manoj.trip.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private static final int MAX_NOTIFICATIONS = 10;
    private final MongoTemplate mongoTemplate;
    private final UserRepository userRepository;

    /**
     * Push a notification to a single user.
     * Uses MongoTemplate $push so we never load the full User document.
     *
     * @param username  target user's MongoDB _id
     * @param type    notification type (enum)
     * @param message human-readable text shown in the UI
     * @param tripId  trip context (used for navigation link in UI)
     * @param tripTitle denormalised trip title
     */
    public void pushNotification(String message,
                                 String username,
                                 NotificationType type,
                                 String tripId,
                                 String tripTitle) {
        Notification notification = Notification.builder()
                .message(message)
                .type(type)
                .tripId(tripId)
                .tripTitle(tripTitle)
                .isRead(false)
                .id(UUID.randomUUID().toString())
                .createdAt(LocalDateTime.now())
                .build();
        Query query = Query.query(Criteria.where("username").is(username));
        Update update = new Update()
                .push("notifications")
                        .atPosition(Update.Position.FIRST)
                                .value(notification);
        mongoTemplate.updateFirst(query,update, User.class);
        // Trim to cap — slice keeps the first MAX_NOTIFICATIONS elements
        Update trim = new Update().set("notifications",
                new org.bson.Document("$slice", MAX_NOTIFICATIONS));
        // MongoDB $slice inside $push is handled inline; alternatively use
        // a separate pipeline update for trim after push:
        trimIfNeeded(username);
    }
    /**
     * Push the same notification to multiple users (bulk, one update each).
     */
    public void pushToMany(List<String> usernames,
                           NotificationType type,
                           String message,
                           String tripId,
                           String tripTitle) {
        usernames.forEach(username -> {
            try {
                pushNotification(message, username, type, tripId, tripTitle);
            } catch (Exception e) {
                throw new RuntimeException(e);            }
        });
    }

    // ── Read ───────────────────────────────────────────────────────────────

    /**
     * Return all notifications for a user, newest first.
     */
    public List<Notification> getNotifications(String username) {
        return userRepository.findByUsername(username)
                .map(User::getNotifications)
                .orElse(Collections.emptyList());
    }

    /**
     * Return only unread notifications for a user.
     */
    public List<Notification> getUnread(String userId) {
        return getNotifications(userId).stream()
                .filter(n -> !n.getIsRead())
                .collect(Collectors.toList());
    }

    // ── Mark read ──────────────────────────────────────────────────────────

    /**
     * Mark a single notification as read using the positional $ operator.
     */
    public void markRead(String username, String notificationId) {
        Query query = Query.query(
                Criteria.where("username").is(username)
                        .and("notifications.id").is(notificationId));
        Update update = new Update().set("notifications.$.isRead", true);
        mongoTemplate.updateFirst(query, update, User.class);
    }

    /**
     * Mark all notifications as read for a user.
     */
    public void markAllRead(String username) {
        Query query = Query.query(Criteria.where("username").is(username));
        // arrayFilters approach for all elements
        Update update = new Update()
                .filterArray(Criteria.where("n.read").is(false))
                .set("notifications.$[n].isRead", true);
        mongoTemplate.updateFirst(query, update, User.class);
    }

    /**
     * If the user has more than MAX_NOTIFICATIONS, drop the excess oldest ones.
     * Done as a separate update to keep the push simple.
     */
    private void trimIfNeeded(String userId) {
        userRepository.findById(userId).ifPresent(user -> {
            if (user.getNotifications().size() > MAX_NOTIFICATIONS) {
                List<Notification> trimmed = user.getNotifications()
                        .subList(0, MAX_NOTIFICATIONS);
                user.setNotifications(trimmed);
                userRepository.save(user);
            }
        });
    }
}
