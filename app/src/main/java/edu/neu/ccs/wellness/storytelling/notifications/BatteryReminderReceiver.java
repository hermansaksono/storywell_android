package edu.neu.ccs.wellness.storytelling.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;

import edu.neu.ccs.wellness.notifications.RegularNotificationManager;
import edu.neu.ccs.wellness.storytelling.HomeActivity;
import edu.neu.ccs.wellness.storytelling.R;
import edu.neu.ccs.wellness.storytelling.SplashScreenActivity;
import edu.neu.ccs.wellness.storytelling.Storywell;
import edu.neu.ccs.wellness.storytelling.settings.SynchronizedSetting;
import edu.neu.ccs.wellness.storytelling.settings.SynchronizedSetting.FitnessSyncInfo;
import edu.neu.ccs.wellness.storytelling.sync.MiBandBatteryModel;
import edu.neu.ccs.wellness.utils.date.HourMinute;

/**
 * Created by hermansaksono on 2/5/19.
 */

public class BatteryReminderReceiver extends BroadcastReceiver {
    public static final int MIN_BATTERY_LEVEL = 20;

    @Override
    public void onReceive(Context context, Intent intent) {
        sendABatteryNotification(context);
    }

    public static void sendABatteryNotification(Context context) {
        MiBandBatteryModel miBandBatteryModel = new MiBandBatteryModel(context);

        boolean isShowCaregiverBatteryLow = miBandBatteryModel.isCaregiverBatteryLevelLow();
        boolean isShowChildBatteryLow = miBandBatteryModel.isChildBatteryLevelLow();

        if (isShowCaregiverBatteryLow && isShowChildBatteryLow) {
            sendABatteryNotification(miBandBatteryModel.getCaregiverName(),
                    miBandBatteryModel.getChildName(), context);
            return;
        }

        if (isShowCaregiverBatteryLow) {
            sendABatteryNotification(miBandBatteryModel.getCaregiverName(), context);
        }

        if (isShowChildBatteryLow) {
            sendABatteryNotification(miBandBatteryModel.getChildName(), context);
        }
    }

    public static boolean isCaregiverBatteryLevelLow(FitnessSyncInfo fitnessSyncInfo) {
        return fitnessSyncInfo.getCaregiverDeviceInfo().getBtBatteryLevel() <= MIN_BATTERY_LEVEL;
    }

    public static boolean isChildBatteryLevelLow(FitnessSyncInfo fitnessSyncInfo) {
        return fitnessSyncInfo.getChildDeviceInfo().getBtBatteryLevel() <= MIN_BATTERY_LEVEL;
    }

    /**
     * Send a battery notification appropriate for the specified day.
     * @param caregiverName
     * @param childName
     * @param context
     */
    public static void sendABatteryNotification(
            String caregiverName, String childName, Context context) {
        String message = String.format(context.getString(R.string.notification_people_battery_low),
                caregiverName, childName);
        sendABatteryNotificationWithMessage(message, context);
    }

    /**
     * Send a battery notification appropriate for the specified day.
     * @param personName
     * @param context
     */
    public static void sendABatteryNotification(String personName, Context context) {
        String message = String.format(context.getString(R.string.notification_person_battery_low),
                personName);
        sendABatteryNotificationWithMessage(message, context);
    }

    private static void sendABatteryNotificationWithMessage(String message, Context context) {
        RegularNotificationManager manager = new RegularNotificationManager(
                context.getString(R.string.notification_default_channel_id));

        String title = context.getString(R.string.notification_battery_low_title);

        Intent intent = getRetrievingActivityIntent(context);


        manager.showNotification(Constants.BATTERY_NOTIFICATION_ID, title, message,
                Constants.DEFAULT_NOTIFICATION_ICON_RESID,intent, context);
        Log.d("SWELL", "Battery reminder sent");

    }

    private static Intent getRetrievingActivityIntent(Context context) {
        Intent intent = new Intent(context, SplashScreenActivity.class);
        intent.putExtra(HomeActivity.KEY_DEFAULT_TAB, HomeActivity.TAB_ADVENTURE);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return intent;
    }

    /**
     * Check if the reminder has been scheduled.
     * @param context
     * @return
     */
    public static boolean isScheduled(Context context) {
        return (PendingIntent.getBroadcast(context, Constants.BATTERY_REMINDER_REQUEST_CODE,
                getAlarmIntent(context),
                PendingIntent.FLAG_NO_CREATE) != null);
    }

    /**
     * Schedule daily regular reminders a few hours before the challenge end time (as specified
     * in the User's configuration)
     * @param context
     */
    public static void scheduleBatteryReminders(Context context) {
        Storywell storywell = new Storywell(context);

        // Determine the time for the alarm reminder
        SynchronizedSetting setting = storywell.getSynchronizedSetting();
        HourMinute hourMinute = setting.getChallengeEndTime();
        Calendar reminderCal = getReminderCalendar(hourMinute);

        // Schedule the Alarm
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, reminderCal.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, getReminderReceiverIntent(context));
        Log.d("SWELL", "Battery reminder scheduled every " + reminderCal.toString());
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
        return Math.max(hourMinute.getHour() + Constants.BATTERY_REMINDER_OFFSET, 0);
    }

    private static PendingIntent getReminderReceiverIntent(Context context) {
        return PendingIntent.getBroadcast(
                context, Constants.BATTERY_REMINDER_REQUEST_CODE, getAlarmIntent(context), 0);
    }

    private static Intent getAlarmIntent(Context context) {
        return new Intent(context, BatteryReminderReceiver.class);
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
