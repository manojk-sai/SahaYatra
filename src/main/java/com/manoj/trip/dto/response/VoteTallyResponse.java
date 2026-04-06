package com.manoj.trip.dto.response;

import com.manoj.trip.enums.StopStatus;
import com.manoj.trip.enums.VoteType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class VoteTallyResponse {
    private String stopId;
    private String stopName;
    private StopStatus stopStatus;

    private int approveCount;
    private int rejectCount;
    private int abstainCount;
    private int totalVotes;
    private int memberCount;
    private int pendingCount;

    private boolean hasVoted;
    private VoteType voteType;

    private List<VoteEntry> votes;

    @Data
    @Builder
    public static class VoteEntry {
        private String userId;
        private String userName;
        private VoteType voteType;
        private LocalDateTime castAt;
    }
}
