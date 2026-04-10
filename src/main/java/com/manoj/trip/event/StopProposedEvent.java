package com.manoj.trip.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
@Getter
public class StopProposedEvent extends ApplicationEvent {

    private final String tripId;
    private final String tripTitle;
    private final String stopName;
    private final String proposerUsername;

    public StopProposedEvent(Object source,
                             String tripId,
                            String tripTitle,
                            String stopName,
                            String proposerUsername) {
        super(source);
        this.tripId = tripId;
        this.tripTitle = tripTitle;
        this.stopName = stopName;
        this.proposerUsername = proposerUsername;
    }
}
