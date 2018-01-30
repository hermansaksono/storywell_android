package edu.neu.ccs.wellness.fitness.interfaces;

import java.util.List;

import edu.neu.ccs.wellness.fitness.challenges.AvailableChallenge;

/**
 * Created by hermansaksono on 10/16/17.
 */

public interface GroupChallengeInterface {

    enum ChallengeUnit {
        UNKNOWN,
        STEPS,
        MINUTES,
        DISTANCE
    }

    String getText();

    String getSubtext();

    List<AvailableChallenge> getAvailableChallenges();

}
