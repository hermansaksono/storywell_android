package edu.neu.ccs.wellness.storytelling.utils;

import android.content.Context;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import edu.neu.ccs.wellness.people.Person;
import edu.neu.ccs.wellness.storytelling.Storywell;
import edu.neu.ccs.wellness.storytelling.settings.SynchronizedSetting;
import edu.neu.ccs.wellness.storytelling.settings.SynchronizedSetting.FitnessSyncInfo;
import edu.neu.ccs.wellness.storytelling.settings.SynchronizedSettingRepository;
import edu.neu.ccs.wellness.storytelling.settings.UserBioInfo;
import edu.neu.ccs.wellness.trackers.UserInfo;
import edu.neu.ccs.wellness.trackers.miband2.MiBand2Profile;
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

    /**
     * Factory method to get an instance of a {@link StorywellPerson}
     * @param person
     * @param context
     * @return
     */
    public static StorywellPerson newInstance(Person person, Context context) {
        SynchronizedSetting setting = new Storywell(context).getSynchronizedSetting();
        /*
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        String address = prefs.getString(getBtPrefKeyFromRoleString(person.getRole()), "");
        MiBand2Profile miBandProfile = new MiBand2Profile(address);

        UserInfo userInfo = getUserInfo(prefs, person, person.getRole());
        */
        String address = getBtAddressFromRoleString(person, setting.getFitnessSyncInfo());
        MiBand2Profile miBandProfile = new MiBand2Profile(address);
        UserInfo userInfo = getUserinfo(person, setting.getFitnessSyncInfo());

        return new StorywellPerson(person, miBandProfile, userInfo);
    }

    private static String getBtAddressFromRoleString(Person person, FitnessSyncInfo syncInfo) {
        switch (person.getRole()) {
            case Person.ROLE_PARENT:
                return syncInfo.getCaregiverDeviceInfo().getBtAddress();
            case Person.ROLE_CHILD:
                return syncInfo.getChildDeviceInfo().getBtAddress();
            default:
                return syncInfo.getCaregiverDeviceInfo().getBtAddress();
        }
    }

    private static UserInfo getUserinfo(Person person, FitnessSyncInfo fitnessSyncInfo) {
        UserBioInfo userBioInfo;
        switch (person.getRole()) {
            case Person.ROLE_PARENT:
                userBioInfo = fitnessSyncInfo.getCaregiverBio();
                break;
            case Person.ROLE_CHILD:
                userBioInfo = fitnessSyncInfo.getChildBio();
                break;
            default:
                userBioInfo = fitnessSyncInfo.getCaregiverBio();
                break;
        }
        return getUserInfo(person, userBioInfo);
    }

    private static UserInfo getUserInfo(Person person, UserBioInfo bioInfo) {
        int type = bioInfo.getType();
        int gender = bioInfo.getGender();
        int age = bioInfo.getAge();
        return new UserInfo(
                person.getId(),
                gender,
                age,
                (int) bioInfo.getHeightCm(),
                bioInfo.getWeightKg(),
                person.getName(),
                type);
    }

    /**
     * Returns the string representation of this {@link StorywellPerson}
     * @return
     */
    @Override
    public String toString() {
        return String.format(Locale.US,"%s (uid: %d, %s)",
                this.getPerson().getName(),
                this.getPerson().getId(),
                this.getBtProfile().getAddress());
    }

    /**
     * Returns the {@link Person} being represented by this object.
     * @return
     */
    public Person getPerson() { return this.person; }

    /**
     * Returns the {@link MiBand2Profile} of this person.
     * @return
     */
    public MiBand2Profile getBtProfile() { return this.btProfile; }

    /**
     * Returns the {@link UserInfo} of this person. The {@link UserInfo} will be used for MiBand
     * synchronization.
     * @return
     */
    public UserInfo getBtUserInfo() { return this.btUserInfo; }

    /**
     * Returns the {@link Calendar} that shows when the {@link StorywellPerson} last synchronized
     * their band.
     * @param context
     * @return
     */
    public Calendar getLastSyncTime(Context context) {
        /*
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
        */
        SynchronizedSetting setting = new Storywell(context).getSynchronizedSetting();
        long timestamp;

        switch (this.person.getRole()) {
            case Person.ROLE_PARENT:
                timestamp = setting.getFitnessSyncInfo().getCaregiverDeviceInfo().getLastSyncTime();
                break;
            case Person.ROLE_CHILD:
                timestamp = setting.getFitnessSyncInfo().getChildDeviceInfo().getLastSyncTime();
                break;
            default:
                timestamp = setting.getFitnessSyncInfo().getCaregiverDeviceInfo().getLastSyncTime();
                break;
        }

        Calendar cal = GregorianCalendar.getInstance();
        cal.setTimeInMillis(timestamp);
        return cal;
    }

    /**
     * Sets the {@link Calendar} that tells the last time the {@link StorywellPerson} synchronized
     * their band.
     * @param context
     * @param calendar
     */
    public void setLastSyncTime(Context context, GregorianCalendar calendar) {
        /*
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        if (this.person.isRole(Person.ROLE_PARENT)) {
            editor.putLong(Keys.CAREGIVER_LAST_SYNC_TIME, calendar.getTimeInMillis());
        } else if (this.person.isRole(Person.ROLE_CHILD)) {
            editor.putLong(Keys.CHILD_LAST_SYNC_TIME, calendar.getTimeInMillis());
        }
        editor.commit();
        */

        SynchronizedSetting setting = new Storywell(context).getSynchronizedSetting();
        long timestamp = calendar.getTimeInMillis();

        switch (this.person.getRole()) {
            case Person.ROLE_PARENT:
                setting.getFitnessSyncInfo().getCaregiverDeviceInfo().setLastSyncTime(timestamp);
                break;
            case Person.ROLE_CHILD:
                setting.getFitnessSyncInfo().getChildDeviceInfo().setLastSyncTime(timestamp);
                break;
            default:
                setting.getFitnessSyncInfo().getCaregiverDeviceInfo().setLastSyncTime(timestamp);
                break;
        }

        SynchronizedSettingRepository.saveLocalAndRemoteInstance(setting, context);
    }

    /**
     * Returns true if the band was syncronized within the last {@param intervalMins}. Otherwise
     * return false.
     * @param intervalMins
     * @param context
     * @return
     */
    public boolean isLastSyncTimeWithinInterval(int intervalMins, Context context) {
        Calendar lastSyncCal = this.getLastSyncTime(context);
        Calendar intervalCal = WellnessDate.getCalendarAfterNMinutes(lastSyncCal, intervalMins);
        Calendar currentCal = WellnessDate.getNow();
        return intervalCal.getTimeInMillis() > currentCal.getTimeInMillis();

    }

    /**
     * Get the battery level of the MiBand device associated with this {@link StorywellPerson}.
     * @param context
     * @return
     */
    public int getBatteryLevel(Context context) {
        /*
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        GregorianCalendar cal = (GregorianCalendar) GregorianCalendar.getInstance();
        int batteryLevel = 0;
        if (this.person.isRole(Person.ROLE_PARENT)) {
            batteryLevel = prefs.getInt(Keys.CAREGIVER_BATTERY_LEVEL, 0);
        } else if (this.person.isRole(Person.ROLE_CHILD)) {
            batteryLevel = prefs.getInt(Keys.CHILD_BATTERY_LEVEL, 0);
        }
        return batteryLevel;
        */

        SynchronizedSetting setting = new Storywell(context).getSynchronizedSetting();

        switch (this.person.getRole()) {
            case Person.ROLE_PARENT:
                return setting.getFitnessSyncInfo().getCaregiverDeviceInfo().getBtBatteryLevel();
            case Person.ROLE_CHILD:
                return setting.getFitnessSyncInfo().getChildDeviceInfo().getBtBatteryLevel();
            default:
                return setting.getFitnessSyncInfo().getCaregiverDeviceInfo().getBtBatteryLevel();
        }
    }

    /**
     * Set the battery level of the MiBand device associated with this {@link StorywellPerson}.
     * @param context
     * @return
     */
    public void setBatteryLevel(Context context, int percent) {
        /*
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        if (this.person.isRole(Person.ROLE_PARENT)) {
            editor.putLong(Keys.CAREGIVER_BATTERY_LEVEL, percent);
        } else if (this.person.isRole(Person.ROLE_CHILD)) {
            editor.putLong(Keys.CHILD_BATTERY_LEVEL, percent);
        }
        editor.commit();
        */
        SynchronizedSetting setting = new Storywell(context).getSynchronizedSetting();

        switch (this.person.getRole()) {
            case Person.ROLE_PARENT:
                setting.getFitnessSyncInfo().getCaregiverDeviceInfo().setBtBatteryLevel(percent);
                break;
            case Person.ROLE_CHILD:
                setting.getFitnessSyncInfo().getChildDeviceInfo().setBtBatteryLevel(percent);
                break;
            default:
                setting.getFitnessSyncInfo().getCaregiverDeviceInfo().setBtBatteryLevel(percent);
                break;
        }

        SynchronizedSettingRepository.saveLocalAndRemoteInstance(setting, context);
    }

    /* HELPER METHODS */
    /*
    private static int getAgeFromBirthYear(int birthYear) {
        int thisYear = Calendar.getInstance().get(Calendar.YEAR);
        return thisYear - birthYear;
    }

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
*/

}
