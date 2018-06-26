package edu.neu.ccs.wellness.utils;

import java.util.Calendar;

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

    public static int getDayOfWeek() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

    public static Calendar getClone(Calendar cal) {
        Calendar cloned = Calendar.getInstance();
        cloned.setTime(cal.getTime());
        cloned.setTimeZone(cal.getTimeZone());

        return cloned;
    }

    public static Calendar getResetToBeginningOfDay(Calendar cal) {
        Calendar reset = Calendar.getInstance();
        reset.setTime(cal.getTime());
        reset.setTimeZone(cal.getTimeZone());
        reset.set(Calendar.HOUR, 0);
        reset.set(Calendar.MINUTE, 0);
        reset.set(Calendar.SECOND, 0);
        reset.set(Calendar.MILLISECOND, 0);

        return reset;
    }
}
