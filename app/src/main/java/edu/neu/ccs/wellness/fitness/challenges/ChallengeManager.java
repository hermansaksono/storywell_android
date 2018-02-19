package edu.neu.ccs.wellness.fitness.challenges;

import android.content.Context;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.neu.ccs.wellness.fitness.interfaces.ChallengeManagerInterface;
import edu.neu.ccs.wellness.fitness.interfaces.ChallengeStatus;
import edu.neu.ccs.wellness.fitness.interfaces.AvailableChallengesInterface;
import edu.neu.ccs.wellness.server.RestServer;
import edu.neu.ccs.wellness.utils.WellnessIO;

/**
 * Created by hermansaksono on 2/5/18.
 */

public class ChallengeManager implements ChallengeManagerInterface {
    // STATIC VARIABLES
    public static final String RES_CHALLENGES = "group/challenges";
    public static final String FILENAME_CHALLENGEMAN = "challengeManager.json";
    public static final String FILENAME_CHALLENGELIST = "challengeList.json";

    // PRIVATE VARIABLES
    private ChallengeStatus status;
    private AvailableChallengesInterface availableChallenges = null;
    private Challenge runningChallenge = null;

    private transient RestServer server;
    private transient Context context;

    // PRIVATE CONSTRUCTORS
    private ChallengeManager(RestServer server, Context context) {
        this.server = server;
        this.context = context;
    }

    /**
     * Factory to create a @OldChallengeManager object.
     * @param server RestServer object for the @OldChallengeManager that also contains login info.
     * @param context Android application's context
     * @return The OldChallengeManager
     */
    public static ChallengeManagerInterface create(RestServer server, Context context) {
        ChallengeManager challengeManager = null;
        WellnessIO storage = new WellnessIO(context);

        if (storage.isFileExist(FILENAME_CHALLENGEMAN)) {
            Gson gson = new Gson();
            String jsonString = storage.read(FILENAME_CHALLENGEMAN);
            challengeManager = gson.fromJson(jsonString, ChallengeManager.class);
            challengeManager.server = server;
            challengeManager.context = context;
        } else {
            // TODO Create a basic ChallengeManager object
            // TODO Do a request to RES_CHALLENGES at the REST server, then populate availableChallenges
            JSONObject jsonObject = requestJsonChallenge(server, context, false);
            challengeManager = new ChallengeManager(server, context);
            challengeManager.status = getChallengeStatus(jsonObject);
            if (challengeManager.status == ChallengeStatus.AVAILABLE) {
                challengeManager.availableChallenges = AvailableChallenges.create(jsonObject);
            } else {
                //challengeManager.runningChallenge = AvailableChallenges.create(jsonObject); //TODO
           //current in the Json, populate that, add start date end date in java class (duration and all that
                //from json
                //create running challenge new class and  with those 4-5 fields from json
                //ischallenge running?
            }
            challengeManager.saveToJson();
        }

        return challengeManager;
    }

    // PUBLIC METHODS

    /**
     * Get the user's current ChallengeStatus
     * @return ChallengeStatus
     */
    @Override
    public ChallengeStatus getStatus() { return this.status; }

    /**
     * Get the a list of available challenges if the ChallengeStatus is either UNSTARTED or AVAILABLE
     * @return Currently running challenge
     */
    @Override
    public AvailableChallengesInterface getAvailableChallenges() {
        if (this.status == ChallengeStatus.UNSTARTED) {
            // TODO Do a request to RES_CHALLENGES, then populate availableChallenges
            return this.availableChallenges; // TODO;
        } else if (this.status == ChallengeStatus.AVAILABLE) {
            return this.availableChallenges;
        } else if (this.status == ChallengeStatus.UNSYNCED_RUN) {
            return null;
        } else if (this.status == ChallengeStatus.RUNNING) {
            return null;
        } else if (this.status == ChallengeStatus.COMPLETED) {
            return null;
        } else if (this.status == ChallengeStatus.ERROR_CONNECTING) {
            return null;
        } else if (this.status == ChallengeStatus.MALFORMED_JSON) {
            return null;
        } else {
            return null;
        }
    }

