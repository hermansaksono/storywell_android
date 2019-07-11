package edu.neu.ccs.wellness.storytelling.sync;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import edu.neu.ccs.wellness.fitness.storage.FitnessRepository;
import edu.neu.ccs.wellness.fitness.storage.onDataUploadListener;
import edu.neu.ccs.wellness.trackers.BatteryInfo;
import edu.neu.ccs.wellness.trackers.DeviceProfile;
import edu.neu.ccs.wellness.trackers.callback.ActionCallback;
import edu.neu.ccs.wellness.trackers.callback.BatteryInfoCallback;
import edu.neu.ccs.wellness.trackers.miband2.MiBand;
import edu.neu.ccs.wellness.trackers.callback.FetchActivityListener;
import edu.neu.ccs.wellness.people.Group;
import edu.neu.ccs.wellness.people.Person;
import edu.neu.ccs.wellness.storytelling.utils.StorywellPerson;
import edu.neu.ccs.wellness.trackers.miband2.MiBandScanner;
import edu.neu.ccs.wellness.utils.WellnessDate;

/**
 * Created by hermansaksono on 7/19/18.
 */

public class FitnessSync {

    /**
     * The minutes in which a new sync can be initiated. This must not be zero.
     */
    private static final int SYNC_INTERVAL_MINS = 5;
    private static final int SAFE_MINUTES = 0;
    private static final int REAL_INTERVAL_MINS = SAFE_MINUTES + SYNC_INTERVAL_MINS;
    private static final int SYNC_TIMEOUT_MILLIS = 90 * 1000;
    private static final int MIN_FETCHING_DATA_GAP = 7;
    private static final String TAG = "SWELL-SYNC";

    private Context context;

    private MiBandScanner miBandScanner;
    private MiBand miBand;
    private StorywellPerson currentPerson = null;
    private List<StorywellPerson> storywellMembers;
    private List<StorywellPerson> personSyncQueue = new Vector<>();
    private List<StorywellPerson> syncedPersons = new Vector<>();
    private Map<StorywellPerson, BluetoothDevice> discoveredDevices = new HashMap<>();
    private boolean isQueueBeingProcessed = false;
    private boolean isScanCallbackRunning = true;

    //private ScanCallback scanCallback;
    private OnFitnessSyncProcessListener listener;
    private Handler handlerReSync;
    private Handler handlerTimeOut;

    /* VARIABLES FOR UPLOADING DATA */
    private FitnessRepository fitnessRepository;

    /* INTERFACE */
    public interface OnFitnessSyncProcessListener {
        void onSetUpdate(SyncStatus syncStatus);
        void onPostUpdate(SyncStatus syncStatus);
    }

