package edu.neu.ccs.wellness.utils;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Created by hermansaksono on 2/13/18.
 */

public class WellnessDate {
    /* STATIC VARIABLES */
    public static final String[] DAY_OF_WEEK_STR = {"SUN", "MON", "TUE", "WED", "THUR", "FRI", "SAT"};
    private static final int FIRST_DAY_OF_WEEK = Calendar.SUNDAY;

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

    public static boolean isSameDay(Calendar date) {
        Calendar today = Calendar.getInstance();
        return isSameDay(today, date);
    }

    public static boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
                && cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }

    public static int getYear() {
        GregorianCalendar calendar = (GregorianCalendar) Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getDefault());
        return calendar.get(Calendar.YEAR);
    }

    public static GregorianCalendar getNow() {
        GregorianCalendar calendar = (GregorianCalendar) Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getDefault());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }


    public static GregorianCalendar getTodayDate() {
        GregorianCalendar calendar = (GregorianCalendar) Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    public static GregorianCalendar getFirstDayOfWeek(GregorianCalendar midWeek) {
        GregorianCalendar firstDayOfWeek = (GregorianCalendar) midWeek.clone();
        while (firstDayOfWeek.get(Calendar.DAY_OF_WEEK) != FIRST_DAY_OF_WEEK)
            firstDayOfWeek.add( Calendar.DAY_OF_WEEK, -1 );
        return firstDayOfWeek;
    }

    public static GregorianCalendar getEndDate(GregorianCalendar firstDayOfWeek) {
        GregorianCalendar lastDayOfWeek = (GregorianCalendar) firstDayOfWeek.clone();
        lastDayOfWeek.add(Calendar.DAY_OF_YEAR, 7);
        return lastDayOfWeek;
    }
}
