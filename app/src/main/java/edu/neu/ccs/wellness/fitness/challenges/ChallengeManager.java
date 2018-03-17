package edu.neu.ccs.wellness.fitness.challenges;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import edu.neu.ccs.wellness.fitness.interfaces.ChallengeManagerInterface;
import edu.neu.ccs.wellness.fitness.interfaces.ChallengeStatus;
import edu.neu.ccs.wellness.fitness.interfaces.AvailableChallengesInterface;
import edu.neu.ccs.wellness.server.RestServer;
import edu.neu.ccs.wellness.server.RestServer.ResponseType;
import edu.neu.ccs.wellness.server.WellnessRestServer;
import edu.neu.ccs.wellness.utils.WellnessIO;

/**
 * Created by hermansaksono on 2/5/18.
 */

public class ChallengeManager implements ChallengeManagerInterface {
    // STATIC VARIABLES
    private static final String REST_RESOURCE = "group/challenges2";
    private static final String FILENAME = "challengeManager.json";
    private static final String JSON_FIELD_STATUS = "status";
    private static final String JSON_FIELD_AVAILABLE = "available";
    private static final String JSON_FIELD_UNSYNCED_RUN = "unsynced_run";
    private static final String JSON_FIELD_RUNNING = "running";
    private static final ChallengeStatus DEFAULT_STATUS = ChallengeStatus.UNSTARTED;
    private static final String DEFAULT_STATUS_STRING = ChallengeStatus.toStringCode(DEFAULT_STATUS);

    // PRIVATE VARIABLES
    private RestServer server;
    private Context context;
    private JSONObject jsonObject;

    // PRIVATE CONSTRUCTORS
    private ChallengeManager(RestServer server, Context context) {
        this.server = server;
        this.context = context.getApplicationContext();
    }

    // STATIC FACTORY METHOD
    public static ChallengeManagerInterface create(RestServer server, Context context){
        return new ChallengeManager(server, context);
    }

    // PUBLIC METHODS
    /**
     * Get the user's current ChallengeStatus
     * @return ChallengeStatus
     */
    @Override
    public ChallengeStatus getStatus() {
        String statusString = this.getSavedChallengeJson().optString(JSON_FIELD_STATUS, DEFAULT_STATUS_STRING);
        return ChallengeStatus.fromStringCode(statusString);
    }