    /**
     * Get the user's currently running challenge if the ChallengeStatus is either UNSYNCED_RUN or SYNCED
     * @return Currently running challenge
     */
    @Override
    public Challenge getRunningChallenge() {
        if (this.status == ChallengeStatus.UNSTARTED) {
            return null;
        } else if (this.status == ChallengeStatus.AVAILABLE) {
            return null;
        } else if (this.status == ChallengeStatus.UNSYNCED_RUN) {
            return this.runningChallenge;
        } else if (this.status == ChallengeStatus.RUNNING) {
            return this.runningChallenge;
        } else if (this.status == ChallengeStatus.COMPLETED) {
            return null;
        } else if (this.status == ChallengeStatus.ERROR_CONNECTING) {
            return null;
        } else if (this.status == ChallengeStatus.MALFORMED_JSON) {
            return null;
        } else {
            return null;
        }
    }

    /**
     * Set the running challenge if the ChallengeStatus is AVAILABLE
     * @param challenge
     */
    @Override
    public void setRunningChallenge(Challenge challenge) {
        if (this.status == ChallengeStatus.AVAILABLE) {
            this.status = ChallengeStatus.UNSYNCED_RUN;
            this.availableChallenges = null;
            this.runningChallenge = challenge;
            this.saveToJson();
        } else {
            // TODO——do nothing?
        }
    }

    /**
     * Post the running challenge to the REST server if the ChallengeStatus is UNSYNCED_RUN
     * @return
     */
    @Override
    public RestServer.ResponseType syncRunningChallenge() {
        if (this.status == ChallengeStatus.UNSYNCED_RUN) {
            RestServer.ResponseType response = postChallenge(this.runningChallenge);
            this.status = ChallengeStatus.RUNNING;
            this.saveToJson();
            return response;
        } else {
            // TODO——do nothing?
            return null;
        }
    }

    @Override
    public void completeChallenge() {
        if (this.status == ChallengeStatus.UNSYNCED_RUN) {
            // TODO——do nothing?
        } else if (this.status == ChallengeStatus.RUNNING) {
            this.status = ChallengeStatus.COMPLETED;
            this.saveToJson();
        } else {
            // TODO——do nothing?
        }
    }

    @Override
    public void syncCompletedChallenge() {
        if (this.status == ChallengeStatus.COMPLETED) {
            this.status = ChallengeStatus.UNSTARTED;
            this.saveToJson();
        } else {
            // TODO——do nothing?
        }
    }

    public void setRestServer(RestServer server) { this.server = server; }

    public void setContext(Context context) { this.context = context; }

    /* PRIVATE METHODS */

    /**
     * Convert this class to JSON and save it as FILENAME_CHALLENGEMAN
     */
    private void saveToJson () {
        // TODO
    }

    private String loadFromJson () {
        // TODO
        return null;
    }

    private RestServer.ResponseType postChallenge(Challenge challenge) {
        RestServer.ResponseType response = null;
        try {
            String jsonText = challenge.getJsonText();
            server.doPostRequestFromAResource(jsonText, RES_CHALLENGES);
            response =  RestServer.ResponseType.SUCCESS_202;
        } catch (IOException e) {
            e.printStackTrace();
            response = RestServer.ResponseType.NOT_FOUND_404;
        }
        return response;
    }

    /* PUBLIC STATIC HELPER METHODS */
    private static String requestJsonString(RestServer server, Context context, boolean useSaved) {
        try {
            return server.doGetRequestFromAResource(context, FILENAME_CHALLENGELIST, RES_CHALLENGES, useSaved);
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

    public static ChallengeStatus getChallengeStatus(JSONObject jsonObject) {
        ChallengeStatus status = null;
        try {
            if (jsonObject.getBoolean("is_currently_running") == false) {
                status = ChallengeStatus.AVAILABLE;
            } else {
                status = ChallengeStatus.RUNNING;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return status;
    }

    public static List<Challenge> getListOfAvailableChallenges (JSONObject jsonObject) {
        List<Challenge> challenges = new ArrayList<Challenge>();
        JSONArray array = null;
        try {
            array = jsonObject.getJSONArray("challenges");

            for (int i = 0; i < array.length(); i++) {
                JSONObject jsonObj = array.getJSONObject(i);
                challenges.add(new Challenge(jsonObj));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return challenges;
    }

    public static List<PersonChallenge> getListOfPersonChallenges (JSONObject jsonObject)
            throws JSONException {
        List<PersonChallenge> challenges = new ArrayList<PersonChallenge>();
        JSONArray array = jsonObject.getJSONArray("progress");

        for (int i = 0; i < array.length(); i++) {
            JSONObject jsonObj = array.getJSONObject(i);
            challenges.add(new PersonChallenge(jsonObj));
        }

        return challenges;
    }
}
