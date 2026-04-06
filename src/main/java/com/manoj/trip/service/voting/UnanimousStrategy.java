package com.manoj.trip.service.voting;


import com.manoj.trip.enums.StopStatus;
import com.manoj.trip.enums.VoteType;
import com.manoj.trip.model.Vote;

import java.util.List;
/**
 * UNANIMOUS strategy:
 *  - CONFIRMED if every active member has voted APPROVE (no missing votes, no REJECTs).
 *  - REJECTED  immediately if any member votes REJECT.
 *  - PROPOSED  if everyone has voted but some ABSTAINed
 *              OR not all members have voted yet.
 *
 * Edge cases:
 *  - A single ABSTAIN with no REJECT keeps the stop in PROPOSED indefinitely.
 *    Organizer should use ORGANIZER mode instead if they want to override.
 */
public class UnanimousStrategy implements VotingStrategy {

    @Override
    public StopStatus resolve(List<Vote> votes, int memberCount) {
        if(memberCount == 0 || votes.isEmpty() || votes == null) return StopStatus.PROPOSED;

        boolean hasRejection = votes.stream().anyMatch(vote -> vote.getVoteType() == VoteType.REJECT);
        if(hasRejection) return StopStatus.REJECTED;

        long approvalCount = votes.stream().filter(vote -> vote.getVoteType() == VoteType.APPROVE).count();

        if(approvalCount == memberCount) return StopStatus.CONFIRMED;

        return StopStatus.PROPOSED;
    }
}
