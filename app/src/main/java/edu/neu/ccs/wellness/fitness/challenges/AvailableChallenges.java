package edu.neu.ccs.wellness.fitness.challenges;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.neu.ccs.wellness.fitness.interfaces.AvailableChallengesInterface;

/**
 * Created by hermansaksono on 10/16/17.
 */

public class AvailableChallenges implements AvailableChallengesInterface {
    public static final String STRING_FORMAT = "%s - %s";
    
    private String text;
    private String subtext;
    private List<UnitChallenge> challenges = null;

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
    public List<UnitChallenge> getChallenges() {
        return this.challenges;
    }

    @Override
    public Map<String, List<UnitChallenge>> getChallengesByPerson() {
        return null;
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
            for(int i = 0; i < challengeArray.length(); i++) {
                JSONObject challengeJson = challengeArray.getJSONObject(i);
                UnitChallenge challenge = UnitChallenge.newInstance(challengeJson);
                challenges.add(challenge);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
