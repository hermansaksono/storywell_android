package edu.neu.ccs.wellness.fitness.challenges;

import org.json.JSONException;
import org.json.JSONObject;

import edu.neu.ccs.wellness.fitness.interfaces.ChallengeUnit;

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
        this.unit = ChallengeUnit.getChallengeUnit(jsonObject.getString("unit"));
    }

    public int getPersonId() { return this.personId; }

    public int getGoal() { return this.goal; }
}
