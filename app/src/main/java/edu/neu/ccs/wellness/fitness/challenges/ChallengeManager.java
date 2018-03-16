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
import edu.neu.ccs.wellness.utils.WellnessIO;

/**
 * Created by hermansaksono on 2/5/18.
 */

public class ChallengeManager implements ChallengeManagerInterface {
    // STATIC VARIABLES
    public static final String REST_RESOURCE = "group/challenges2";
    public static final String FILENAME = "challengeManager.json";
    public static final String JSON_FIELD_STATUS = "status";
    public static final String JSON_FIELD_AVAILABLE = "available";
    public static final String JSON_FIELD_UNSYNCED_RUN = "unsynced_run";
    public static final String JSON_FIELD_RUNNING = "running";

    // PRIVATE VARIABLES
    private ChallengeStatus status = ChallengeStatus.UNINITIALIZED;

    private RestServer server;
    private Context context;
    private JSONObject jsonObject;

    // PRIVATE CONSTRUCTORS
    private ChallengeManager(RestServer server, Context context) {
        this.server = server;
        this.context = context;
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
        if (this.status == null) {
            String defaultStatus = ChallengeStatus.toStringCode(ChallengeStatus.UNINITIALIZED);
            String statusString = this.getChallengeJson().optString(JSON_FIELD_STATUS, defaultStatus);
            this.status = ChallengeStatus.fromStringCode(statusString);
        }
        return this.status;
    }

    /**
     * Sets the user's current ChallengeStatus
     * @param status The new ChallengeStatus
     */
    @Override
    public void setStatus(String status) {
        try {
            this.status = ChallengeStatus.fromStringCode(status);
            this.getChallengeJson().put(JSON_FIELD_STATUS, status);
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
    public AvailableChallengesInterface getAvailableChallenges(Context context){
        AvailableChallengesInterface availableChallenges = null;
        try {
            JSONObject availableJson = this.getChallengeJson().getJSONObject(JSON_FIELD_AVAILABLE);
            availableChallenges = AvailableChallenges.create(availableJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return availableChallenges;
    }

    /**
     * Get the currently unsynced running Challenge
     * INVARIANT: the ChallengeStatus is UNSYNCED_RUN.
     * @param context
     * @return Currently running but unsynced challenge
     */
    @Override
    public Challenge getUnsyncedChallenge(Context context) {
        Challenge unsyncedChallenge = null;
        try {
            JSONObject challengesJson = this.getChallengeJson().getJSONObject(JSON_FIELD_UNSYNCED_RUN);
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
    public RunningChallenge getRunningChallenge(Context context) {
        RunningChallenge runningChallenge = null;
        try {
            JSONObject runningChallengesJson = this.getChallengeJson().getJSONObject(JSON_FIELD_RUNNING);
            runningChallenge = RunningChallenge.create(runningChallengesJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return runningChallenge;
    }

    /**
     * Set the running challenge to the given challenge
     * INVARIANT: ChallengeStatus is AVAILABLE
     * @param challenge
     */
    @Override
    public void setRunningChallenge(Challenge challenge) {
        try {
            JSONObject jsonObject = this.getChallengeJson();

            this.status = ChallengeStatus.UNSYNCED_RUN;

            jsonObject.put(JSON_FIELD_STATUS,  ChallengeStatus.toStringCode(this.status));
            jsonObject.put(JSON_FIELD_AVAILABLE, null);
            jsonObject.put(JSON_FIELD_UNSYNCED_RUN, challenge.getJsonText());
            this.saveChallengeJson();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Post the running challenge to the REST server
     * INVARIANT: ChallengeStatus is UNSYNCED_RUN
     * @return
     */
    @Override
    public ResponseType syncRunningChallenge() {
        if (this.server.isOnline(this.context) == false) {
            return ResponseType.NO_INTERNET;
        } else {
            try {
                JSONObject jsonObject = this.getChallengeJson();
                Challenge unsyncedChallenge = Challenge.create(jsonObject.getJSONObject(JSON_FIELD_UNSYNCED_RUN));
                String jsonString = postChallenge(unsyncedChallenge);
                JSONObject jsonUnsyncedObject = new JSONObject(jsonString);

                this.status = ChallengeStatus.RUNNING;

                jsonObject.put(JSON_FIELD_STATUS,  ChallengeStatus.toStringCode(this.status));
                jsonObject.put(JSON_FIELD_AVAILABLE, null);
                jsonObject.put(JSON_FIELD_UNSYNCED_RUN, null);
                jsonObject.put(JSON_FIELD_RUNNING, RunningChallenge.create(jsonUnsyncedObject));
                this.saveChallengeJson();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return ResponseType.SUCCESS_202;
        }
    }

    @Override
    public void completeChallenge() {
        if (this.getStatus() == ChallengeStatus.RUNNING) {
            try {
                this.status = ChallengeStatus.UNSTARTED;

                JSONObject jsonObject = this.getChallengeJson();
                jsonObject.put(JSON_FIELD_STATUS,  ChallengeStatus.toStringCode(this.status));
                jsonObject.put(JSON_FIELD_AVAILABLE, null);
                jsonObject.put(JSON_FIELD_UNSYNCED_RUN, null);
                jsonObject.put(JSON_FIELD_RUNNING, null);
                this.saveChallengeJson();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void syncCompletedChallenge() {
        if (this.getStatus() == ChallengeStatus.UNSTARTED) {
            this.status = ChallengeStatus.AVAILABLE;
            this.jsonObject = requestJsonChallenge(this.server, this.context, false);
        }
    }

    /* PRIVATE METHODS */
    private JSONObject getChallengeJson () {
        if (this.jsonObject == null) {
            this.jsonObject = requestJsonChallenge(this.server, this.context, true);
        }
        return this.jsonObject;
    }

    private void saveChallengeJson() {
        String jsonString = this.getChallengeJson().toString();
        WellnessIO.writeFileToStorage(this.context, FILENAME, jsonString);
    }

    private String postChallenge(Challenge challenge) {
        String response = null;
        try {
            String jsonText = challenge.getJsonText();
            response = server.doPostRequestFromAResource(jsonText, REST_RESOURCE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
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

    private static JSONObject requestJsonChallenge(RestServer server, Context context, boolean useSaved) {
        try {
            String jsonString = requestJsonString(server, context, useSaved);
            JSONObject jsonObject = new JSONObject(jsonString);
            return jsonObject;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
    Should get Challenges from server/local depending on the status
    */
    public int manageChallenge(){
        if(getStatus() == ChallengeStatus.AVAILABLE) {
            getAvailableChallenges(this.context);
            return 0;
        }
        else if(getStatus() == ChallengeStatus.RUNNING){
            getRunningChallenge(this.context);
            return 1;
        }
        else if(getStatus() == ChallengeStatus.UNINITIALIZED){
           getAvailableChallenges(this.context);
           setStatus("AVAILABLE");
           return 0;
        }
        return 2;
    }
}
