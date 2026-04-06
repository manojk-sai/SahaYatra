package com.manoj.trip.dto.request;

import com.manoj.trip.enums.VoteType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NonNull;

@Data
public class CastVoteRequest {
    @NotNull (message = "voteType is required")
    private VoteType voteType;
}
