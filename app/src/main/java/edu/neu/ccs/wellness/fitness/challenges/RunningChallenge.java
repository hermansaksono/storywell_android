package edu.neu.ccs.wellness.fitness.challenges;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import edu.neu.ccs.wellness.fitness.interfaces.RunningChallengeInterface;

/**
 * Created by RAJ on 2/19/2018.
 */

public class RunningChallenge implements RunningChallengeInterface {

    private static final int ZERO = 0;
    private static final String EMPTY_STRING = "";
    private static final String SEVEN_DAY_DURATION = "7d";
    private static final int NO_OPTION = -1;
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'";
    private static final String DEFAULT_DATE_STRING = "2018-07-01'T'00:00:00.000000'Z'";

    private String text;
    private String subText;
    private String totalDuration;
    private Date startDate;
    private Date endDate;
    private int levelId;
    private int levelOrder;
    private List<ChallengeProgress> challengeProgress;

    private RunningChallenge(String text, String subText, String totalDuration,
                             Date startDate, Date endDate, int levelId, int levelOrder,
                             List<ChallengeProgress> challengeProgress) {
        this.text = text;
        this.subText = subText;
        this.totalDuration = totalDuration;
        this.startDate = startDate;
        this.endDate = endDate;
        this.levelId = levelId;
        this.levelOrder = levelOrder;
        this.challengeProgress = challengeProgress;
    }

    public static RunningChallenge newInstance(JSONObject jsonObject) {
        RunningChallenge runningChallenge = null;
        try {
            DateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
            JSONArray jsonArray = jsonObject.getJSONArray("progress");
            Date startDate = formatter.parse(jsonObject.optString(
                    "start_datetime", DEFAULT_DATE_STRING));
            Date endDate = formatter.parse(jsonObject.optString(
                    "end_datetime", DEFAULT_DATE_STRING));
            runningChallenge = new RunningChallenge(
                    jsonObject.optString("text", EMPTY_STRING),
                    jsonObject.optString("subtext", EMPTY_STRING),
                    jsonObject.optString("total_duration", SEVEN_DAY_DURATION),
                    startDate,
                    endDate,
                    jsonObject.optInt("level_id", ZERO),
                    jsonObject.optInt("level_order", ZERO),
                    getLisOfPersonChallenge(jsonArray));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return runningChallenge;
    }

    @Override
    public boolean isCurrentlyRunning() {
        return true;
    }

    @Override
    public String getText() {
        return this.text;
    }

    @Override
    public String getSubText() {
        return this.subText;
    }

    @Override
    public String getTotalDuration() {
        return this.totalDuration;
    }

    @Override
    public Date getStartDate() {
        return this.startDate;
    }

    @Override
    public Date getEndDate() {
        return this.endDate;
    }

    @Override
    public UnitChallenge getUnitChallenge() {
        if (this.challengeProgress.size() == 0) {
            return null;
        } else {
            ChallengeProgress challengeProgress = this.challengeProgress.get(0);
            return new UnitChallenge(NO_OPTION, EMPTY_STRING, EMPTY_STRING,
                    (float) challengeProgress.getGoal(), challengeProgress.getUnit());
        }
    }

    @Override
    public int getLevelId() {
        return this.levelId;
    }

    @Override
    public int getLevelOrder() {
        return this.levelOrder;
    }

    @Override
    public List<ChallengeProgress> getChallengeProgress() {
        return challengeProgress;
    }

    @Override
    public boolean isChallengePassed() {
        Calendar endCalendar = GregorianCalendar.getInstance(Locale.US);
        endCalendar.setTime(this.getEndDate());
        endCalendar.set(Calendar.HOUR_OF_DAY, 21);
        endCalendar.set(Calendar.MINUTE, 0);
        endCalendar.set(Calendar.SECOND, 0);

        Calendar now = GregorianCalendar.getInstance(Locale.US);

        return endCalendar.after(now);
    }

    /* STATIC HELPER METHODS */
    private static List<ChallengeProgress> getLisOfPersonChallenge(JSONArray jsonArray)
            throws JSONException {
        List<ChallengeProgress> challengeProgressList = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject eachProgress = (JSONObject) jsonArray.get(i);
            int personId = eachProgress.getInt("person_id");
            double goal = eachProgress.getDouble("goal");
            String unit = eachProgress.getString("unit");
            String duration = eachProgress.getString("unit_duration");

            challengeProgressList.add(new ChallengeProgress(personId, goal, unit, duration));
        }
        return challengeProgressList;
    }

