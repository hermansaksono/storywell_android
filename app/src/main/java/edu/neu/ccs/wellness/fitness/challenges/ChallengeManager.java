package edu.neu.ccs.wellness.fitness.challenges;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import edu.neu.ccs.wellness.fitness.interfaces.UnitChallengeInterface;
import edu.neu.ccs.wellness.fitness.interfaces.ChallengeManagerInterface;
import edu.neu.ccs.wellness.fitness.interfaces.ChallengeStatus;
import edu.neu.ccs.wellness.fitness.interfaces.AvailableChallengesInterface;
import edu.neu.ccs.wellness.server.RestServer;
import edu.neu.ccs.wellness.server.RestServer.ResponseType;
import edu.neu.ccs.wellness.server.WellnessRestServer;
import edu.neu.ccs.wellness.server.WellnessRepository;

/**
 * Created by hermansaksono on 2/5/18.
 */

public class ChallengeManager implements ChallengeManagerInterface {
    // STATIC VARIABLES
    private static final String REST_RESOURCE = "group/challenges";
    private static final String REST_RESOURCE_COMPLETED = REST_RESOURCE.concat("/set_completed");
    private static final String FILENAME = "challengeManager.json";
    private static final String JSON_FIELD_STATUS = "status";
    private static final String JSON_FIELD_AVAILABLE = "available";
    private static final String JSON_FIELD_UNSYNCED_RUN = "unsynced_run";
    private static final String JSON_FIELD_RUNNING = "running";
    private static final ChallengeStatus DEFAULT_STATUS = ChallengeStatus.UNSTARTED;
    private static final String DEFAULT_STATUS_STRING = ChallengeStatus.toStringCode(DEFAULT_STATUS);

    // PRIVATE VARIABLES
    private Context context;
    private JSONObject jsonObject;
    private WellnessRepository repository;


