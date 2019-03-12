package edu.neu.ccs.wellness.storytelling.sync;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.SystemClock;
import android.util.Log;

public class FitnessSyncReceiver extends BroadcastReceiver {

    public static final long ONE_SEC_IN_MILLIS = 1000;
    public static final long ONE_MINUTE_IN_SEC = 60;
    //public static final long SYNC_INTERVAL = AlarmManager.INTERVAL_HOUR * 2;
    public static final long SYNC_INTERVAL = ONE_MINUTE_IN_SEC * ONE_SEC_IN_MILLIS;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("SWELL-SVC-RCVR", "Receiving sync request");
        Intent syncServiceIntent = new Intent(context, FitnessSyncService.class);
        syncServiceIntent.putExtra(
                FitnessSyncService.KEY_SCHEDULE_AFTER_COMPLETION, FitnessSyncService.DO_SCHEDULE);
        context.startService(syncServiceIntent);
    }

    /* PUBLIC SCHEDULING METHODS */
    /**
     * Schedule FitnessSync operation within the specified time. This uses AlarmManager for
     * scheduling.
     * @param context the context for setting up the AlarmManager.
     * @param triggerIntervalMillis
     */
    public static void scheduleFitnessSync(Context context, long triggerIntervalMillis) {
        long triggerAtMillisec = SystemClock.elapsedRealtime() + triggerIntervalMillis;
        Intent syncIntent = new Intent(context, FitnessSyncReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
                syncIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtMillisec, pendingIntent);
        Log.d("SWELL-SVC-RCVR", String.format("Scheduled sync in %d millisecs.", triggerIntervalMillis));
    }

    public static void unscheduleFitnessSync(Context context, PendingIntent syncPendingIntent) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(syncPendingIntent);
    }

    public static void scheduleAfterEveryRestart(Context context) {
        ComponentName receiver = new ComponentName(context, ScheduleFitnessSyncReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    public static void unscheduleAfterEveryRestart(Context context) {
        ComponentName receiver = new ComponentName(context, ScheduleFitnessSyncReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }
}
