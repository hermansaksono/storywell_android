package edu.neu.ccs.wellness.fitness.challenges;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import edu.neu.ccs.wellness.fitness.interfaces.ChallengeStatus;
import edu.neu.ccs.wellness.fitness.interfaces.AvailableChallengesInterface;

/**
 * Created by hermansaksono on 10/16/17.
 */

public class AvailableChallenges implements AvailableChallengesInterface {
    public static final String STRING_FORMAT = "%s - %s";
    
    private String text;
    private String subtext;
    private List<Challenge> challenges = null;

    private AvailableChallenges() { }

    public static AvailableChallenges create(String jsonString) throws JSONException {
        AvailableChallenges groupChallenge = new AvailableChallenges();
        groupChallenge.processChallengesFromJsonString(jsonString);
        return groupChallenge;
    }

    public static AvailableChallenges create(JSONObject jsonObject) {
        AvailableChallenges groupChallenge = new AvailableChallenges();
        groupChallenge.processChallengesFromJsonObject(jsonObject);
        return groupChallenge;
    }

    @Override
    public String getText() { return this.text; }

    @Override
    public String getSubtext() { return this.subtext; }

    @Override
    public String toString() {
        return String.format(STRING_FORMAT, this.text, this.subtext);
    }

    @Override
    public List<Challenge> getChallenges() {
        return this.challenges;
    }

    private void processChallengesFromJsonString(String jsonString) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonString);
        processChallengesFromJsonObject(jsonObject);
    }

    private void processChallengesFromJsonObject(JSONObject jsonObject) {
        try {
            this.text = jsonObject.getString("text");
            this.subtext =  jsonObject.getString("subtext");
            this.challenges = new ArrayList<>();
            JSONArray challengeArray = jsonObject.getJSONArray("challenges");
            Challenge challenge = null;
            for(int i = 0; i < challengeArray.length(); i++) {
                challenge = new Challenge(challengeArray.getJSONObject(i));
                challenges.add(challenge);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
