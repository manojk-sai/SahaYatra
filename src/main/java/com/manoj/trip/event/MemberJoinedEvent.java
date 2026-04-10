package com.manoj.trip.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
@Getter
public class MemberJoinedEvent extends ApplicationEvent {
    private final String tripId;
    private final String tripTitle;
    private final String newMemberUsername;
    private final String organizerUsername;

    public MemberJoinedEvent(Object source,
                             String tripId,
                             String tripTitle,
                             String newMemberUsername,
                             String organizerUsername) {
        super(source);
        this.tripId          = tripId;
        this.tripTitle       = tripTitle;
        this.newMemberUsername = newMemberUsername;
        this.organizerUsername = organizerUsername;
    }
}
