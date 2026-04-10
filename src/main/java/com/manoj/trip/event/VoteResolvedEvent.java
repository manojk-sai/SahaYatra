package com.manoj.trip.event;

import com.manoj.trip.enums.StopStatus;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;
@Getter
public class VoteResolvedEvent extends ApplicationEvent {
    private final String     tripId;
    private final String     tripTitle;
    private final String     stopId;
    private final String     stopName;
    private final StopStatus resolvedStatus;

    public VoteResolvedEvent(Object source,
                             String tripId,
                             String tripTitle,
                             String stopId,
                             String stopName,
                             StopStatus resolvedStatus) {
        super(source);
        this.tripId         = tripId;
        this.tripTitle      = tripTitle;
        this.stopId         = stopId;
        this.stopName       = stopName;
        this.resolvedStatus = resolvedStatus;
    }
}
