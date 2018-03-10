package edu.neu.ccs.wellness.utils;

/**
 * Created by hermansaksono on 2/13/18.
 */

public class WellnessDate {
    /* STATIC VARIABLES */
    public static final String[] DAY_OF_WEEK_STR = {"SUN", "MON", "TUE", "WED", "THUR", "FRI", "SAT"};

    public static String getDayOfWeek(int dayOfWeek) {
        if ((0 < dayOfWeek) && (dayOfWeek <= 7)) {
            return DAY_OF_WEEK_STR[dayOfWeek - 1];
        } else {
            return "";
        }
    }
}
