package edu.neu.ccs.wellness.storytelling.settings;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Calendar;

import edu.neu.ccs.wellness.trackers.UserInfo;

/**
 * Created by hermansaksono on 3/7/19.
 */
@IgnoreExtraProperties
public class UserBioInfo {

    public static final int DEFAULT_WEIGHT_KG = 75;
    public static final float DEFAULT_HEIGHT_CM = 175;
    public static final int DEFAULT_BIRTH_YEAR = 2000;

    public UserBioInfo() {

    }

    private int type = 1;
    private int gender = UserInfo.BIOLOGICAL_SEX_FEMALE;
    private int weightKg = DEFAULT_WEIGHT_KG;
    private float heightCm = DEFAULT_HEIGHT_CM;
    private int birthYear = DEFAULT_BIRTH_YEAR;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public int getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(int weightKg) {
        this.weightKg = weightKg;
    }

    public float getHeightCm() {
        return heightCm;
    }

    public void setHeightCm(float heightCm) {
        this.heightCm = heightCm;
    }

    public int getBirthYear() {
        return birthYear;
    }

    public void setBirthYear(int birthYear) {
        this.birthYear = birthYear;
    }

    @Exclude
    public int getAge() {
        int thisYear = Calendar.getInstance().get(Calendar.YEAR);
        return thisYear - this.birthYear;
    }


}
