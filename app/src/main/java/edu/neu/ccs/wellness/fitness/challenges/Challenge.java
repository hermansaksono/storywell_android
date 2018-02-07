package edu.neu.ccs.wellness.fitness.challenges;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by hermansaksono on 10/16/17.
 */

public class Challenge {
    private int option;
    private String text;
    private String jsonText;
    private int goal;
    private String unit;

    public Challenge(JSONObject jsonObject) throws JSONException {
        this.option = jsonObject.getInt("option");
        this.text = jsonObject.getString("text");
        this.goal = jsonObject.getInt("goal");
        this.unit = jsonObject.getString("unit");
        this.jsonText = jsonObject.toString();
    }

    public int getOption() { return this.option; }

    public String getText() { return this.text; }

    public String getJsonText() {return this.jsonText; }

    public int getGoal() { return this.goal; }

    public String getUnit() { return this.unit; }
}
