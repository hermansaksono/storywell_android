package edu.neu.ccs.wellness.storytelling.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import edu.neu.ccs.wellness.notifications.RegularNotificationManager;
import edu.neu.ccs.wellness.storytelling.HomeActivity;
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
    }

    /**
     * Send a regular notification appropriate for the specified day.
     * @param day
     * @param context
     */
    public static void sendARegularNotification(int day, Context context) {
        RegularNotificationManager manager = new RegularNotificationManager(Constants.CHANNEL_ID);
        Intent intent = getRetrievingActivityIntent(context);

        manager.generateAndShowARegularNotification(day, Constants.DEFAULT_NOTIFICATION_ICON_RESID, intent, context);

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

        return (int) (interval / TimeUnit.DAYS.toMillis(1));
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
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, getHour(hourMinute));
        calendar.set(Calendar.MINUTE, hourMinute.getMinute());

        // Determine the intent for the Alarm
        Intent intent = new Intent(context, RegularReminderReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        // Schedule the Alarm
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, alarmIntent);
    }

    private static int getHour(HourMinute hourMinute) {
        return Math.max(hourMinute.getHour() + Constants.SEND_REMINDER_BEFORE, 0);
    }
}