    /**
     * Sets the user's current ChallengeStatus
     * @param status The new ChallengeStatus
     */
    @Override
    public void setStatus(String status) {
        try {
            this.getSavedChallengeJson().put(JSON_FIELD_STATUS, status);
            this.saveChallengeJson();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the a list of available challenges
     * INVARIANT: ChallengeStatus is either UNSTARTED or AVAILABLE
     * @return Currently running challenge
     */
    @Override
    public AvailableChallengesInterface getAvailableChallenges(){
        AvailableChallengesInterface availableChallenges = null;
        try {
            JSONObject availableJson = this.getSavedChallengeJson().getJSONObject(JSON_FIELD_AVAILABLE);
            availableChallenges = AvailableChallenges.create(availableJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return availableChallenges;
    }

    /**
     * Get the currently unsynced running Challenge
     * INVARIANT: the ChallengeStatus is UNSYNCED_RUN.
     * @return Currently running but unsynced challenge
     */
    @Override
    public Challenge getUnsyncedChallenge() {
        Challenge unsyncedChallenge = null;
        try {
            JSONObject challengesJson = this.getSavedChallengeJson().getJSONObject(JSON_FIELD_UNSYNCED_RUN);
            unsyncedChallenge = Challenge.create(challengesJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return unsyncedChallenge;
    }

    /**
     * Get the user's currently running challenge
     * INVARIANT: ChallengeStatus is RUNNING
     * @return Currently running challenge
     */
    @Override
    public RunningChallenge getRunningChallenge() {
        RunningChallenge runningChallenge = null;
        try {
            JSONObject runningChallengesJson = this.getSavedChallengeJson().getJSONObject(JSON_FIELD_RUNNING);
            runningChallenge = RunningChallenge.create(runningChallengesJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return runningChallenge;
    }

    /**
     * Set the running challenge to the given challenge
     * INVARIANT: ChallengeStatus is AVAILABLE
     * @param challenge The challenge that will be set as the running challenge
     */
    @Override
    public void setRunningChallenge(Challenge challenge) {
        try {
            JSONObject jsonObject = this.getSavedChallengeJson();
            ChallengeStatus newStatus = ChallengeStatus.UNSYNCED_RUN;

            jsonObject.put(JSON_FIELD_STATUS,  ChallengeStatus.toStringCode(newStatus));
            jsonObject.put(JSON_FIELD_AVAILABLE, null);
            jsonObject.put(JSON_FIELD_UNSYNCED_RUN, challenge.getJsonText());
            this.saveChallengeJson();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Post the running challenge to the REST server.
     * INVARIANT: ChallengeStatus is UNSYNCED_RUN and there is an internet connection.
     * @return The result of synchronization.
     */
    @Override
    public ResponseType syncRunningChallenge() {
        try {
            JSONObject jsonObject = this.getSavedChallengeJson();
            Challenge unsyncedChallenge = Challenge.create(jsonObject.getJSONObject(JSON_FIELD_UNSYNCED_RUN));
            String jsonString = this.postChallenge(unsyncedChallenge);
            JSONObject jsonUnsyncedObject = new JSONObject(jsonString);
            ChallengeStatus newStatus = ChallengeStatus.RUNNING;

            jsonObject.put(JSON_FIELD_STATUS,  ChallengeStatus.toStringCode(newStatus));
            jsonObject.put(JSON_FIELD_AVAILABLE, null);
            jsonObject.put(JSON_FIELD_UNSYNCED_RUN, null);
            jsonObject.put(JSON_FIELD_RUNNING, RunningChallenge.create(jsonUnsyncedObject));
            this.saveChallengeJson();
            return ResponseType.SUCCESS_202;
        } catch (JSONException e) {
            e.printStackTrace();
            return ResponseType.BAD_JSON;
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseType.NO_INTERNET;
        }
    }

    /**
     * Sets the challenge as COMPLETED. However, the challenge needs to be synced with the server.
     * INVARIANT: Challenge status is RUNNING.
     */
    @Override
    public void completeChallenge() {
        try {
            ChallengeStatus newStatus = ChallengeStatus.COMPLETED;

            JSONObject jsonObject = this.getSavedChallengeJson();
            jsonObject.put(JSON_FIELD_STATUS,  ChallengeStatus.toStringCode(newStatus));
            jsonObject.put(JSON_FIELD_AVAILABLE, null);
            jsonObject.put(JSON_FIELD_UNSYNCED_RUN, null);
            this.saveChallengeJson();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Synchronize a COMPLETED challenge to the RestServer. This will get a new set of challenges.
     * INVARIANT: Challenge status is COMPLETED  and there is an internet connection.
     */
    @Override
    public void syncCompletedChallenge() {
        this.jsonObject = requestJson(this.server, this.context, false);
    }

    /* PRIVATE METHODS */
    private JSONObject getSavedChallengeJson() {
        if (this.jsonObject == null) {
            this.jsonObject = requestJson(this.server, this.context, true);
        }
        return this.jsonObject;
    }

    private void saveChallengeJson() {
        String jsonString = this.getSavedChallengeJson().toString();
        WellnessIO.writeFileToStorage(this.context, FILENAME, jsonString);
    }

    private String postChallenge(Challenge challenge) throws IOException {
        String jsonText = challenge.getJsonText();
        return server.doPostRequestFromAResource(jsonText, REST_RESOURCE);
    }

    /* PUBLIC STATIC HELPER METHODS */
    private static String requestJsonString(RestServer server, Context context, boolean useSaved) {
        try {
            return server.doGetRequestFromAResource(context, FILENAME, REST_RESOURCE, useSaved);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static JSONObject requestJson(RestServer server, Context context, boolean useSaved) {
        try {
            String jsonString = requestJsonString(server, context, useSaved);
            return new JSONObject(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
    TODO HS: I can see the merit of this function, but the current implementation is unclear.
    (1) Why do this function sometimes returns 0, 1, or 2. What are the meanings of these numbers?
    Since the codes give little information about the meaning of the numbers, this can create a
    confusion when other programmers are trying to understand at the code.
    (2) The method getAvailableChallenges() is called twice, butI am unsure why it has to appear in
    two different conditional blocks. Furthermore, (in the previous implementation)
    getAvailableChallenges() and getRunningChallenge() used to call the same resource , so I am
    not sure why using different method for a same purpose.

    I am proposing to delete this method. But, since I think the functionality is needed (thank you
    for pointing this out!) I am proposing a similar function called
    fetchChallengeDataFromRestServer(). See the implementation below.

    */

    /*
    Should get Challenges from server/local depending on the status
    */
    public int manageChallenge(){
        if(getStatus() == ChallengeStatus.AVAILABLE) {
            getAvailableChallenges();
            return 0;
        }
        else if(getStatus() == ChallengeStatus.RUNNING){
            getRunningChallenge();
            return 1;
        }
        else if(getStatus() == ChallengeStatus.UNINITIALIZED){
           getAvailableChallenges();
           setStatus("AVAILABLE");
           return 0;
        }
        return 2;
    }

    /**
     * Do a GET request to the (@link RestServer) to get the most up-to-date challenge data. Then
     * store the GET response into phone's local storage.
     */
    public void fetchChallengeDataFromRestServer() {
        this.jsonObject = requestJson(this.server, this.context, WellnessRestServer.DONT_USE_SAVED);
    }
}
