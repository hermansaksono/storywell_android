package edu.neu.ccs.wellness.utils;

/**
 * Created by hermansaksono on 7/23/18.
 */

public class WellnessUnit {
    public static final float LBS_TO_KGS = 0.453592f;

    public static float getKgsFromLbs(int lbs) {
        return lbs * LBS_TO_KGS;
    }
}
