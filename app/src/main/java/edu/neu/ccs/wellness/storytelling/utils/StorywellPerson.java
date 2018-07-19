package edu.neu.ccs.wellness.storytelling.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Calendar;
import java.util.Locale;

import edu.neu.ccs.wellness.miband2.model.MiBandProfile;
import edu.neu.ccs.wellness.miband2.model.UserInfo;
import edu.neu.ccs.wellness.people.Group;
import edu.neu.ccs.wellness.people.Person;
import edu.neu.ccs.wellness.people.PersonDoesNotExistException;
import edu.neu.ccs.wellness.storytelling.Storywell;
import edu.neu.ccs.wellness.storytelling.settings.Keys;
import edu.neu.ccs.wellness.utils.WellnessIO;

/**
 * Created by hermansaksono on 7/19/18.
 */

public class StorywellPerson {
    private Person person;
    private MiBandProfile btProfile;
    private UserInfo btUserInfo;

    public StorywellPerson(Person person, MiBandProfile miBandProfile, UserInfo btUserInfo) {
        this.person = person;
        this.btProfile = miBandProfile;
        this.btUserInfo = btUserInfo;
    }

    public static StorywellPerson newInstance(Person person, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        String address = prefs.getString(getBtPrefKeyFromRoleString(person.getRole()), "");
        MiBandProfile miBandProfile = new MiBandProfile(address);

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

    public MiBandProfile getBtProfile() { return this.btProfile; }

    public UserInfo getBtUserInfo() { return this.btUserInfo; }

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
        int gender = UserInfo.GENDER_FEMALE;
        int age = getAgeFromBirthYear(getInt(prefs, Keys.CAREGIVER_BIRTH_YEAR, "1970"));
        return new UserInfo(
                person.getId(),
                gender,
                age,
                getInt(prefs, Keys.CAREGIVER_HEIGHT, "170"),
                getInt(prefs, Keys.CAREGIVER_WEIGHT, "70"),
                person.getName(),
                type);
    }

    private static UserInfo getChildUserInfo(Person person, SharedPreferences prefs) {
        int type = 1; // No clear reason why we use 1
        int gender = UserInfo.GENDER_FEMALE;
        int age = getAgeFromBirthYear(getInt(prefs, Keys.CHILD_BIRTH_YEAR, "2000"));
        return new UserInfo(
                person.getId(),
                gender,
                age,
                getInt(prefs, Keys.CHILD_HEIGHT, "170"),
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

}
