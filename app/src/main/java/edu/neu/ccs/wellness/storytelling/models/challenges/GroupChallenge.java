package edu.neu.ccs.wellness.storytelling.models.challenges;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import edu.neu.ccs.wellness.storytelling.interfaces.GroupChallengeInterface.ChallengeStatus;
import edu.neu.ccs.wellness.storytelling.interfaces.StorytellingException;
import edu.neu.ccs.wellness.storytelling.models.WellnessRestServer;
import edu.neu.ccs.wellness.storytelling.models.WellnessUser;

/**
 * Created by hermansaksono on 10/16/17.
 */

public class GroupChallenge {
    public static final String RES_CHALLENGES = "group/challenges";
    public static final String FILENAME_CHALLENGES = "challenges";


    private ChallengeStatus status = ChallengeStatus.UNINITIATED;
    private String text;
    private String subtext;
    private List<AvailableChallenge> availableChallenges;
    private List<PersonChallenge> personChallenges;

    public GroupChallenge() {
    }

    public ChallengeStatus getStatus() { return this.status; }

    public String getText() { return this.text; }

    public String getSubtext() { return this.subtext; }

    public List<AvailableChallenge> getAvailableChallenges() throws StorytellingException {
        return this.availableChallenges;
    }

    public List<PersonChallenge> getCurrentProgress() throws StorytellingException {
        return this.personChallenges;
    }

    public void loadChallenges(Context context) {
        try {
            WellnessUser user = new WellnessUser(WellnessRestServer.DEFAULT_USER,
                    WellnessRestServer.DEFAULT_PASS);
            WellnessRestServer server = new WellnessRestServer(
                    WellnessRestServer.WELLNESS_SERVER_URL, 0,
                    WellnessRestServer.STORY_API_PATH, user);
            String jsonString = server.getSavedGetRequest(context, FILENAME_CHALLENGES, RES_CHALLENGES);
            this.processChallenges(new JSONObject(jsonString));
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
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
