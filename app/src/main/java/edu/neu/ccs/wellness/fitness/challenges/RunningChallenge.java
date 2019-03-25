package edu.neu.ccs.wellness.fitness.challenges;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import edu.neu.ccs.wellness.fitness.interfaces.RunningChallengeInterface;
import edu.neu.ccs.wellness.utils.WellnessDate;

/**
 * Created by RAJ on 2/19/2018.
 */

public class RunningChallenge implements RunningChallengeInterface {

    private static final int ZERO = 0;
    private static final String EMPTY_STRING = "";
    private static final String SEVEN_DAY_DURATION = "7d";
    private static final int NO_OPTION = -1;
    private static final String DEFAULT_DATE_STRING = "2018-07-01'T'00:00:00.000000'Z'";
    private static final int CHALLENGE_END_HOUR = 21;

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
        TimeZone timeZone = TimeZone.getTimeZone("GMT");
        RunningChallenge runningChallenge = null;
        try {
            JSONArray jsonArray = jsonObject.getJSONArray("progress");
            String startDateString = jsonObject.optString("start_datetime", DEFAULT_DATE_STRING);
            String endDateString = jsonObject.optString("end_datetime", DEFAULT_DATE_STRING);
            Date startDate = WellnessDate.getDateFromString(startDateString, timeZone);
            Date endDate = WellnessDate.getDateFromString(endDateString, timeZone);
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
                    (float) challengeProgress.getGoal(), this.getStartDate(),
                    challengeProgress.getUnit());
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
        Date now = GregorianCalendar.getInstance(Locale.US).getTime();
        return now.after(this.getEndDate());
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
}
