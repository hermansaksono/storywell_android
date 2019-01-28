package edu.neu.ccs.wellness.utils.date;

/**
 * Created by hermansaksono on 1/28/19.
 */

public class HourMinute {
    public static final int MIN_HOUR = 0;
    public static final int MAX_HOUR = 23;
    public static final int MIN_MINUTE = 0;
    public static final int MAX_MINUTE = 59;

    private int hour = 0; // 24-hour format, range: 0 to 23
    private int minute = 0; // range: 0 to 59

    public HourMinute(int hourOfDay, int minuteOfDay) {
        this.setHour(hourOfDay);
        this.setMinute(minuteOfDay);
    }

    public HourMinute() {
        this.setHour(MIN_HOUR);
        this.setMinute(MAX_HOUR);
    }

    public int getHour() {
        return this.hour;
    }

    public void setHour(int hourOfDay) {
        if (hour < MIN_HOUR) {
            this.hour = MIN_HOUR;
        } else if (hour > MAX_HOUR) {
            this.hour = MAX_HOUR;
        } else {
            this.hour = hourOfDay;
        }
    }

    public int getMinute() {
        return this.minute;
    }

    public void setMinute(int minuteOfDay) {
        if (minute < MIN_MINUTE) {
            this.minute = MIN_MINUTE;
        } else if (minute > MAX_MINUTE) {
            this.minute = MAX_MINUTE;
        } else {
            this.minute = minuteOfDay;
        }
    }
}
