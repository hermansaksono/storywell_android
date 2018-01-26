package edu.neu.ccs.wellness.fitness.challenges;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.neu.ccs.wellness.server.RestServer;
import edu.neu.ccs.wellness.utils.WellnessIO;

/**
 * Created by hermansaksono on 1/23/18.
 */

public class ChallengeManager {

    public static final String RES_CHALLENGES = "group/challenges";
    public static final String FILENAME_CHALLENGES = "challenges.json";
    private static final String SHAREDPREF_NAME = "challenge_status";

    private ChallengeStatus status = ChallengeStatus.UNINITIALIZED;
    private RestServer server;

    // PRIVATE CONSTRUCTORS
    private ChallengeManager(RestServer server) {
        this.server = server;
    }

    // STATIC FACTORY METHODS
    public static ChallengeManager create(RestServer server) {
        return new ChallengeManager(server);
    }

    // PUBLIC METHODS
    public ChallengeStatus getStatus (Context context) {
        if (this.status == ChallengeStatus.UNINITIALIZED) {
            return this.status;
        } else {
            return getSavedChallengeStatus(context);
        }
    }

    public void download(Context context) {
        JSONObject challengeJson = requestJsonChallenge(context, false);
        this.status = getChallengeStatus(challengeJson);
        setChallengeStatus(this.status, context);
        // TODO save the state in SharedPreferences
    }

    public void loadSaved (Context context) {
        JSONObject challengeJson = requestJsonChallenge(context, true);
        this.status = getChallengeStatus(challengeJson);
    }

    public List<AvailableChallenge> getAvailable (Context context) {
        List<AvailableChallenge> challenges = null;
        JSONObject challengeJson = requestJsonChallenge(context, true);
        try {
            challenges = getListOfAvailableChallenges(challengeJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return challenges;
    }

    public List<PersonChallenge> getRunning (Context context) {
        List<PersonChallenge> challenges = null;
        JSONObject challengeJson = requestJsonChallenge(context, true);
        try {
            return getListOfPersonChallenges(challengeJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return challenges;
    }

    // PRIVATE METHODS
    private JSONObject requestJsonChallenge(Context context, boolean useSaved) {
        JSONObject jsonObject = null;
        try {
            String jsonString = this.server.doGetRequestFromAResource(context, FILENAME_CHALLENGES, RES_CHALLENGES, useSaved);
            jsonObject = new JSONObject(jsonString);
        }
        catch (JSONException e) {
            this.status = ChallengeStatus.MALFORMED_JSON;
        } catch (IOException e) {
            this.status = ChallengeStatus.ERROR_CONNECTING;
        }
        return jsonObject;
    }

    private static ChallengeStatus getChallengeStatus (JSONObject jsonObject) {
        try {
            if (jsonObject.getBoolean("is_currently_running") == false) {
                return ChallengeStatus.AVAILABLE;
            } else {
                return ChallengeStatus.RUNNING;
            }
        } catch (JSONException e) {
            return ChallengeStatus.MALFORMED_JSON;
        }
    }

    private static List<AvailableChallenge> getListOfAvailableChallenges (JSONObject jsonObject)
            throws JSONException {
        List<AvailableChallenge> challenges = new ArrayList<AvailableChallenge>();
        JSONArray array = jsonObject.getJSONArray("challenges");

        for (int i = 0; i < array.length(); i++) {
            JSONObject jsonObj = array.getJSONObject(i);
            challenges.add(new AvailableChallenge(jsonObj));
        }

        return challenges;
    }

    private static List<PersonChallenge> getListOfPersonChallenges (JSONObject jsonObject)
            throws JSONException {
        List<PersonChallenge> challenges = new ArrayList<PersonChallenge>();
        JSONArray array = jsonObject.getJSONArray("progress");

        for (int i = 0; i < array.length(); i++) {
            JSONObject jsonObj = array.getJSONObject(i);
            challenges.add(new PersonChallenge(jsonObj));
        }

        return challenges;
    }

    private static ChallengeStatus getSavedChallengeStatus (Context context) {
        SharedPreferences sharedPref = WellnessIO.getSharedPref(context);
        String stringCode = sharedPref.getString(SHAREDPREF_NAME,
                ChallengeStatus.toStringCode(ChallengeStatus.UNINITIALIZED));
        return ChallengeStatus.fromStringCode(stringCode);
    }

    private static void setChallengeStatus (ChallengeStatus status, Context context) {
        String stringCode = ChallengeStatus.toStringCode(status);
        SharedPreferences sharedPref = WellnessIO.getSharedPref(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(SHAREDPREF_NAME, stringCode);
        editor.commit();
    }

}
