package edu.neu.ccs.wellness.utils;

/**
 * Created by hermansaksono on 2/13/18.
 */

public class WellnessDate {
    /* STATIC VARIABLES */
    public static final String[] DAYS_OF_WEEK = {"SUN", "MON", "TUE", "WED", "THUR", "FRI", "SAT"};

    public static String getDayOfWeek(int dayOfWeek) {
        if ((0 < dayOfWeek) && (dayOfWeek <= 7)) {
            return DAYS_OF_WEEK[dayOfWeek - 1];
        } else {
            return "";
        }
    }
}
