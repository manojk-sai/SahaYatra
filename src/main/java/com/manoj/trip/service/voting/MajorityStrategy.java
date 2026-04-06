package com.manoj.trip.service.voting;

import com.manoj.trip.enums.StopStatus;
import com.manoj.trip.enums.VoteType;
import com.manoj.trip.model.Stop;
import com.manoj.trip.model.Vote;

import java.util.List;

public class MajorityStrategy implements VotingStrategy{

    @Override
    public StopStatus resolve(List<Vote> votes, int memberCount) {
        if (votes == null || votes.isEmpty() || memberCount == 0) return StopStatus.PROPOSED;

        long approveCount = votes.stream().filter(v -> v.getVoteType() == VoteType.APPROVE).count();
        long rejectCount = votes.stream().filter(v -> v.getVoteType() == VoteType.REJECT).count();

        double threshold = memberCount / 2.0 ;

        if(approveCount > threshold) return StopStatus.CONFIRMED;
        if(rejectCount > threshold) return StopStatus.REJECTED;

        return StopStatus.PROPOSED;
    }
}
