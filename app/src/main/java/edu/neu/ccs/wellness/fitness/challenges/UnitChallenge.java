package edu.neu.ccs.wellness.fitness.challenges;

import com.google.firebase.database.Exclude;
import com.google.gson.annotations.SerializedName;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import edu.neu.ccs.wellness.fitness.interfaces.UnitChallengeInterface;
import edu.neu.ccs.wellness.utils.WellnessDate;

/**
 * Created by hermansaksono on 10/16/17.
 */

public class UnitChallenge implements UnitChallengeInterface {
    private int option;
    private float goal;

    private Date startDate;
    private String text;
    private String unit;


    @SerializedName("unit_duration")
    private String unitDuration;

    @SerializedName("total_duration")
    private String totalDuration;

    @SerializedName("level_id")
    private int levelId;

    @Exclude
    private String jsonText;

    private UnitChallenge() {

    }

    public UnitChallenge(int option, String text, String jsonText, float goal,
                         Date startDate, String unit) {
        this.option = option;
        this.text = text;
        this.goal = goal;
        this.startDate = startDate;
        this.unit = unit;
        this.jsonText = jsonText;
    }

    public static UnitChallenge newInstance(JSONObject jsonObject){
        TimeZone timeZone = TimeZone.getTimeZone("GMT");
        UnitChallenge unitChallenge = null;
        try {
            unitChallenge = new UnitChallenge(jsonObject.getInt("option"),
                    jsonObject.getString("text"),
                    jsonObject.toString(),
                    (float) jsonObject.getDouble("goal"),
                    WellnessDate.getDateFromString(
                            jsonObject.getString("start_datetime_utc"), timeZone),
                    jsonObject.getString("unit"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return unitChallenge;
    }

    public int getOption() { return this.option; }

    @Override
    public String getText() { return this.text; }

    @Override
    public float getGoal() { return this.goal; }

    @Override
    public String getUnit() { return this.unit; }

    @Override
    public String getJsonText() {
        SimpleDateFormat sdf = new SimpleDateFormat(WellnessDate.DATE_FORMAT_SHORT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String startDateUTC = sdf.format(this.startDate);
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(this.jsonText);
            jsonObject.put("start_datetime_utc", startDateUTC);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    @Override
    public Date getStartDate() {
        return this.startDate;
    }

    @Override
    public void setStartDate(Date date) {
        this.startDate = date;
    }

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
}
