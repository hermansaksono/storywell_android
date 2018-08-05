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

    public static final String KEY_SCHEDULE_AFTER_COMPLETION = "KEY_SCHEDULE_AFTER_COMPLETION";
    public static final boolean DO_SCHEDULE = true;
    public static final boolean DO_NOT_SCHEDULE = false;

    private FitnessSync fitnessSync;
    private SyncStatus status; // TODO This may be not needed
    private boolean isScheduleAfterCompletion = DO_NOT_SCHEDULE;

    /* CONSTRUCTORS */
    public FitnessSyncService() { }

    /* OVERRIDEN METHODS */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("SWELL-SVC", "Starting sync service");
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
     * Schedule FitnessSync operation within the specified time. This uses AlarmManager for
     * scheduling.
     * @param context the context for setting up the AlarmManager.
     * @param triggerIntervalMillis
     */
    public static void scheduleFitnessSync(Context context, long triggerIntervalMillis) {
        long triggerAtMillisec = SystemClock.elapsedRealtime() + triggerIntervalMillis;

        Intent syncIntent = new Intent(context, FitnessSyncService.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
                syncIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtMillisec, pendingIntent);

        Log.d("SWELL-SVC", String.format("Scheduled sync in %d millis.", triggerAtMillisec));
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
        this.scheduleSyncIfNeeded();
        this.stopSelf();
    }

    private void scheduleSyncIfNeeded() {
        if (this.isScheduleAfterCompletion) {
            Log.d("SWELL-SVC", "Scheduling sync service");
            scheduleFitnessSync(getApplicationContext(), FitnessSyncService.SYNC_INTERVAL);
        } else {
            Log.d("SWELL-SVC", "Not scheduling sync service");
        }
    }

    private String getCurrentPersonString() {
        return this.fitnessSync.getCurrentPerson().toString();
    }
}
