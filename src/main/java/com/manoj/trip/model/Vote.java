package com.manoj.trip.model;

import com.manoj.trip.enums.VoteType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class Vote {
    private String userId;
    private VoteType voteType;
    private LocalDateTime castAt;
}
