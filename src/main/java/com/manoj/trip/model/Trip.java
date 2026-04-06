package com.manoj.trip.model;

import com.manoj.trip.enums.TripStatus;
import com.manoj.trip.enums.TripVisibility;
import com.manoj.trip.enums.VotingMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Document(collection = "trips")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Trip {
    @Id
    private String id;
    private String title;
    private String description;
    @Builder.Default
    private TripStatus status = TripStatus.PLANNING;
    private TripVisibility visibility = TripVisibility.INVITE_ONLY;
    private boolean locked; //Organizer locks the trip when completed
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal budgetCap;
    @Builder.Default
    private List<Stop> stops = new ArrayList<>();
    @Builder.Default
    private List<TripMembership> members = new ArrayList<>();
    @Builder.Default
    private VotingMode votingMode = VotingMode.MAJORITY;
}
