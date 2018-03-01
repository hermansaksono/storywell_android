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


    public static Challenge create(JSONObject jsonObject){
        try {
            return new Challenge(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getOption() { return this.option; }

    public String getText() { return this.text; }

    public String getJsonText() {return this.jsonText; }

    public void setOption(int option) {
        this.option = option;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setJsonText(String jsonText) {
        this.jsonText = jsonText;
    }

    public void setGoal(int goal) {
        this.goal = goal;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int getGoal() { return this.goal; }

    public String getUnit() { return this.unit; }
}
