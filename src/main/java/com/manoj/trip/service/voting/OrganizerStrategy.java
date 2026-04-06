package com.manoj.trip.service.voting;

import com.manoj.trip.enums.StopStatus;
import com.manoj.trip.model.Vote;

import java.util.List;
/**
 * ORGANIZER strategy:
 *  Votes from members are purely advisory — they never auto-resolve the stop.
 *  The organizer reads the tally and manually advances the stop status
 *  via PATCH /trips/{id}/stops/{stopId}/status.
 *
 *  This strategy always returns PROPOSED so the auto-resolution
 *  in VotingService.castVote() is a no-op.
 */
public class OrganizerStrategy implements VotingStrategy{
    @Override
    public StopStatus resolve(List<Vote> votes, int memberCount) {
        return StopStatus.PROPOSED;
    }
}