    /* SCAN CALLBACK */
    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if (isScanCallbackRunning) {
                handleFoundDevice(result.getDevice());
            }
        }

        @Override
        public void onScanFailed (int errorCode) {
            Log.e(TAG, "Scan failed. Error code: " + errorCode);
        }
    };

    /* CONSTRUCTOR*/
    public FitnessSync(@NonNull Context context, OnFitnessSyncProcessListener listener ) {
        this.context = context.getApplicationContext();
        this.fitnessRepository = new FitnessRepository();
        this.listener = listener;
        this.handlerReSync = new Handler();
        this.handlerTimeOut = new Handler();
    }

    /* PUBLIC METHODS*/
    /**
     * Returns true if all members of the groups were synced within the last n minutes as defined
     * in {@link #SYNC_INTERVAL_MINS}. Otherwise return false;
     * @param group The group that will be checked on.
     * @return A {@link boolean} that indicates the members last sync times are within the interval.
     */
    public boolean isSyncedWithinInterval(Group group) {
        this.storywellMembers = getStorywellMembers(group, context);
        return isSyncedWithinInterval(this.storywellMembers, REAL_INTERVAL_MINS, this.context);
    }

    /**
     * Connect to fitness trackers, download the data from the tracker, and upload it to the
     * repository. These steps are performed to each of the members of Group. The data must be
     * downloaded starting from startDate that is unique to every user.
     * @param group
     */
    public void perform(Group group) {
        this.storywellMembers = getStorywellMembers(group, context);

        StringBuilder sb = new StringBuilder("Scanning for this BT devices= [");
        for (StorywellPerson person : this.storywellMembers) {
            sb.append(person.getBtProfile().getAddress()).append(", ");
        }
        sb.append("]");
        Log.d(TAG, sb.toString());

        boolean isSynced = isSyncedWithinInterval(this.storywellMembers, REAL_INTERVAL_MINS, this.context);
        if (!isSynced) {
            this.isQueueBeingProcessed = false;
            this.isScanCallbackRunning = true;
            this.personSyncQueue.clear();
            this.syncedPersons.clear();
            this.discoveredDevices.clear();

            this.miBand = new MiBand();
            this.miBandScanner = new MiBandScanner(this.context);
            this.miBandScanner.startScan(this.scanCallback);
            this.listener.onSetUpdate(SyncStatus.INITIALIZING);

            this.restartTimeoutTimer();
        } else {
            this.listener.onSetUpdate(SyncStatus.NO_NEW_DATA);
        }
    }

    private static boolean isSyncedWithinInterval(
            List<StorywellPerson> members, int intervalMins, Context context) {
        for (StorywellPerson storywellPerson : members) {
            if (!storywellPerson.isLastSyncTimeWithinInterval(intervalMins, context)) {
                Log.d(TAG, "At least one tracker was not synced within interval.");
                return false;
            } else {
                Log.d(TAG, "All trackers were synced within interval");
            }
        }
        return true;
    }

    /**
     * Perform synchronization on the next person in queue.
     */
    public boolean performNext () {
        if (this.storywellMembers.size() == 0) {
            return false;
        } else {
            this.connectFromQueue(this.personSyncQueue);
            this.restartTimeoutTimer();
            return true;
        }
    }

    /**
     * Stops all operations.
     */
    public void stop() {
        if (this.miBand != null) {
            this.miBand.disconnect();
        }
        this.stopScan();
        this.stopTimeoutTimer();
    }

    /**
     * Stops scanning.
     */
    public void stopScan() {
        if (this.miBandScanner != null && this.scanCallback != null) {
            Log.d(TAG, "Stopping Bluetooh scan.");
            this.miBandScanner.stopScan(this.scanCallback);
            this.miBandScanner = null;
            this.isScanCallbackRunning = false;
        }
    }

    /**
     * Get the person whose data is currently being fetched.
     * @return @StorywellPerson Person who is currently being synchronized.
     */
    public StorywellPerson getCurrentPerson() {
        return this.currentPerson;
    }

    /* BLUETOOTH SCAN CALLBACK */
    private ScanCallback getScanCallback() {
        return new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                handleFoundDevice(result.getDevice());
            }
        };
    }

    private void handleFoundDevice(BluetoothDevice device) {
        StorywellPerson storywellPerson = this.getPersonWhoWearsThisDevice(device);

        if (storywellPerson != null && !this.isPersonDeviceHasBeenFound(storywellPerson)) {
            Log.v(TAG, "Mi Band 2 found: " + storywellPerson.toString());
            this.discoveredDevices.put(storywellPerson, device);
            this.addPersonToQueue(storywellPerson);
            this.startProcessingQueue();
        } else {
            Log.v(TAG,
                    "Mi Band 2 found (" + device.getAddress() + "), but has been discovered");
        }

        if (this.isAllTrackersBeenFound()) {
            Log.d(TAG,"All trackers have been found "
                    + this.discoveredDevices.toString());
            this.stopScan();
            this.stopTimeoutTimer();
        }
    }

    private void addPersonToQueue(StorywellPerson storywellPerson) {
        this.personSyncQueue.add(storywellPerson);
    }

    private void startProcessingQueue() {
        if (this.isQueueBeingProcessed == false) {
            this.connectFromQueue(this.personSyncQueue);
            this.isQueueBeingProcessed = true;
        }
    }

    private StorywellPerson getPersonWhoWearsThisDevice(BluetoothDevice device) {
        for (StorywellPerson person: this.storywellMembers) {
            String address = person.getBtProfile().getAddress();
            if (address.equals(device.getAddress())) {
                return person;
            }
        }
        return null;
    }

    private boolean isPersonDeviceHasBeenFound(StorywellPerson storywellPerson) {
        return this.discoveredDevices.containsKey(storywellPerson);
    }

    private boolean isAllTrackersBeenFound() {
        return discoveredDevices.size() == storywellMembers.size();
    }

    private boolean isAllTrackersHasBeenSynced() {
        return syncedPersons.size() == storywellMembers.size();
    }

    /* BLUETOOTH CONNECTION METHODS */
    private void connectFromQueue(List<StorywellPerson> queue) {
        if (queue.size() > 0) {
            this.currentPerson = queue.get(0);
            queue.remove(0);
            Log.d(TAG, "Start connecting to: " + this.currentPerson.toString());
            this.connectToMiBand(this.discoveredDevices.get(this.currentPerson), this.currentPerson);
        } else {
            Log.d(TAG, "Connecting from queue is paused because queue is empty");
            this.isQueueBeingProcessed = false;
        }
        if (isAllTrackersHasBeenSynced()) {
            Log.d(TAG, "All trackers have been synchronized.");
            this.listener.onSetUpdate(SyncStatus.COMPLETED);
            this.stop();
            this.startSyncTimer();
        }
    }

    /**
     * INVARIANT: The device has been been authenticated for this Android device.
     * @param device The device to connect to.
     * @param person
     */
    private void connectToMiBand(BluetoothDevice device, final StorywellPerson person) {

        GregorianCalendar startDate = (GregorianCalendar) person.getLastSyncTime(this.context);
        if (getDeltaMinutes(startDate) <= 0) {
            Log.d(TAG, String.format("Stopping syncing %s: Start datetime is equal with now.",
                    person.getPerson().getName()));
            this.doBypassThisPersonBTDevice(person);
            return;
        }

        this.miBand = MiBand.newConnectionInstance(device, this.context, new ActionCallback() {
            @Override
            public void onSuccess(Object data){
                Log.d(TAG, String.format("Connecting to %s's band successful.", person.getPerson().getName()));
                doPair(person);
            }

            @Override
            public void onFail(int errorCode, String msg){
                Log.e(TAG, String.format("Connect failed (%d): %s", errorCode, msg));
            }
        });
        // this.restartTimeoutTimer();
        this.listener.onSetUpdate(SyncStatus.CONNECTING);
    }

    /* PAIRING METHODS */
    private void doPair(final StorywellPerson person) {
        this.restartTimeoutTimer();
        Log.d(TAG, String.format("Now pairing to %s's band", person.getPerson().getName()));
        this.miBand.pair(new ActionCallback() {
            @Override
            public void onSuccess(Object data){
                Log.d(TAG, String.format("Paired with %s's band: %s", person.getPerson().getName(), data.toString()));
                doPostPair(person);
            }
            @Override
            public void onFail(int errorCode, String msg){
                Log.e(TAG, String.format("Pair with %s's band failed (%d): %s", person.getPerson().getName(), errorCode, msg));
            }
        });
    }

    private void doPostPair(StorywellPerson person) {
        this.doDownloadFromBand(person);
    }

    /* DOWNLOADING METHODS */

    /**
     * Download fitness data from the BLE device that is associated with {@param person}.
     * If the difference between the {@param person}'s last sync time and the current time is zero
     * minutes, then the downloading process is skipped. Otherwise, downloading process is initiated.
     * @param person the person in which the fitness data will be downloaded.
     */
    private void doDownloadFromBand(final StorywellPerson person) {
        this.restartTimeoutTimer();
        GregorianCalendar startDate = (GregorianCalendar) person.getLastSyncTime(this.context);
        /*
        GregorianCalendar endDate = getExpectedEndDate();
        if (getDeltaMinutes(startDate) <= 0) {
            Log.d(TAG, String.format("Stopping sync. Start datetime is equal with now for %s",
                    person.getPerson().getName()));
            this.doCompleteOneBtDevice(person, 0);
        } else {
            this.doStartDownloading(person, startDate);
        }
        */
        this.doStartDownloading(person, startDate);
    }

    /**
     * Start downloading fitness data from BLE device that is associated with {@param person}.
     * INVARIANT: At least, one minute of data can be downloaded from the BLE device.
     * @param person
     * @param startDate
     */
    private void doStartDownloading(final StorywellPerson person, GregorianCalendar startDate) {

        this.listener.onPostUpdate(SyncStatus.DOWNLOADING);

        this.miBand.fetchActivityData(startDate, new FetchActivityListener() {
            @Override
            public void OnFetchComplete(Calendar startDate, int expectedSamples, List<Integer> steps) {
                doRetrieveBatteryLevel(person);

                // A case when the BLE device sends empty fitness data, although it promised to send
                // more than one data.
                if (steps.isEmpty()) {
                    onFetchingFailed(person, startDate, steps, expectedSamples);
                    return;
                }

                // Case when the BLE device sends some data, but the length of the data is
                // significantly shorter than what is promised.
                int deltaMinutes = getDeltaMinutes(startDate);
                if (isFetchingResultTooShort(steps.size(), deltaMinutes)) {
                    doUploadToRepository(person, startDate, steps, deltaMinutes);
                }
                // Otherwise, everything is ok.
                else {
                    doUploadToRepository(person, startDate, steps, expectedSamples);
                }
            }

            @Override
            public void OnFetchProgress(int index, int numData) {
                Log.d(TAG, String.format("Fetching %d data out of %d", index, numData));
                restartTimeoutTimer();
            }

            private boolean isFetchingResultTooShort(int stepsDataLength, int deltaMinutes) {
                return deltaMinutes - stepsDataLength > MIN_FETCHING_DATA_GAP;
            }
        });

        Log.d(TAG, String.format("Downloading %s\'s fitness data from %s",
                person.getPerson().getName(), startDate.getTime().toString()));

    }

    /* UPLOADING METHODS */
    private void doUploadToRepository(final StorywellPerson person, Calendar startDate,
                                      List<Integer> steps, int expectedSamples) {
        this.restartTimeoutTimer();
        this.listener.onPostUpdate(SyncStatus.UPLOADING);
        final int missingMinutes = expectedSamples - steps.size();
        int minutesElapsed = steps.size() - SAFE_MINUTES;
        setPersonLastSyncTime(person, startDate, minutesElapsed);

        final Date date = startDate.getTime();
        this.fitnessRepository.insertIntradaySteps(person.getPerson(), date, steps,
                new onDataUploadListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, String.format("Uploading %s fitness data.",
                        currentPerson.getPerson().getName()));
                doUpdateDailyFitness(person, date, missingMinutes);
            }

            @Override
            public void onFailed() {
                Log.e(TAG, String.format("Error uploading %s fitness data",
                        currentPerson.getPerson().getName()));
            }
        });
    }

    private void setPersonLastSyncTime(StorywellPerson person, Calendar startDate, int minutes) {
        GregorianCalendar endDate = WellnessDate.getCalendarAfterNMinutes(startDate, minutes);
        person.setLastSyncTime(this.context, endDate);
        Log.d(TAG, String.format("Updating %s last sync time from %s by %d minutes.",
                person.getPerson().getName(), startDate.getTime().toString(), minutes));
    }

    private void doUpdateDailyFitness(
            final StorywellPerson storywellPerson, Date startDate, final int missingMinutes) {
        this.fitnessRepository.updateDailyFitness(storywellPerson.getPerson(),
                startDate, new onDataUploadListener(){
            @Override
            public void onSuccess() {
                doCompleteOneBtDevice(storywellPerson, missingMinutes);
            }

            @Override
            public void onFailed() {
                Log.e(TAG, String.format("Error updating %s daily fitness data",
                        currentPerson.getPerson().getName()));
            }
        });
    }

    private void onFetchingFailed(
            StorywellPerson person, Calendar startDate, List<Integer> steps, int expectedSamples) {
        int minutesElapsed;

        Log.e(TAG,
                String.format("Fetching %s's fitness data failed. %d/%d steps received",
                        currentPerson.getPerson().getName(),
                        steps.size(),
                        expectedSamples));

        switch (steps.size()) {
            case 0:
                minutesElapsed = 0;
                break;
            case 1:
                minutesElapsed = 1;
                break;
            default:
                minutesElapsed = steps.size();
                break;
        }

        setPersonLastSyncTime(person, startDate, minutesElapsed);
        this.listener.onPostUpdate(SyncStatus.FAILED);
    }

    /* BATTERY LEVEL RETRIEVAL */
    private void doRetrieveBatteryLevel(final StorywellPerson storywellPerson) {
        this.miBand.getBatteryInfo(new BatteryInfoCallback() {
            @Override
            public void onSuccess(BatteryInfo batteryInfo) {
                storywellPerson.setBatteryLevel(context, batteryInfo.getLevel());
                Log.d(TAG, String.format("%s battery level: %s",
                        currentPerson.getPerson().getName(), batteryInfo.toString()));
            }

            @Override
            public void onFail(int errorCode, String msg) {
                Log.e(TAG, String.format("Error retrieving %s battery info",
                        currentPerson.getPerson().getName()));
            }
        });
    }

    /* COMPLETION METHODS */
    private void doCompleteOneBtDevice(StorywellPerson storywellPerson, int missingMinutes) {
        /*
        this.miBand.disconnect();
        this.addToSyncedList(storywellPerson);
        if (missingMinutes < 0) {
            this.listener.onPostUpdate(SyncStatus.FAILED);
            this.stopTimeoutTimer();
        } else {
            this.listener.onPostUpdate(SyncStatus.IN_PROGRESS);
            this.restartTimeoutTimer();
        }
        */
        if (missingMinutes > MIN_FETCHING_DATA_GAP) {
            this.doDownloadFromBand(storywellPerson);
        } else {
            this.miBand.disconnect();
            this.addToSyncedList(storywellPerson);
            this.listener.onPostUpdate(SyncStatus.IN_PROGRESS);
            this.restartTimeoutTimer();
        }
    }

    private void doBypassThisPersonBTDevice(StorywellPerson storywellPerson) {
        this.addToSyncedList(storywellPerson);
        this.listener.onPostUpdate(SyncStatus.IN_PROGRESS);
        this.restartTimeoutTimer();
    }

    private void addToSyncedList(StorywellPerson storywellPerson) {
        if (!this.syncedPersons.contains(storywellPerson)) {
            this.syncedPersons.add(storywellPerson);
        }
    }

    private void startSyncTimer() {
        this.handlerReSync.postDelayed(new Runnable() {
            @Override
            public void run() {
                listener.onPostUpdate(SyncStatus.NEW_DATA_AVAILABLE);
            }
        }, SYNC_INTERVAL_MINS * 60 * 1000);
    }

    /* Handle Sync Timeout */
    private Runnable timeoutRunnable = new Runnable() {
        @Override
        public void run() {
            stopScan();
            listener.onPostUpdate(SyncStatus.FAILED);
            Log.e(TAG, "Bluetooth timer timeout.");
        }
    };


    private void restartTimeoutTimer() {
        Log.d(TAG, "Restarting Bluetooth timer.");
        this.stopTimeoutTimer();
        this.handlerTimeOut.postDelayed(timeoutRunnable, SYNC_TIMEOUT_MILLIS);
    }

    private void stopTimeoutTimer() {
        if (this.handlerTimeOut != null)
            this.handlerTimeOut.removeCallbacks(timeoutRunnable);
    }

    /* ERROR METHOD */
    private void onScanFailed(int errorCode) {
        this.listener.onSetUpdate(SyncStatus.FAILED);
    }

    /* STORYWELL HELPER */
    private static List<StorywellPerson> getStorywellMembers(Group group, Context context) {
        List<StorywellPerson> storywellPeople = new ArrayList<>();
        for (Person person : group.getMembers()) {
            storywellPeople.add(StorywellPerson.newInstance(person, context));
        }
        return storywellPeople;
    }

    private static GregorianCalendar getExpectedEndDate() {
        GregorianCalendar expectedEndDate = (GregorianCalendar)
                WellnessDate.getRoundedMinutes(GregorianCalendar.getInstance());
        //expectedEndDate.add(Calendar.MINUTE, -1);
        return expectedEndDate;
    }

    private static int getDeltaMinutes(Calendar startDate) {
        long deltaMillis = getNowCalendar().getTimeInMillis() - startDate.getTimeInMillis();
        return (int) TimeUnit.MILLISECONDS.toMinutes(deltaMillis);
    }

    private static Calendar getNowCalendar() {
        return WellnessDate.getRoundedMinutes(GregorianCalendar.getInstance());
    }

    /*
    private static int getNumberOfMinutes(Calendar startDate, Calendar endDate) {
        return (int) WellnessDate.getDurationInMinutes(startDate, endDate);
    }

    private static List<DeviceProfile> getProfileList(List<StorywellPerson> storywellPersonList) {
        List<DeviceProfile> profileList = new ArrayList<>();
        for (StorywellPerson storywellPerson : storywellPersonList) {
            profileList.add(storywellPerson.getBtProfile());
        }
        return profileList;
    }

    private static List<Integer> getSamplesFromEmpty(int numSamples) {
        List<Integer> fitnessSamples = new ArrayList<>();
        for (int i = 0; i < numSamples; i++) {
            fitnessSamples.add(0);
        }
        return fitnessSamples;
    }
    */
}