    // PRIVATE CONSTRUCTORS2
    private ChallengeManager(RestServer server, Context context) {
        this.context = context.getApplicationContext();
        this.repository = new WellnessRepository(server, context);
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
    public ChallengeStatus getStatus() throws IOException, JSONException {
        String statusString = this.getSavedChallengeJson()
                .optString(JSON_FIELD_STATUS, DEFAULT_STATUS_STRING);
        return ChallengeStatus.fromStringCode(statusString);
    }

    /**
     * Sets the user's current ChallengeStatus
     * @param status The new ChallengeStatus
     */
    private void setStatus(ChallengeStatus status) throws IOException {
        try {
            this.getSavedChallengeJson().put(JSON_FIELD_STATUS,
                    ChallengeStatus.toStringCode(status));
            this.saveChallengeJson();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the a list of available challenges
     * INVARIANT: ChallengeStatus is either UNSTARTED or AVAILABLE or CLOSED
     * @return Currently running challenge
     */
    @Override
    public AvailableChallengesInterface getAvailableChallenges() throws IOException, JSONException {
        this.setStatus(ChallengeStatus.AVAILABLE);
        JSONObject availableJson = new JSONObject(this.getSavedChallengeJson()
                .getString(JSON_FIELD_AVAILABLE));
        return AvailableChallenges.create(availableJson);
    }

    /**
     * Get the currently unsynced running UnitChallenge
     * INVARIANT: the ChallengeStatus is UNSYNCED_RUN.
     * @return Currently running but unsynced challenge
     */
    @Override
    public UnitChallengeInterface getUnsyncedChallenge() throws IOException, JSONException {
        JSONObject challengesJson = this.getSavedChallengeJson().getJSONObject(JSON_FIELD_UNSYNCED_RUN);
        return UnitChallenge.newInstance(challengesJson);
    }

    /**
     * Get the user's currently running challenge
     * INVARIANT: ChallengeStatus is RUNNING
     * @return Currently running challenge
     */
    @Override
    public UnitChallengeInterface getRunningChallenge() throws IOException, JSONException {
        JSONObject runningChallengesJson = new JSONObject(this.getSavedChallengeJson().getString(JSON_FIELD_RUNNING));
        RunningChallenge runningChallenge = RunningChallenge.newInstance(runningChallengesJson);
        Log.d("SWELL", "Running Challenge JSON: " + runningChallengesJson.toString());
        return runningChallenge.getUnitChallenge();
    }

    @Override
    public UnitChallengeInterface getUnsyncedOrRunningChallenge() throws Exception {
        ChallengeStatus status = this.getStatus();
        if (status == ChallengeStatus.UNSYNCED_RUN) {
            return this.getUnsyncedChallenge();
        } else if (status == ChallengeStatus.RUNNING) {
            return this.getRunningChallenge();
        } else {
            throw new Exception("Current status must be UNSYNCED_RUN or RUNNING");
        }
    }

    /**
     * Set the running challenge to the given challenge
     * INVARIANT: ChallengeStatus is AVAILABLE
     * @param challenge The challenge that will be set as the running challenge
     */
    @Override
    public void setRunningChallenge(UnitChallenge challenge) throws IOException, JSONException {
        JSONObject jsonObject = this.getSavedChallengeJson();
        ChallengeStatus newStatus = ChallengeStatus.UNSYNCED_RUN;

        jsonObject.put(JSON_FIELD_STATUS,  ChallengeStatus.toStringCode(newStatus));
        jsonObject.put(JSON_FIELD_AVAILABLE, null);
        jsonObject.put(JSON_FIELD_UNSYNCED_RUN, challenge.getJsonText());
        this.saveChallengeJson();
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
            UnitChallenge unsyncedChallenge = UnitChallenge.newInstance(new JSONObject(jsonObject.getString(JSON_FIELD_UNSYNCED_RUN)));
            String jsonString = this.postChallenge(unsyncedChallenge);
            JSONObject jsonUnsyncedObject = new JSONObject(jsonString);
            ChallengeStatus newStatus = ChallengeStatus.RUNNING;
            jsonObject.put(JSON_FIELD_STATUS,  ChallengeStatus.toStringCode(newStatus));
            jsonObject.put(JSON_FIELD_AVAILABLE, null);
            jsonObject.put(JSON_FIELD_UNSYNCED_RUN, null);
            jsonObject.put(JSON_FIELD_RUNNING, jsonUnsyncedObject.getString("running"));
            this.saveChallengeJson();
            // TODO HS Need the codes for actual synchorization
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
     * Sets the challenge as CLOSED. However, the challenge needs to be synced with the server.
     * INVARIANT: UnitChallenge status is RUNNING.
     */
    @Override
    public void closeChallenge() throws IOException, JSONException {
        ChallengeStatus newStatus = ChallengeStatus.CLOSED;

        JSONObject jsonObject = this.getSavedChallengeJson();
        jsonObject.put(JSON_FIELD_STATUS,  ChallengeStatus.toStringCode(newStatus));
        jsonObject.put(JSON_FIELD_AVAILABLE, null);
        jsonObject.put(JSON_FIELD_UNSYNCED_RUN, null);
        //this.saveChallengeJson();
        this.doSetChallengeClosed();
    }

    /**
     * Synchronize a CLOSED challenge to the RestServer. This will get a new set of challenges.
     * INVARIANT: UnitChallenge status is CLOSED  and there is an internet connection.
     */
    @Override
    public void syncCompletedChallenge() throws IOException, JSONException {
        this.jsonObject = repository.requestJson(this.context, false, FILENAME, REST_RESOURCE);
    }

    /* PRIVATE METHODS */
    private void doRefreshJson() {
        try {
            this.jsonObject = repository.requestJson(context, false, FILENAME, REST_RESOURCE);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private JSONObject getSavedChallengeJson() throws IOException, JSONException {
        if (this.jsonObject == null) {
            this.jsonObject = repository.requestJson(this.context, true, FILENAME, REST_RESOURCE);
        }
        return this.jsonObject;
    }

    private void saveChallengeJson() throws IOException, JSONException {
        String jsonString = this.getSavedChallengeJson().toString();
        repository.writeFileToStorage(this.context, jsonString, FILENAME);
    }

    private String postChallenge(UnitChallenge challenge) throws IOException {
        String jsonText = challenge.getJsonText();
        return repository.postRequest(jsonText, REST_RESOURCE);
    }

    private void doSetChallengeClosed() {
        try {
            this.repository.getRequest(REST_RESOURCE_COMPLETED);
            this.doRefreshJson();
            this.setStatus(ChallengeStatus.CLOSED);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private UnitChallengeInterface getChallenge() {
        UnitChallenge challenge = null;
        try {
            JSONArray jsonArray = jsonObject.getJSONArray("progress");
            if (jsonArray.length() > 0) {
                JSONObject firstPersonProgress = (JSONObject) jsonArray.get(1);
                challenge = UnitChallenge.newInstance(firstPersonProgress);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return challenge;
    }

//    /* PUBLIC STATIC HELPER METHODS */
//    private static String requestJsonString(RestServer server, Context context, boolean useSaved) {
//        try {
//            return server.doGetRequestFromAResource(context, FILENAME, REST_RESOURCE, useSaved);
//        } catch (IOException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//
//    private static JSONObject requestJson(RestServer server, Context context, boolean useSaved) {
//        try {
//            String jsonString = requestJsonString(server, context, useSaved);
//            return new JSONObject(jsonString);
//        } catch (JSONException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }


    //this method is called by any other class wanting to change the status
    /*
    @Override
    public void changeChallengeStatus(int state) throws Exception {

        switch (state){

            case 0:
                setStatus("UNINITIALIZED");
                break;
            case 1:
                setStatus("AVAILABLE");
                break;
            case 2:
                setStatus("UNSYNCED_RUN");
                break;
            case 3:
                setStatus("RUNNING");
                break;
            case 4:
                setStatus("UNSTARTED");
                break;
            case 5:
                setStatus("CLOSED");
                break;
            case 6:
                setStatus("ERROR_CONNECTING");
                break;
            case 7:
                setStatus("MALFORMED_JSON");
                break;

             default:
                 throw new Exception();

        }
    }
    */

    /**
     * Do a GET request to the (@link RestServer) to get the most up-to-date challenge data. Then
     * store the GET response into phone's local storage.
     */
    public void fetchChallengeDataFromRestServer() throws IOException, JSONException {
        this.jsonObject = repository.requestJson(this.context, WellnessRestServer.DONT_USE_SAVED, FILENAME, REST_RESOURCE);
    }
}
