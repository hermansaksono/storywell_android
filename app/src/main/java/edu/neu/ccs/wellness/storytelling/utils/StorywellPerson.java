package edu.neu.ccs.wellness.storytelling.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import edu.neu.ccs.wellness.trackers.miband2.MiBand2Profile;
import edu.neu.ccs.wellness.trackers.UserInfo;
import edu.neu.ccs.wellness.people.Group;
import edu.neu.ccs.wellness.people.Person;
import edu.neu.ccs.wellness.people.PersonDoesNotExistException;
import edu.neu.ccs.wellness.storytelling.settings.Keys;
import edu.neu.ccs.wellness.utils.WellnessDate;

/**
 * Created by hermansaksono on 7/19/18.
 */

public class StorywellPerson {
    private Person person;
    private MiBand2Profile btProfile;
    private UserInfo btUserInfo;

    public StorywellPerson(Person person, MiBand2Profile miBandProfile, UserInfo btUserInfo) {
        this.person = person;
        this.btProfile = miBandProfile;
        this.btUserInfo = btUserInfo;
    }

    public static StorywellPerson newInstance(Person person, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        String address = prefs.getString(getBtPrefKeyFromRoleString(person.getRole()), "");
        MiBand2Profile miBandProfile = new MiBand2Profile(address);

        UserInfo userInfo = getUserInfo(prefs, person, person.getRole());

        return new StorywellPerson(person, miBandProfile, userInfo);
    }

    @Override
    public String toString() {
        return String.format(Locale.US,"%s (uid: %d, %s)",
                this.getPerson().getName(),
                this.getPerson().getId(),
                this.getBtProfile().getAddress());
    }

    public Person getPerson() { return this.person; }

    public MiBand2Profile getBtProfile() { return this.btProfile; }

    public UserInfo getBtUserInfo() { return this.btUserInfo; }

    public GregorianCalendar getLastSyncTime(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        GregorianCalendar cal = (GregorianCalendar) GregorianCalendar.getInstance();
        long time = getDefaultDate();
        if (this.person.isRole(Person.ROLE_PARENT)) {
            time = prefs.getLong(Keys.CAREGIVER_LAST_SYNC_TIME, time);
        } else if (this.person.isRole(Person.ROLE_CHILD)) {
            time = prefs.getLong(Keys.CHILD_LAST_SYNC_TIME, time);
        }
        Timestamp timestamp = new Timestamp(time);
        cal.setTime(timestamp);
        return cal;
    }

    public void setLastSyncTime(Context context, GregorianCalendar calendar) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        if (this.person.isRole(Person.ROLE_PARENT)) {
            editor.putLong(Keys.CAREGIVER_LAST_SYNC_TIME, calendar.getTimeInMillis());
        } else if (this.person.isRole(Person.ROLE_CHILD)) {
            editor.putLong(Keys.CHILD_LAST_SYNC_TIME, calendar.getTimeInMillis());
        }
        editor.commit();
    }

    public boolean isLastSyncTimeWithinInterval(int intervalMins, Context context) {
        GregorianCalendar lastSyncCal = this.getLastSyncTime(context);
        GregorianCalendar intervalCal = WellnessDate.getCalendarAfterNMinutes(
                lastSyncCal, intervalMins);
        GregorianCalendar currentCal = WellnessDate.getNow();

        return intervalCal.getTimeInMillis() > currentCal.getTimeInMillis();

    }

    public int getBatteryLevel(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        GregorianCalendar cal = (GregorianCalendar) GregorianCalendar.getInstance();
        int batteryLevel = 0;
        if (this.person.isRole(Person.ROLE_PARENT)) {
            batteryLevel = prefs.getInt(Keys.CAREGIVER_BATTERY_LEVEL, 0);
        } else if (this.person.isRole(Person.ROLE_CHILD)) {
            batteryLevel = prefs.getInt(Keys.CHILD_BATTERY_LEVEL, 0);
        }
        return batteryLevel;
    }

    public void setBatteryLevel(Context context, int percent) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        if (this.person.isRole(Person.ROLE_PARENT)) {
            editor.putLong(Keys.CAREGIVER_BATTERY_LEVEL, percent);
        } else if (this.person.isRole(Person.ROLE_CHILD)) {
            editor.putLong(Keys.CHILD_BATTERY_LEVEL, percent);
        }
        editor.commit();
    }

    /* HELPER METHODS */
    private static Person getPersonByRole(Group group, String roleString) {
        try {
            return group.getPersonByRole(roleString);
        } catch (PersonDoesNotExistException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getBtPrefKeyFromRoleString(String roleString) {
        if (Person.ROLE_PARENT.equals(roleString)) {
            return Keys.CAREGIVER_BLUETOOTH_ADDR;
        } else if (Person.ROLE_CHILD.equals(roleString)) {
            return Keys.CHILD_BLUETOOTH_ADDR;
        } else {
            return null;
        }
    }

    private static UserInfo getUserInfo(SharedPreferences prefs, Person person, String roleString) {
        if (Person.ROLE_PARENT.equals(roleString)) {
            return getCaregiverUserInfo(person, prefs);
        } else if (Person.ROLE_CHILD.equals(roleString)) {
            return getChildUserInfo(person, prefs);
        } else {
            return null;
        }
    }

    private static UserInfo getCaregiverUserInfo(Person person, SharedPreferences prefs) {
        int type = 1; // No clear reason why we use 1
        int gender = UserInfo.BIOLOGICAL_SEX_FEMALE;
        int age = getAgeFromBirthYear(prefs.getInt(Keys.CAREGIVER_BIRTH_YEAR, 1970));
        return new UserInfo(
                person.getId(),
                gender,
                age,
                (int) prefs.getFloat(Keys.CAREGIVER_HEIGHT, 170),
                getInt(prefs, Keys.CAREGIVER_WEIGHT, "70"),
                person.getName(),
                type);
    }

    private static UserInfo getChildUserInfo(Person person, SharedPreferences prefs) {
        int type = 1; // No clear reason why we use 1
        int gender = UserInfo.BIOLOGICAL_SEX_FEMALE;
        int age = getAgeFromBirthYear(prefs.getInt(Keys.CHILD_BIRTH_YEAR, 2000));
        return new UserInfo(
                person.getId(),
                gender,
                age,
                (int) prefs.getFloat(Keys.CHILD_HEIGHT, 170),
                getInt(prefs, Keys.CHILD_WEIGHT, "70"),
                person.getName(),
                type);
    }

    private static int getInt(SharedPreferences prefs, String key, String defaultVal) {
        return Integer.parseInt(prefs.getString(key, defaultVal));
    }

    private static int getAgeFromBirthYear(int birthYear) {
        Calendar cal = Calendar.getInstance();
        int thisYear = cal.get(Calendar.YEAR);
        return thisYear - birthYear;
    }

    private static long getDefaultDate() {
        GregorianCalendar calendar = (GregorianCalendar) Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getDefault());
        calendar.set(Calendar.YEAR, 2018);
        calendar.set(Calendar.MONTH, Calendar.JULY);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

}
