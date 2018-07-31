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

import edu.neu.ccs.wellness.storytelling.Storywell;
import edu.neu.ccs.wellness.storytelling.sync.FitnessSync.OnFitnessSyncProcessListener;

public class FitnessSyncReceiver extends BroadcastReceiver
        implements OnFitnessSyncProcessListener {

    public static final long ONE_SEC_IN_MILLIS = 1000;
    public static final long ONE_MINUTE_IN_SEC = 60;
    //public static final long SYNC_INTERVAL = AlarmManager.INTERVAL_HOUR * 2;
    //public static final long SYNC_INTERVAL = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
    public static final long SYNC_INTERVAL = ONE_MINUTE_IN_SEC * ONE_SEC_IN_MILLIS;

    private FitnessSync fitnessSync;
    private SyncStatus status;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("SWELL-SVC", "Starting sync service");
        Storywell storywell = new Storywell(context);
        this.fitnessSync = new FitnessSync(context.getApplicationContext(), this);
        this.fitnessSync.perform(storywell.getGroup());
        FitnessSyncReceiver.scheduleFitnessSync(context, FitnessSyncReceiver.SYNC_INTERVAL);
    }

    @Override
    public void onSetUpdate(SyncStatus syncStatus) {
        this.handleStatusChange(syncStatus);
    }

    @Override
    public void onPostUpdate(SyncStatus syncStatus) {
        this.handleStatusChange(syncStatus);
    }

    /* SYNC UPDATE METHODS */
    private void handleStatusChange(SyncStatus syncStatus) {
        this.status = syncStatus;

        if (SyncStatus.CONNECTING.equals(syncStatus)) {
            Log.d("SWELL-SVC", "Connecting: " + getCurrentPersonString());
        } else if (SyncStatus.DOWNLOADING.equals(syncStatus)) {
            Log.d("SWELL-SVC", "Downloading fitness data: " + getCurrentPersonString());
        } else if (SyncStatus.UPLOADING.equals(syncStatus)) {
            Log.d("SWELL-SVC", "Uploading fitness data: " + getCurrentPersonString());
        } else if (SyncStatus.IN_PROGRESS.equals(syncStatus)) {
            Log.d("SWELL-SVC", "Sync completed for: " + getCurrentPersonString());
            this.fitnessSync.performNext();
        } else if (SyncStatus.COMPLETED.equals(syncStatus)) {
            completeSync();
            Log.d("SWELL-SVC", "All sync successful!");
        } else if (SyncStatus.FAILED.equals(syncStatus)) {
            completeSync();
            Log.d("SWELL-SVC", "Sync failed");
        }
    }

    private void completeSync() {
        Log.d("SWELL-SVC", "Stopping sync service");
        this.fitnessSync.stop();
    }

    private String getCurrentPersonString() {
        return this.fitnessSync.getCurrentPerson().toString();
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
