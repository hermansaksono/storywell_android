package edu.neu.ccs.wellness.fitness.challenges;

import android.content.Context;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

import edu.neu.ccs.wellness.fitness.interfaces.RunningChallengeInterface;
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

public class UnusedChallengeManager implements ChallengeManagerInterface {
    // STATIC VARIABLES
    private static final String REST_RESOURCE = "group/challenges";
    private static final String REST_RESOURCE_STEPS_AVERAGE =
            REST_RESOURCE.concat("/steps_average/");
    private static final String REST_RESOURCE_COMPLETED =
            REST_RESOURCE.concat("/set_completed/override");
    private static final String FILENAME = "challenge_info.json";
    private static final String JSON_FIELD_STATUS = "status";
    private static final String JSON_FIELD_AVAILABLE = "available";
    private static final String JSON_FIELD_UNSYNCED_RUN = "unsynced_run";
    private static final String JSON_FIELD_RUNNING = "running";
    private static final String JSON_FIELD_PASSED = "passed";
    private static final ChallengeStatus DEFAULT_STATUS = ChallengeStatus.UNSTARTED;
    private static final String DEFAULT_STATUS_STRING = ChallengeStatus.toStringCode(DEFAULT_STATUS);

    // PRIVATE VARIABLES
    private Context context;
    private JSONObject jsonObject;
    private WellnessRepository repository;


