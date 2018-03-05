package edu.neu.ccs.wellness.fitness.interfaces;

import android.content.Context;

import java.net.ConnectException;

import edu.neu.ccs.wellness.fitness.challenges.Challenge;
import edu.neu.ccs.wellness.fitness.challenges.RunningChallenge;
import edu.neu.ccs.wellness.server.RestServer;

/**
 * Created by hermansaksono on 2/5/18.
 */

public interface ChallengeManagerInterface {

    ChallengeStatus getStatus();

    void setStatus(String status);

    AvailableChallengesInterface getAvailableChallenges(Context context);

    void setRunningChallenge(Challenge challenge);

    RunningChallenge getRunningChallenge(Context context);

    RestServer.ResponseType syncRunningChallenge();

    int manageChallenge();

    void completeChallenge();

    void syncCompletedChallenge();
}
