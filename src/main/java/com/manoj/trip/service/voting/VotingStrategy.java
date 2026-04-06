package com.manoj.trip.service.voting;

import com.manoj.trip.enums.StopStatus;
import com.manoj.trip.model.Vote;

import java.util.List;

/**
 * Strategy interface for resolving a stop's status from its vote list.
 *
 * Each implementation encodes one voting rule.
 * The factory selects the correct one based on Trip.votingMode.
 *
 * Contract:
 *  - Returns CONFIRMED if the threshold for approval is met.
 *  - Returns REJECTED  if the threshold for rejection is met.
 *  - Returns PROPOSED  if neither threshold is met yet (voting still open).
 */
public interface VotingStrategy {

    /**
     * @param votes       current list of votes on the stop
     * @param memberCount number of active trip members eligible to vote
     * @return the resolved StopStatus
     */
    StopStatus resolve(List<Vote> votes, int memberCount);
}
