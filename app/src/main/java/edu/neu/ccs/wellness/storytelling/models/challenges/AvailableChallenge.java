package edu.neu.ccs.wellness.storytelling.models.challenges;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by hermansaksono on 10/16/17.
 */

public class AvailableChallenge {
    private int option;
    private String text;
    private String jsonText;

    public AvailableChallenge(JSONObject jsonObject) throws JSONException {
        this.option = jsonObject.getInt("option");
        this.text = jsonObject.getString("text");
        this.jsonText = jsonObject.toString();
    }

    public int getOption() { return this.option; }

    public String getText() { return this.text; }

    @Override
    public String toString() {return this.jsonText; }
}
