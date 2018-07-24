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

import edu.neu.ccs.wellness.storytelling.sync.FitnessSync.OnFitnessSyncProcessListener;

public class FitnessSyncReceiver extends BroadcastReceiver
        implements OnFitnessSyncProcessListener {

    public static final long SYNC_INTERVAL = AlarmManager.INTERVAL_HOUR * 2;

    private FitnessSync fitnessSync;
    private SyncStatus status;

    @Override
    public void onReceive(Context context, Intent intent) {
        //this.fitnessSync = new FitnessSync(context.getApplicationContext(), this);
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
            Log.d("SWELL", "Connecting: " + getCurrentPersonString());
        } else if (SyncStatus.DOWNLOADING.equals(syncStatus)) {
            Log.d("SWELL", "Downloading fitness data: " + getCurrentPersonString());
        } else if (SyncStatus.UPLOADING.equals(syncStatus)) {
            Log.d("SWELL", "Uploading fitness data: " + getCurrentPersonString());
        } else if (SyncStatus.IN_PROGRESS.equals(syncStatus)) {
            Log.d("SWELL", "Sync completed for: " + getCurrentPersonString());
            this.fitnessSync.performNext();
        } else if (SyncStatus.SUCCESS.equals(syncStatus)) {
            Log.d("SWELL", "All sync successful!");
        } else if (SyncStatus.FAILED.equals(syncStatus)) {
            Log.d("SWELL", "Sync failed");
        }
    }

    private String getCurrentPersonString() {
        return this.fitnessSync.getCurrentPerson().toString();
    }

    /* PUBLIC SCHEDULING METHODS */
    public static void scheduleFitnessSync(Context context) {
        long triggerAtMillisec = SystemClock.elapsedRealtime() + SYNC_INTERVAL;
        Intent syncIntent = new Intent(context, FitnessSyncReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
                syncIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtMillisec,
                SYNC_INTERVAL, pendingIntent);
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