    /*
    //TODO RK: I am using the above method as a factory method for RunningChallenge. I will delete the below
    //TODO     method in case we do not require it at all.

    // TODO HS: I'm having a difficulty understanding what this function does. It seems that it
    // TODO     takes an unsynced challenge, and take some of the values (e.g., total_duration,
    // TODO     start_datetime). However an unsynced challenge will not have those values.
    // TODO     My suggestion is to have the newInstance factory method to take a RunninChallenge json
    // TODO     (i.e., the one with total_duration, start_datetime, etc)and put the values.
    public static RunningChallenge newInstance(UnitChallenge challenge) {
        JSONObject jsonObject = null;
        RunningChallenge runningChallenge = null;
        try {
            jsonObject = new JSONObject(challenge.getJsonText());
            runningChallenge = new RunningChallenge(jsonObject);
            runningChallenge.setTotalDuration(jsonObject.getString("total_duration"));
            runningChallenge.setStartDateTime(jsonObject.getString("start_datetime"));
            runningChallenge.setEndDateTime(jsonObject.getString("end_datetime"));
            runningChallenge.setLevelId(jsonObject.getInt("level_id"));
            runningChallenge.setLevelOrder(jsonObject.getInt("level_order"));
            runningChallenge.setText(jsonObject.getString("text"));
            runningChallenge.setSubText(jsonObject.getString("subtext"));
            runningChallenge.setIsCurrentlyRunning(jsonObject.getBoolean("is_currently_running"));
            runningChallenge.setChallengeProgress(null); //it is a new Running UnitChallenge
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return runningChallenge;
    }

    public boolean getIsCurrentlyRunning() {
        return isCurrentlyRunning;
    }

    public void setIsCurrentlyRunning(boolean isCurrentlyRunning) {
        this.isCurrentlyRunning = isCurrentlyRunning;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setSubText(String subText) {
        this.subText = subText;
    }

    public void setTotalDuration(String totalDuration) {
        this.totalDuration = totalDuration;
    }

    public String getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(String startDateTime) {
        this.startDateTime = startDateTime;
    }

    public String getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(String endDateTime) {
        this.endDateTime = endDateTime;
    }

    public void setLevelId(int levelId) {
        this.levelId = levelId;
    }

    public void setLevelOrder(int levelOrder) {
        this.levelOrder = levelOrder;
    }

    public JSONObject getJsonObject() {
        return jsonObject;
    }

    public void setJsonObject(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    private static void parseRunningChallengeJSON(RunningChallenge runningChallenge) {
        JSONObject jsonObject = runningChallenge.jsonObject;
        try {
            runningChallenge.setText(jsonObject.getString("text"));
            runningChallenge.setSubText(jsonObject.getString("subtext"));
            JSONArray jsonArray = jsonObject.getJSONArray("progress");
            JSONObject eachProgress;
            ChallengeProgress challengeProgress;
            List<ChallengeProgress> challengeProgressList = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                eachProgress = (JSONObject) jsonArray.get(i);
                int personId = eachProgress.getInt("person_id");
                double goal = eachProgress.getDouble("goal");
                String unit = eachProgress.getString("unit");
                String duration = eachProgress.getString("unit_duration");
                challengeProgress = new ChallengeProgress(personId, goal, unit, duration);
                challengeProgressList.add(challengeProgress);
            }
            runningChallenge.setChallengeProgress(challengeProgressList);
            runningChallenge.setLevelId(jsonObject.getInt("level_id"));
            runningChallenge.setLevelOrder(jsonObject.getInt("level_order"));
            runningChallenge.setTotalDuration(jsonObject.getString("total_duration"));
            runningChallenge.setStartDateTime(jsonObject.getString("start_datetime"));
            runningChallenge.setEndDateTime(jsonObject.getString("end_datetime"));
            runningChallenge.setIsCurrentlyRunning(jsonObject.getBoolean("is_currently_running"));
        } catch (JSONException jsonException) {

        }
    }

    @Override
    public List<ChallengeProgress> getChallengeProgress() {
        return challengeProgress;
    }

    public void setChallengeProgress(List<ChallengeProgress> challengeProgress) {
        this.challengeProgress = challengeProgress;
    }


    @Override
    public boolean isCurrentlyRunning() {
        return false;
    }

    @Override
    public String getText() {
        return null;
    }

    @Override
    public String getSubText() {
        return null;
    }

    @Override
    public String getTotalDuration() {
        return null;
    }

    @Override
    public Date getStartDate() {
        return null;
    } // TODO HS: make this returns a Date

    @Override
    public Date getEndDate() {
        return null;
    } // TODO HS: make this returns a Date

    @Override
    public int getLevelId() {
        return 0;
    }

    @Override
    public int getLevelOrder() {
        return 0;
    }
    */
}
