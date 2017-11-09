package edu.neu.ccs.wellness.fitness.interfaces;

/**
 * Created by hermansaksono on 10/16/17.
 */

public interface GroupChallengeInterface {

    enum ChallengeStatus {
        UNINITIATED,
        AVAILABLE,
        RUNNING
    }

    enum ChallengeUnit {
        UNKNOWN,
        STEPS,
        MINUTES,
        DISTANCE
    }

    ChallengeStatus getStatus();

    String getText();

    String getSubtext();

}
