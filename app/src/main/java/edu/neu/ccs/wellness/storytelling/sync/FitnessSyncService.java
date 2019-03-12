package edu.neu.ccs.wellness.storytelling.sync;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import edu.neu.ccs.wellness.storytelling.Storywell;

public class FitnessSyncService extends Service
        implements FitnessSync.OnFitnessSyncProcessListener {
    /* CONSTANTS */
    public static final long ONE_SEC_IN_MILLIS = 1000;
    public static final long ONE_MINUTE_IN_SEC = 60;
    //public static final long SYNC_INTERVAL = AlarmManager.INTERVAL_HOUR * 2;
    public static final long SYNC_INTERVAL = ONE_MINUTE_IN_SEC * ONE_SEC_IN_MILLIS;
    public static final int REQUEST_CODE = 0;

    public static final String KEY_SCHEDULE_AFTER_COMPLETION = "KEY_SCHEDULE_AFTER_COMPLETION";
    public static final boolean DO_SCHEDULE = true;
    public static final boolean DO_NOT_SCHEDULE = false;
    
    private static final String TAG = "SWELL-SVC";

    private FitnessSync fitnessSync;
    private SyncStatus status; // TODO This may be not needed
    private boolean isScheduleAfterCompletion = DO_NOT_SCHEDULE;

    /* CONSTRUCTORS */
    public FitnessSyncService() { }

    /* OVERRIDEN METHODS */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Starting sync service");
        Storywell storywell = new Storywell(getApplicationContext());
        this.isScheduleAfterCompletion = intent.getBooleanExtra(
                KEY_SCHEDULE_AFTER_COMPLETION, DO_NOT_SCHEDULE);
        this.fitnessSync = new FitnessSync(getApplicationContext(), this);
        this.fitnessSync.perform(storywell.getGroup());
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /* OnFitnessSyncProcessListener METHODS */
    @Override
    public void onSetUpdate(SyncStatus syncStatus) {
        this.handleStatusChange(syncStatus);
    }

    @Override
    public void onPostUpdate(SyncStatus syncStatus) {
        this.handleStatusChange(syncStatus);
    }

    /* PUBLIC SCHEDULING METHODS */

    /**
     * Schedule FitnessSync operation within {@link FitnessSyncService}.SYNC_INTERVAL. 
     * This uses AlarmManager for scheduling.
     * @param context the context for setting up the AlarmManager.
     */
    public static void scheduleFitnessSync(Context context) {
        scheduleFitnessSync(context, FitnessSyncService.SYNC_INTERVAL);
    }
    
    /**
     * Schedule FitnessSync operation within the specified time. This uses AlarmManager for
     * scheduling.
     * @param context the context for setting up the AlarmManager.
     * @param triggerIntervalMillis
     */
    public static void scheduleFitnessSync(Context context, long triggerIntervalMillis) {
        Log.d(TAG, "Scheduling sync service");
        long triggerAtMillisec = SystemClock.elapsedRealtime() + triggerIntervalMillis;

        Intent syncIntent = new Intent(context, FitnessSyncService.class);
        syncIntent.putExtra(KEY_SCHEDULE_AFTER_COMPLETION, DO_SCHEDULE);
        PendingIntent pendingIntent = PendingIntent.getService(
                context, REQUEST_CODE, syncIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtMillisec, pendingIntent);

        Log.d(TAG, String.format("Scheduled sync in %d millis.", triggerIntervalMillis));
    }

    /* SYNC UPDATE METHODS */
    private void handleStatusChange(SyncStatus syncStatus) {
        this.status = syncStatus;

        if (SyncStatus.CONNECTING.equals(syncStatus)) {
            Log.d(TAG, "Connecting: " + getCurrentPersonString());
        } else if (SyncStatus.DOWNLOADING.equals(syncStatus)) {
            Log.d(TAG, "Downloading fitness data: " + getCurrentPersonString());
        } else if (SyncStatus.UPLOADING.equals(syncStatus)) {
            Log.d(TAG, "Uploading fitness data: " + getCurrentPersonString());
        } else if (SyncStatus.IN_PROGRESS.equals(syncStatus)) {
            Log.d(TAG, "Sync completed for: " + getCurrentPersonString());
            this.fitnessSync.performNext();
        } else if (SyncStatus.COMPLETED.equals(syncStatus)) {
            completeSync();
            Log.d(TAG, "All sync successful!");
        } else if (SyncStatus.FAILED.equals(syncStatus)) {
            completeSync();
            Log.d(TAG, "Sync failed");
        }
    }

    private void completeSync() {
        Log.d(TAG, "Stopping sync service");
        this.fitnessSync.stop();
        this.scheduleSyncIfNeeded();
        this.stopSelf();
    }

    private void scheduleSyncIfNeeded() {
        if (this.isScheduleAfterCompletion) {
            scheduleFitnessSync(getApplicationContext(), FitnessSyncService.SYNC_INTERVAL);
        } else {
            Log.d(TAG, "Not scheduling sync service");
        }
    }

    private String getCurrentPersonString() {
        return this.fitnessSync.getCurrentPerson().toString();
    }
}
