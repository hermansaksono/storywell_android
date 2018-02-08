package edu.neu.ccs.wellness.fitness.interfaces;

import edu.neu.ccs.wellness.fitness.challenges.Challenge;
import edu.neu.ccs.wellness.server.RestServer;

/**
 * Created by hermansaksono on 2/5/18.
 */

public interface ChallengeManagerInterface {

    ChallengeStatus getStatus();

    AvailableChallengesInterface getAvailableChallenges();

    void setRunningChallenge(Challenge challenge);

    Challenge getRunningChallenge();

    RestServer.ResponseType syncRunningChallenge();

    void completeChallenge();

    void syncCompletedChallenge();
}
