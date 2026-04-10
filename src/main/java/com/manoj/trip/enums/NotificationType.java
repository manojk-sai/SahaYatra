package com.manoj.trip.enums;

public enum NotificationType {
    STOP_PROPOSED,        // A member proposed a new stop
    VOTE_RESOLVED,        // A stop was confirmed or rejected
    MEMBER_JOINED,        // A new member accepted an invite
    TRIP_STATUS_CHANGED   // Trip lifecycle advanced (PLANNING → CONFIRMED etc.)
}
