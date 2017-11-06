package edu.neu.ccs.wellness.fitness.challenges;

import org.json.JSONException;
import org.json.JSONObject;

import edu.neu.ccs.wellness.fitness.interfaces.GroupChallengeInterface.ChallengeUnit;

/**
 * Created by hermansaksono on 10/16/17.
 */

public class PersonChallenge {
    private int personId;
    private int goal;
    private ChallengeUnit unit;

    public PersonChallenge(JSONObject jsonObject) throws JSONException {
        this.personId = jsonObject.getInt("person_id");
        this.goal = jsonObject.getInt("goal");
        this.unit = getChallengeUnit(jsonObject.getString("unit"));
    }

    public int getPersonId() { return this.personId; }

    public int getGoal() { return this.goal; }

    private ChallengeUnit getChallengeUnit(String unitString) {
        if (unitString.equals("steps")) {
            return ChallengeUnit.STEPS;
        } else if (unitString.equals("minutes")) {
            return ChallengeUnit.MINUTES;
        } else if (unitString.equals("distance")) {
            return ChallengeUnit.DISTANCE;
        } else {
            return ChallengeUnit.UNKNOWN;
        }
    }
}
