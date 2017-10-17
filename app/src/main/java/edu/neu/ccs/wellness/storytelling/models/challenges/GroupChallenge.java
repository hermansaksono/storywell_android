package edu.neu.ccs.wellness.storytelling.models.challenges;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.neu.ccs.wellness.storytelling.interfaces.GroupChallengeInterface;
import edu.neu.ccs.wellness.storytelling.interfaces.RestServer;
import edu.neu.ccs.wellness.storytelling.interfaces.StorytellingException;
import edu.neu.ccs.wellness.storytelling.models.WellnessRestServer;

/**
 * Created by hermansaksono on 10/16/17.
 */

public class GroupChallenge implements GroupChallengeInterface {
    public static final String RES_CHALLENGES = "group/challenges";
    public static final String FILENAME_CHALLENGES = "challenges";
    public static final String STRING_FORMAT = "%s - %s";


    private ChallengeStatus status = ChallengeStatus.UNINITIATED;
    private String text;
    private String subtext;
    private List<AvailableChallenge> availableChallenges = null;
    private List<PersonChallenge> personChallenges = null;

    public GroupChallenge() { }

    @Override
    public ChallengeStatus getStatus() { return this.status; }

    @Override
    public String getText() { return this.text; }

    @Override
    public String getSubtext() { return this.subtext; }

    @Override
    public String toString() {
        if (this.getStatus() != ChallengeStatus.UNINITIATED) {
            return String.format(STRING_FORMAT, this.text, this.subtext);
        } else {
            return ChallengeStatus.UNINITIATED.toString();
        }
    }

    public List<AvailableChallenge> getAvailableChallenges() {
        return this.availableChallenges;
    }

    public List<PersonChallenge> getCurrentProgress() {
        return this.personChallenges;
    }

    public RestServer.ResponseType downloadChallenges(Context context, WellnessRestServer server) {
        try {
            server.saveGetResponse(context, FILENAME_CHALLENGES, RES_CHALLENGES);
            return RestServer.ResponseType.SUCCESS_202;
        } catch (IOException e) {
            return RestServer.ResponseType.NOT_FOUND_404;
        }
    }

    public RestServer.ResponseType loadChallenges(Context context, WellnessRestServer server) {
        RestServer.ResponseType response = null;
        try {
            String jsonString = server.getSavedGetRequest(context, FILENAME_CHALLENGES, RES_CHALLENGES);
            this.processChallenges(new JSONObject(jsonString));
            response = RestServer.ResponseType.SUCCESS_202;
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            response = RestServer.ResponseType.NOT_FOUND_404;
        }
        return response;
    }

    private void processChallenges(JSONObject jsonObject) throws JSONException {
        this.text = jsonObject.getString("text");
        this.subtext =  jsonObject.getString("subtext");

        if (jsonObject.getBoolean("is_currently_running")) {
            this.processRunningChallenges(jsonObject);
        } else {
            this.processAvailableChallenges(jsonObject);
        }
    }

    private void processRunningChallenges(JSONObject jsonObject) throws JSONException {
        this.status = ChallengeStatus.RUNNING;
        this.personChallenges = getListOfPersonChallenges(jsonObject);
    }

    private void processAvailableChallenges(JSONObject jsonObject) throws JSONException {
        this.status = ChallengeStatus.AVAILABLE;
        this.availableChallenges = getListOfAvailableChallenges(jsonObject);
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
}
