package com.manoj.trip.service;

import com.manoj.trip.enums.NotificationType;
import com.manoj.trip.enums.StopStatus;
import com.manoj.trip.event.MemberJoinedEvent;
import com.manoj.trip.event.StopProposedEvent;
import com.manoj.trip.event.TripStatusChangedEvent;
import com.manoj.trip.event.VoteResolvedEvent;
import com.manoj.trip.model.Notification;
import com.manoj.trip.model.Trip;
import com.manoj.trip.model.TripMembership;
import com.manoj.trip.model.User;
import com.manoj.trip.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class NotificationListener {

    private final NotificationService notificationService;
    private final TripRepository tripRepository;

    /**
     * Notifies all active members except the proposer when a stop is proposed.
     */
    @EventListener
    @Async
    public void handleStopProposed(StopProposedEvent event) {

        List<String> recipients = getActiveMemberIds(event.getTripId()).stream()
                .filter(userId -> !userId.equals(event.getProposerUsername())) // Don't notify the proposer;
                .collect(Collectors.toList());

        String message = String.format("%s proposed a new stop: %s in %s trip",
                event.getProposerUsername(), event.getStopName(), event.getTripTitle());

        notificationService.pushToMany(
                recipients,
                NotificationType.STOP_PROPOSED,
                message,
                event.getTripId(),
                event.getTripTitle());
    }

    /**
     * Notifies all active members when a stop is auto-confirmed or rejected.
     */
    @Async
    @EventListener
    public void onVoteResolved(VoteResolvedEvent event) {
        List<String> recipients = getActiveMemberIds(event.getTripId());

        String outcome = event.getResolvedStatus() == StopStatus.CONFIRMED
                ? "confirmed ✓" : "rejected ✗";
        String message = String.format(
                "Stop '%s' was %s in '%s'",
                event.getStopName(), outcome, event.getTripTitle());

        notificationService.pushToMany(
                recipients,
                NotificationType.VOTE_RESOLVED,
                message,
                event.getTripId(),
                event.getTripTitle());
    }

    /**
     * Notifies the organizer when a new member accepts their invite.
     */
    @Async
    @EventListener
    public void onMemberJoined(MemberJoinedEvent event) {

        String message = String.format(
                "A new member joined your trip '%s'", event.getTripTitle());

        notificationService.pushNotification(
                message,
                event.getOrganizerUsername(),
                NotificationType.MEMBER_JOINED,
                event.getTripId(),
                event.getTripTitle());
    }

    /**
     * Notifies all active members when the trip lifecycle advances.
     */
    @Async
    @EventListener
    public void handleTripStatusChanged(TripStatusChangedEvent event) {

        List<String> recipients = getActiveMemberIds(event.getTripId());
        String statusLabel = event.getNewStatus().name()
                .replace("_", " ")
                .toLowerCase();
        String message = String.format(
                "The trip '%s' is now %s", event.getTripTitle(), statusLabel);

        notificationService.pushToMany(
                recipients,
                NotificationType.TRIP_STATUS_CHANGED,
                message,
                event.getTripId(),
                event.getTripTitle());
    }
    private List<String> getActiveMemberIds(String tripId) {
        return tripRepository.findById(tripId)
                .map(Trip::getMembers)
                .orElse(List.of())
                .stream()
                .filter(TripMembership::isActive)
                .map(TripMembership::getUserId)
                .collect(Collectors.toList());
    }
}
