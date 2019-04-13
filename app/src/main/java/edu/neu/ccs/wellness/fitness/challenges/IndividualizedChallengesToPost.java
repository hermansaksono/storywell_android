package edu.neu.ccs.wellness.fitness.challenges;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import edu.neu.ccs.wellness.utils.WellnessDate;

public class IndividualizedChallengesToPost {
    private static final String TZ_UTC = "UTC";

    @SerializedName("start_datetime_utc")
    private String startDatetimeUtcString;

    @SerializedName("total_duration")
    private String totalDurationString;

    @SerializedName("level_id")
    private int levelId;

    @SerializedName("challenges_by_person")
    private Map<String, UnitChallenge> challengesByPerson;

    IndividualizedChallengesToPost(
            String startDatetimeUtcString, String totalDurationString, int levelId) {
        this.startDatetimeUtcString = startDatetimeUtcString;
        this.totalDurationString = totalDurationString;
        this.levelId = levelId;
        this.challengesByPerson = new HashMap<>();
    }

    public Date getStartDateUtc() {
        TimeZone timeZone = TimeZone.getTimeZone(TZ_UTC);
        return WellnessDate.getDateFromString(this.startDatetimeUtcString, timeZone);
    }

    public void setStartDateUtc(Date startDateUtc) {
        SimpleDateFormat sdf = new SimpleDateFormat(WellnessDate.DATE_FORMAT_SHORT, Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone(TZ_UTC));
        this.startDatetimeUtcString = sdf.format(startDateUtc);
    }

    public void setChallengeToStartTomorrow(Date todayDateUtc) {
        Calendar startCalendar = Calendar.getInstance(Locale.US);
        startCalendar.setTimeZone(TimeZone.getDefault());
        startCalendar.setTimeInMillis(todayDateUtc.getTime());
        startCalendar.set(Calendar.HOUR_OF_DAY, 0);
        startCalendar.set(Calendar.MINUTE, 0);
        startCalendar.set(Calendar.SECOND, 0);
        startCalendar.set(Calendar.MILLISECOND, 0);
        startCalendar.add(Calendar.DATE, 1);
        startCalendar.setTimeZone(TimeZone.getTimeZone(TZ_UTC));
        this.setStartDateUtc(startCalendar.getTime());
    }

    public void put(int personId, UnitChallenge unitChallenge) {
        this.challengesByPerson.put(String.valueOf(personId), unitChallenge);
    }

    public boolean contains(int personId) {
        return this.challengesByPerson.containsKey(String.valueOf(personId));
    }

    public UnitChallenge remove(int personId) {
        return this.challengesByPerson.remove(String.valueOf(personId));
    }

    public UnitChallenge get(int personId) {
        return this.challengesByPerson.get(String.valueOf(personId));
    }

    public String getJsonString() {
        return new Gson().toJson(this, IndividualizedChallengesToPost.class);
    }
}
