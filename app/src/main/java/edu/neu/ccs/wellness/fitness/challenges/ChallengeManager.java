package edu.neu.ccs.wellness.fitness.challenges;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import edu.neu.ccs.wellness.fitness.interfaces.GroupChallengeInterface;
import edu.neu.ccs.wellness.server.RestServer;
import edu.neu.ccs.wellness.server.RestServer.ResponseType;
import edu.neu.ccs.wellness.utils.WellnessIO;

/**
 * Created by hermansaksono on 1/23/18.
 */

public class ChallengeManager {

    public static final String RES_CHALLENGES = "group/challenges";
    public static final String FILENAME_CHALLENGES = "challenges.json";
    private static final String SHAREDPREF_NAME = "challenge_status";
    private RestServer server;

    // PRIVATE CONSTRUCTORS
    private ChallengeManager(RestServer server) {
        this.server = server;
    }

    // STATIC FACTORY METHODS
    /**
     * Factory to create a @ChallengeManager object.
     * @param server RestServer object for the @ChallengeManager that also contains login info.
     * @return The @ChallengeManager object
     */
    public static ChallengeManager create(RestServer server) {
        return new ChallengeManager(server);
    }

    // PUBLIC METHODS
    /**
     * Get the status of the Challenge
     * @param context The Android application's context
     * @return If the challenge has been downloaded, return the status.
     * Otherwise return UNINITIALIZED.
     */
    public ChallengeStatus getStatus (Context context) { return getSavedChallengeStatus(context); }

    /**
     * Download the Challenge from the RestServer and override the saved Challenge file.
     * @param context The Android application's context
     */
    public void download(Context context) {
        requestJsonChallenge(context, false);
    }

    /**
     * If there is a saved Challenge file, then load the file. Otherwise, request from RestServer.
     * @param context The Android application's context
     */
    public void loadSaved (Context context) {
        requestJsonChallenge(context, true);
    }

    /***
     * Get the object that stores the challenge information. If there is a saved file load the file.
     * Otherwise connect to the server.
     * @param context The Android application's context
     * @return A GroupChallengeInterface object that stores Challenge information.
     */
    public GroupChallengeInterface getGroupChallenge (Context context) {
        try {
            String jsonString = requestJsonString(context, true);
            GroupChallenge challenges = GroupChallenge.createFromString(jsonString);
            return challenges;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Post a challenge to the server.
     * @param challenge The challenge that will be posted.
     * @param context The Android application's context.
     * @return ResponseType.SUCCESS_202 if the post is successful.
     * Otherwise returns ResponseType.NOT_FOUND_404.
     */
    public ResponseType postAvailableChallenge(AvailableChallenge challenge,
                                                          Context context) {
        ResponseType response = null;
        try {
            server.doPostRequestFromAResource(challenge.getJsonText(), RES_CHALLENGES);
            saveChallengeStatus(ChallengeStatus.RUNNING, context);
            response =  ResponseType.SUCCESS_202;
        } catch (IOException e) {
            e.printStackTrace();
            response = ResponseType.NOT_FOUND_404;
        }
        return response;
    }

    // PRIVATE METHODS
    private String requestJsonString(Context context, boolean useSaved) {
        try {
            return this.server.doGetRequestFromAResource(context, FILENAME_CHALLENGES, RES_CHALLENGES, useSaved);
        } catch (IOException e) {
            saveChallengeStatus(ChallengeStatus.ERROR_CONNECTING, context);
            return null;
        }
    }

    private JSONObject requestJsonChallenge(Context context, boolean useSaved) {
        try {
            String jsonString = requestJsonString(context, useSaved);
            JSONObject jsonObject = new JSONObject(jsonString);
            saveChallengeStatus(getChallengeStatus(jsonObject), context);
            return jsonObject;
        } catch (JSONException e) {
            saveChallengeStatus(ChallengeStatus.MALFORMED_JSON, context);
            return null;
        }
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

    private static ChallengeStatus getSavedChallengeStatus(Context context) {
        SharedPreferences sharedPref = WellnessIO.getSharedPref(context);
        String stringCode = sharedPref.getString(SHAREDPREF_NAME,
                ChallengeStatus.toStringCode(ChallengeStatus.UNINITIALIZED));
        return ChallengeStatus.fromStringCode(stringCode);
    }

    private static void saveChallengeStatus(ChallengeStatus status, Context context) {
        String stringCode = ChallengeStatus.toStringCode(status);
        SharedPreferences sharedPref = WellnessIO.getSharedPref(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(SHAREDPREF_NAME, stringCode);
        editor.commit();
    }

}