    // PRIVATE CONSTRUCTORS
    private UnusedChallengeManager(RestServer server, boolean useSaved, Context context) {
        this.context = context.getApplicationContext();
        this.repository = new WellnessRepository(server, context);
        try {
            if (useSaved) {
                getSavedChallengeJson();
            } else {
                getFreshChallengeJson();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // STATIC FACTORY METHOD
    public static ChallengeManagerInterface getInstance(RestServer server, Context context){
        return new UnusedChallengeManager(server, true, context);
    }

    public static ChallengeManagerInterface getInstance(
            RestServer server, boolean useSaved, Context context){
        return new UnusedChallengeManager(server, useSaved, context);
    }

    /**
     * Delete the instance of the Challenge.
     * @param context
     * @param restServer
     * @return
     */
    public static boolean deleteInstance(Context context, RestServer restServer) {
        if (restServer.isFileExists(context, FILENAME)) {
            context.deleteFile(FILENAME);
            return true;
        } else {
            return false;
        }
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
        ChallengeStatus status = ChallengeStatus.fromStringCode(statusString);
        // return getPassedStatusIfChallengeHasPassed(status);
        return status;
    }

    private ChallengeStatus getPassedStatusIfChallengeHasPassed(ChallengeStatus status) {
        try {
            if (ChallengeStatus.RUNNING.equals(status)) {
                if (this.getRunningChallenge().isChallengePassed()) {
                    this.setStatus(ChallengeStatus.PASSED);
                    return ChallengeStatus.PASSED;
                } else {
                    return ChallengeStatus.RUNNING;
                }
            } else {
                return status;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return status;
        } catch (IOException e) {
            e.printStackTrace();
            return status;
        }
    }

    /**
     * Sets the user's current ChallengeStatus
     * @param status The new ChallengeStatus
     */
    private void setStatus(ChallengeStatus status) {
        try {
            this.getSavedChallengeJson().put(JSON_FIELD_STATUS,
                    ChallengeStatus.toStringCode(status));
            this.saveChallengeJson();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
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
        // this.setStatus(ChallengeStatus.AVAILABLE);
        JSONObject availableJson = new JSONObject(this.getSavedChallengeJson()
                .getString(JSON_FIELD_AVAILABLE));
        return AvailableChallenges.create(availableJson);
    }

    /**
     * Get the a list of available challenges if the ChallengeStatus is either UNSTARTED or
     * AVAILABLE. This method will ask the server to estimate challenge based on the given average.
     * @param personOneSteps
     * @param personTwoSteps
     * @return Available challenges
     * @throws IOException
     * @throws JSONException
     */
    @Override
    public AvailableChallengesInterface getAvailableChallenges(
            int personOneSteps, int personTwoSteps)
            throws IOException, JSONException {
        int steps_average = Math.round((personOneSteps + personTwoSteps) / 2);
        String restResource = REST_RESOURCE_STEPS_AVERAGE.concat(String.valueOf(steps_average));
        JSONObject jsonObject = repository.requestJson(
                this.context, false, FILENAME, restResource);
        JSONObject availableJson = new JSONObject(jsonObject.getString(JSON_FIELD_AVAILABLE));

        return AvailableChallenges.create(availableJson);
    }

    /**
     * Get the a list of available challenges if the ChallengeStatus is either UNSTARTED or
     * AVAILABLE. The challenges will be adjusted based on the given baseline map.  This method is
     * not implemented
     * @param peopleBaselineSteps
     * @return Available challenges
     * @throws IOException
     * @throws JSONException
     */
    @Override
    public AvailableChallengesInterface getAvailableChallenges(
            Map<Integer, Integer> peopleBaselineSteps)
            throws IOException, JSONException {
        return null; // Not implemented
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
    public RunningChallengeInterface getRunningChallenge() throws IOException, JSONException {

        String jsonString;

        switch (getStatus()) {
            case UNSYNCED_RUN:
                // pass
            case RUNNING:
                jsonString = this.getSavedChallengeJson().getString(JSON_FIELD_RUNNING);
                break;
            case PASSED:
                jsonString = this.getSavedChallengeJson().getString(JSON_FIELD_PASSED);
                break;
            default:
                throw new JSONException("Can't find RunningChallenge data in JSON.");
        }

        JSONObject runningChallengesJson = new JSONObject(jsonString);
        return RunningChallenge.newInstance(runningChallengesJson);
    }

    @Override
    public UnitChallengeInterface getUnsyncedOrRunningChallenge() throws Exception {
        ChallengeStatus status = this.getStatus();
        if (status == ChallengeStatus.UNSYNCED_RUN) {
            return this.getUnsyncedChallenge();
        } else if (status == ChallengeStatus.RUNNING) {
            return this.getRunningChallenge().getUnitChallenge();
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
            String runningChallengeJson = this.postRunningChallenge();
            this.saveRunningChallengeJson(runningChallengeJson);
            return ResponseType.SUCCESS_202;
        } catch (JSONException e) {
            Crashlytics.logException(e);
            e.printStackTrace();
            return ResponseType.BAD_JSON;
        } catch (IOException e) {
            Crashlytics.logException(e);
            e.printStackTrace();
            return ResponseType.NO_INTERNET;
        }
    }

    private String postRunningChallenge()
            throws IOException, JSONException {
        JSONObject jsonObject = this.getSavedChallengeJson();
        UnitChallenge unsyncedChallenge = UnitChallenge.newInstance(
                new JSONObject(jsonObject.getString(JSON_FIELD_UNSYNCED_RUN)));
        //return this.postUnitChallenge(unsyncedChallenge);
        return repository.postRequest(unsyncedChallenge.getJsonText(), REST_RESOURCE);
    }

    private void saveRunningChallengeJson(String challengeJsonString)
            throws IOException, JSONException {
        JSONObject jsonObject = this.getSavedChallengeJson();
        JSONObject jsonUnsyncedObject = new JSONObject(challengeJsonString);
        ChallengeStatus newStatus = ChallengeStatus.RUNNING;
        jsonObject.put(JSON_FIELD_STATUS,  ChallengeStatus.toStringCode(newStatus));
        jsonObject.put(JSON_FIELD_AVAILABLE, null);
        jsonObject.put(JSON_FIELD_UNSYNCED_RUN, null);
        jsonObject.put(JSON_FIELD_RUNNING, jsonUnsyncedObject.getString("running"));
        this.saveChallengeJson();
    }

    /**
     * Post the unit challenge and update the local data.
     */
    public RestServer.ResponseType postUnitChallenge(UnitChallengeInterface unitChallenge) {
        try {
            String runningChallengeJson = repository.postRequest(
                    unitChallenge.getJsonText(), REST_RESOURCE);
            this.saveRunningChallengeJson(runningChallengeJson);
            return ResponseType.SUCCESS_202;
        } catch (JSONException e) {
            Crashlytics.logException(e);
            e.printStackTrace();
            return ResponseType.BAD_JSON;
        } catch (IOException e) {
            Crashlytics.logException(e);
            e.printStackTrace();
            return ResponseType.NO_INTERNET;
        }
    }

    /**
     * Post the individualised challenge and update the local data. This method is not implemented.
     * @param challenge
     * @return
     */
    @Override
    public ResponseType postIndividualizedChallenge(IndividualizedChallengesToPost challenge) {
        return null; // This method is not implemented
    }

    /**
     * Sets the challenge as CLOSED. However, the challenge needs to be synced with the server.
     * INVARIANT: UnitChallenge status is PASSED.
     */
    @Override
    public void closeChallenge() throws IOException, JSONException {
        ChallengeStatus newStatus = ChallengeStatus.CLOSED;

        JSONObject jsonObject = this.getSavedChallengeJson();
        jsonObject.put(JSON_FIELD_STATUS,  ChallengeStatus.toStringCode(newStatus));
        // jsonObject.put(JSON_FIELD_AVAILABLE, null);
        // jsonObject.put(JSON_FIELD_UNSYNCED_RUN, null);
        // jsonObject.put(JSON_FIELD_RUNNING, null);
        this.saveChallengeJson();
        //this.doSetChallengeClosed();
    }

    /**
     * Synchronize a CLOSED challenge to the RestServer. This will get a new set of challenges.
     * INVARIANT: There is an internet connection.
     */
    @Override
    public void syncCompletedChallenge() throws IOException, JSONException {
        // this.jsonObject = repository.requestJson(this.context, false, FILENAME, REST_RESOURCE);
        try {
            this.repository.getRequest(REST_RESOURCE_COMPLETED);
        } catch (IOException e) {
            Crashlytics.logException(e);
            e.printStackTrace();
            throw e;
        }

        this.doRefreshJson();
    }

    @Override
    public boolean isChallengeInfoStored() {
        return this.repository.isSavedExist(context, FILENAME);
    }

    /* PRIVATE METHODS */
    private void doRefreshJson() throws IOException, JSONException {
        this.jsonObject = repository.requestJson(context, false, FILENAME, REST_RESOURCE);
    }

    private JSONObject getSavedChallengeJson() throws IOException, JSONException {
        if (this.jsonObject == null) {
            this.jsonObject = repository.requestJson(this.context, true, FILENAME, REST_RESOURCE);
        }
        return this.jsonObject;
    }

    private JSONObject getFreshChallengeJson() throws IOException, JSONException {
        if (this.jsonObject == null) {
            this.jsonObject = repository.requestJson(this.context, false, FILENAME, REST_RESOURCE);
        }
        return this.jsonObject;
    }

    private void saveChallengeJson() throws IOException, JSONException {
        String jsonString = this.getSavedChallengeJson().toString();
        repository.writeFileToStorage(this.context, jsonString, FILENAME);
        Log.d("SWELL", "Challenge JSON has been updated: " + jsonString);
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

    /**
     * Do a GET request to the (@link RestServer) to get the most up-to-date challenge data. Then
     * store the GET response into phone's local storage.
     */
    public void fetchChallengeDataFromRestServer() throws IOException, JSONException {
        this.jsonObject = repository.requestJson(this.context, WellnessRestServer.DONT_USE_SAVED, FILENAME, REST_RESOURCE);
    }
}
