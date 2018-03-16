package edu.neu.ccs.wellness.fitness.challenges;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import edu.neu.ccs.wellness.fitness.interfaces.RunningChallengeInterface;

/**
 * Created by RAJ on 2/19/2018.
 */

public class RunningChallenge extends Challenge implements RunningChallengeInterface {

    boolean isCurrentlyRunning;
    String text;
    String subText;
    String totalDuration;
    String startDateTime;
    String endDateTime;
    int levelId;
    int levelOrder;
    JSONObject jsonObject;
    List<ChallengeProgress> challengeProgress;

    RunningChallenge(JSONObject jsonObject) throws JSONException {
        super(jsonObject);
        this.jsonObject = jsonObject;
    }

    public static RunningChallenge create(JSONObject jsonObject){
        RunningChallenge runningChallenge = null;
        try {
            runningChallenge = new RunningChallenge(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        parseRunningChallengeJSON(runningChallenge);
        return runningChallenge;
    }

    public static RunningChallenge create(Challenge challenge){
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
            runningChallenge.setChallengeProgress(null); //it is a new Running Challenge
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

    private static void parseRunningChallengeJSON(RunningChallenge runningChallenge){
        JSONObject jsonObject = runningChallenge.jsonObject;
        try {
            runningChallenge.setText(jsonObject.getString("text"));
            runningChallenge.setSubText(jsonObject.getString("subtext"));
            JSONArray jsonArray = jsonObject.getJSONArray("progress");
            JSONObject eachProgress;
            ChallengeProgress challengeProgress;
            List<ChallengeProgress> challengeProgressList = new ArrayList<>();
            for(int i = 0; i< jsonArray.length(); i++){
                eachProgress = (JSONObject) jsonArray.get(i);
                int personId =  eachProgress.getInt("person_id");
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
        }catch (JSONException jsonException){

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
    public String getStartDate() {
        return null;
    }

    @Override
    public String getEndDate() {
        return null;
    }

    @Override
    public int getLevelId() {
        return 0;
    }

    @Override
    public int getLevelOrder() {
        return 0;
    }
}
