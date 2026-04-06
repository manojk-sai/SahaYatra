package com.manoj.trip.service.voting;
import com.manoj.trip.enums.VotingMode;
import org.springframework.stereotype.Component;

/**
 * Factory that maps a Trip's VotingMode to the correct VotingStrategy.
 *
 * Strategies are stateless so a single instance per type is safe to reuse.
 * If you add a new VotingMode in the future, add a case here.
 */
@Component
public class VotingStrategyFactory {

    private static final MajorityStrategy  MAJORITY   = new MajorityStrategy();
    private static final UnanimousStrategy UNANIMOUS  = new UnanimousStrategy();
    private static final OrganizerStrategy ORGANIZER  = new OrganizerStrategy();

    /**
     * Returns the correct strategy for the given voting mode.
     *
     * @param mode the trip's configured VotingMode
     * @return a VotingStrategy instance
     */
    public VotingStrategy forMode(VotingMode mode) {
        if (mode == null) return MAJORITY; // safe default
        return switch (mode) {
            case MAJORITY   -> MAJORITY;
            case UNANIMOUS  -> UNANIMOUS;
            case ORGANIZER  -> ORGANIZER;
        };
    }
}