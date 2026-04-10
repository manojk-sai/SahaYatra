package com.manoj.trip.event;

import com.manoj.trip.enums.TripStatus;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;
@Getter
public class TripStatusChangedEvent extends ApplicationEvent {
    private final String     tripId;
    private final String     tripTitle;
    private final TripStatus newStatus;

    public TripStatusChangedEvent(Object source,
                                  String tripId,
                                  String tripTitle,
                                  TripStatus newStatus) {
        super(source);
        this.tripId    = tripId;
        this.tripTitle = tripTitle;
        this.newStatus = newStatus;
    }
}
