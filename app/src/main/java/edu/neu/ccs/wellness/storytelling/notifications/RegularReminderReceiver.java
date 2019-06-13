package edu.neu.ccs.wellness.storytelling.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import edu.neu.ccs.wellness.notifications.RegularNotificationManager;
import edu.neu.ccs.wellness.storytelling.HomeActivity;
import edu.neu.ccs.wellness.storytelling.R;
import edu.neu.ccs.wellness.storytelling.SplashScreenActivity;
import edu.neu.ccs.wellness.storytelling.Storywell;
import edu.neu.ccs.wellness.storytelling.settings.SynchronizedSetting;
import edu.neu.ccs.wellness.utils.date.HourMinute;

/**
 * Created by hermansaksono on 2/5/19.
 */

public class RegularReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        sendARegularNotification(getDay(context), context);
        BatteryReminderReceiver.sendABatteryNotification(context);
        // cancelRegularReminders(context);
    }

    /**
     * Send a regular notification appropriate for the specified day.
     * @param day
     * @param context
     */
    public static void sendARegularNotification(int day, Context context) {
        RegularNotificationManager manager = new RegularNotificationManager(
                context.getString(R.string.notification_default_channel_id));
        Intent intent = getRetrievingActivityIntent(context);

        manager.generateAndShowARegularNotification(day, Constants.DEFAULT_NOTIFICATION_ICON_RESID, intent, context);
        Log.d("SWELL", "Regular reminder sent");
    }

    private static Intent getRetrievingActivityIntent(Context context) {
        Intent intent = new Intent(context, SplashScreenActivity.class);
        intent.putExtra(HomeActivity.KEY_DEFAULT_TAB, HomeActivity.TAB_ADVENTURE);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return intent;
    }

    private static int getDay(Context context) {
        Storywell storywell = new Storywell(context);
        SynchronizedSetting setting = storywell.getSynchronizedSetting();

        long appStartDate = setting.getAppStartDate();
        long currentTime = Calendar.getInstance(Locale.US).getTimeInMillis();
        long interval = (currentTime - appStartDate);

        float day = (float) interval / TimeUnit.DAYS.toMillis(1);

        return (int) Math.ceil(day);
    }

    /**
     * Check if the reminder has been scheduled.
     * @param context
     * @return
     */
    public static boolean isScheduled(Context context) {
        return (PendingIntent.getBroadcast(context, Constants.REGULAR_REMINDER_REQUEST_CODE,
                getAlarmIntent(context),
                PendingIntent.FLAG_NO_CREATE) != null);
    }

    /**
     * Schedule daily regular reminders a few hours before the challenge end time (as specified
     * in the User's configuration)
     * @param context
     */
    public static void scheduleRegularReminders(Context context) {
        Storywell storywell = new Storywell(context);

        // Determine the time for the alarm reminder
        SynchronizedSetting setting = storywell.getSynchronizedSetting();
        HourMinute hourMinute = setting.getChallengeEndTime();
        Calendar reminderCal = getReminderCalendar(hourMinute);

        // Schedule the Alarm
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, reminderCal.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, getReminderReceiverIntent(context));
        Log.d("SWELL", "Regular reminder scheduled every " + reminderCal.toString());
    }

    private static Calendar getReminderCalendar(HourMinute hourMinute) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, getReminderHour(hourMinute));
        calendar.set(Calendar.MINUTE, hourMinute.getMinute());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    private static int getReminderHour(HourMinute hourMinute) {
        return Math.max(hourMinute.getHour() + Constants.SEND_REMINDER_BEFORE, 0);
    }

    private static PendingIntent getReminderReceiverIntent(Context context) {
        return PendingIntent.getBroadcast(
                context, Constants.REGULAR_REMINDER_REQUEST_CODE, getAlarmIntent(context), 0);
    }

    private static Intent getAlarmIntent(Context context) {
        return new Intent(context, RegularReminderReceiver.class);
    }

    /**
     * Cancel regular reminders
     * @param context
     */
    public static void cancelRegularReminders(Context context) {
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmMgr.cancel(getReminderReceiverIntent(context));
        Log.d("SWELL", "Regular reminder cancelled");
    }
}
