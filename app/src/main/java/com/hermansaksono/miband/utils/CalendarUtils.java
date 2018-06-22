package com.hermansaksono.miband.utils;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by hermansaksono on 6/21/18.
 */

public class CalendarUtils {

    public static Date bytesToDate(byte[] data) {
        return bytesToCalendar(data).getTime();
    }

    public static Calendar bytesToCalendar(byte[] data) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, TypeConversionUtils.unsignedShortToInt(data[0])
                + (TypeConversionUtils.unsignedShortToInt(data[1]) * 256));
        calendar.set(Calendar.MONTH, TypeConversionUtils.unsignedShortToInt(data[2]) - 1);
        calendar.set(Calendar.DAY_OF_MONTH, TypeConversionUtils.unsignedShortToInt(data[3]));
        calendar.set(Calendar.HOUR_OF_DAY, TypeConversionUtils.unsignedShortToInt(data[4]));
        calendar.set(Calendar.MINUTE, TypeConversionUtils.unsignedShortToInt(data[5]));
        calendar.set(Calendar.SECOND, TypeConversionUtils.unsignedShortToInt(data[6]));
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }
}
