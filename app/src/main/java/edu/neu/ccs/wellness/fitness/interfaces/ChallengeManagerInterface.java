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

    /**
     * Return the user's current ChallengeStatus. If there is no saved challenge in internal storage
     * then return ChallengeStatus.UNITIALIZED
     * @return The status of user's Challenge
     */
    ChallengeStatus getStatus();

    /**
     * Set the user's status.
     * TODO HS: I'm considering to remove this as status changes should be handled by the class
     * @param status
     */
    void setStatus(String status);

    /**
     * Get the a list of available challenges if the ChallengeStatus is either UNSTARTED or AVAILABLE.
     * @param context
     * @return Available challenges
     */
    AvailableChallengesInterface getAvailableChallenges(Context context);

    /**
     * Set the running challenge if the ChallengeStatus is AVAILABLE. Then sets the status to
     * UNSYNCED_RUN. This function MUST save the given challenge to a persistent storage.
     * It should not sync the given challenge to server.
     * @param challenge
     */
    void setRunningChallenge(Challenge challenge);

    /**
     * Get the currently running Challenge if the ChallengeStatus is UNSYNCED_RUN or RUNNING.
     * @param context
     * @return Currently running challenge
     */
    RunningChallenge getRunningChallenge(Context context);

    /**
     * Sync the running challenge (that was saved in the persistent storage) with the server.
     * @return The status of the synchronization
     */
    RestServer.ResponseType syncRunningChallenge();

    /**
     *
     */
    int manageChallenge();

    void completeChallenge();

    void syncCompletedChallenge();
}
