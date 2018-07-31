package edu.neu.ccs.wellness.storytelling.sync;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
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

import edu.neu.ccs.wellness.fitness.storage.FitnessRepository;
import edu.neu.ccs.wellness.fitness.storage.onDataUploadListener;
import edu.neu.ccs.wellness.trackers.DeviceProfile;
import edu.neu.ccs.wellness.trackers.callback.ActionCallback;
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

    private static final int SAFE_MINUTES = 5;

    private Context context;

    private MiBandScanner miBandScanner;
    private MiBand miBand;
    private List<StorywellPerson> storywellMembers;
    private List<StorywellPerson> personSyncQueue = new Vector<>();
    private List<StorywellPerson> syncedPersons = new Vector<>();
    private Map<StorywellPerson, BluetoothDevice> discoveredDevices = new HashMap<>();
    private boolean isQueueBeingProcessed = false;

    private ScanCallback scanCallback;
    private OnFitnessSyncProcessListener listener;

    private StorywellPerson currentPerson = null;

    /* VARIABLES FOR UPLOADING DATA */
    private FitnessRepository fitnessRepository;

    /* INTERFACE */
    public interface OnFitnessSyncProcessListener {
        void onSetUpdate(SyncStatus syncStatus);
        void onPostUpdate(SyncStatus syncStatus);
    }


    /* CONSTRUCTOR*/
    public FitnessSync(@NonNull Context context, OnFitnessSyncProcessListener listener ) {
        this.context = context.getApplicationContext();
        this.fitnessRepository = new FitnessRepository();
        this.listener = listener;
    }

    /* PUBLIC METHODS*/
    /**
     * Connect to fitness trackers, download the data from the tracker, and upload it to the
     * repository. These steps are performed to each of the members of Group. The data must be
     * downloaded starting from startDate that is unique to every user.
     * @param group
     */
    public void perform(Group group) {
        this.storywellMembers = getStorywellMembers(group, context);
        this.miBand = new MiBand();
        this.miBandScanner = new MiBandScanner();
        this.scanCallback = getScanCallback();
        this.miBandScanner.startScan(this.scanCallback);
    }

    /**
     * Perform synchronization on the next person in queue.
     */
    public boolean performNext () {
        if (this.storywellMembers.size() == 0) {
            return false;
        } else {
            this.connectFromQueue(this.personSyncQueue);
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
    }

    /**
     * Stops scanning.
     */
    public void stopScan() {
        if (this.miBandScanner != null && this.scanCallback != null) {
            this.miBandScanner.stopScan(this.scanCallback);
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
            Log.v("SWELL", "Mi Band 2 found: " + storywellPerson.toString());
            this.discoveredDevices.put(storywellPerson, device);
            this.addPersonToQueue(storywellPerson);
            this.startProcessingQueue(storywellPerson);
        } else {
            Log.v("SWELL",
                    "Mi Band 2 found (" + device.getAddress() + "), but has been discovered");
        }

        if (this.isAllTrackersBeenFound()) {
            Log.d("SWELL","All trackers have been found "
                    + this.discoveredDevices.toString());
            this.stopScan();
        }
    }

    private void addPersonToQueue(StorywellPerson storywellPerson) {
        this.personSyncQueue.add(storywellPerson);
        /*
        if (Person.ROLE_PARENT.equals(storywellPerson.getPerson().getRole())) {
            this.personSyncQueue.add(0, storywellPerson);
        } else {
            this.personSyncQueue.add(this.personSyncQueue.size(), storywellPerson);
        }
        */
    }

    private void startProcessingQueue(StorywellPerson maybeParent) {
        /*
        if (Person.ROLE_PARENT.equals(maybeParent.getPerson().getRole())) {
            this.connectFromQueue(this.personSyncQueue);
            this.isQueueBeingProcessed = true;
        }
        */
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
            Log.d("SWELL", "Connecting to: " + this.currentPerson.toString());
            this.connectToMiBand(this.discoveredDevices.get(this.currentPerson), this.currentPerson);
        } else {
            Log.d("SWELL", "Connecting from queue is paused because queue is empty");
            this.isQueueBeingProcessed = false;
        }
        if (isAllTrackersHasBeenSynced()) {
            Log.d("SWELL", "All trackers have been synchronized.");
            this.listener.onSetUpdate(SyncStatus.SUCCESS);
            this.stop();
        }
    }

    /**
     * INVARIANT: The device has been been authenticated for this Android device.
     * @param device The device to connect to.
     * @param person
     */
    private void connectToMiBand(BluetoothDevice device, final StorywellPerson person) {
        this.miBand = MiBand.newConnectionInstance(device, this.context, new ActionCallback() {
            @Override
            public void onSuccess(Object data){
                doPair(person);
            }

            @Override
            public void onFail(int errorCode, String msg){
                Log.e("SWELL", String.format("Connect failed (%d): %s", errorCode, msg));
            }
        });
        this.listener.onSetUpdate(SyncStatus.CONNECTING);
    }

    /* PAIRING METHODS */
    private void doPair(final StorywellPerson person) {
        this.miBand.pair(new ActionCallback() {
            @Override
            public void onSuccess(Object data){
                doPostPair(person);
                Log.d("SWELL", String.format("Paired: %s", data.toString()));
            }
            @Override
            public void onFail(int errorCode, String msg){
                Log.e("SWELL", String.format("Pair failed (%d): %s", errorCode, msg));
            }
        });
    }

    private void doPostPair(StorywellPerson person) {
        this.doDownloadFromBand(person);
    }

    /* DOWNLOADING METHODS */
    private void doDownloadFromBand(final StorywellPerson person) {
        GregorianCalendar startDate = person.getLastSyncTime(this.context);
        this.listener.onPostUpdate(SyncStatus.DOWNLOADING);
        this.miBand.fetchActivityData(startDate, new FetchActivityListener() {
            @Override
            public void OnFetchComplete(Calendar startDate, List<Integer> steps) {
                doUploadToRepository(person, startDate, steps);
            }
        });
        Log.d("SWELL", String.format("Downloading %s\'s fitness data from %s",
                person.getPerson().getName(), startDate.getTime().toString()));
    }

    /* UPLOADING METHODS */
    private void doUploadToRepository(final StorywellPerson person,
                                      Calendar startDate, List<Integer> steps) {
        this.listener.onPostUpdate(SyncStatus.UPLOADING);
        int minutesElapsed = steps.size() - SAFE_MINUTES;
        person.setLastSyncTime(this.context,
                WellnessDate.getCalendarAfterNMinutes(startDate, minutesElapsed));
        final Date date = startDate.getTime();
        this.fitnessRepository.insertIntradaySteps(person.getPerson(), startDate.getTime(), steps,
                new onDataUploadListener() {
            @Override
            public void onSuccess() {
                doUpdateDailyFitness(person, date);
            }

            @Override
            public void onFailed() {
                Log.e("SWELL", String.format("Error uploading %s fitness data",
                        currentPerson.getPerson().getName()));
            }
        });
    }

    private void doUpdateDailyFitness(final StorywellPerson storywellPerson, Date startDate) {
        this.fitnessRepository.updateDailyFitness(storywellPerson.getPerson(),
                startDate, new onDataUploadListener(){
            @Override
            public void onSuccess() {
                doCompleteOneBtDevice(storywellPerson);
            }

            @Override
            public void onFailed() {
                Log.e("SWELL", String.format("Error updating %s daily fitness data",
                        currentPerson.getPerson().getName()));
            }
        });
    }

    /* COMPLETION METHODS */
    private void doCompleteOneBtDevice(StorywellPerson storywellPerson) {
        this.miBand.disconnect();
        this.addToSyncedList(storywellPerson);
        this.listener.onPostUpdate(SyncStatus.IN_PROGRESS);
    }

    private void addToSyncedList(StorywellPerson storywellPerson) {
        if (!this.syncedPersons.contains(storywellPerson)) {
            this.syncedPersons.add(storywellPerson);
        }
    }

    /* STORYWELL HELPER */
    private static List<StorywellPerson> getStorywellMembers(Group group, Context context) {
        List<StorywellPerson> storywellPeople = new ArrayList<>();
        for (Person person : group.getMembers()) {
            storywellPeople.add(StorywellPerson.newInstance(person, context));
        }
        return storywellPeople;
    }

    private static List<DeviceProfile> getProfileList(List<StorywellPerson> storywellPersonList) {
        List<DeviceProfile> profileList = new ArrayList<>();
        for (StorywellPerson storywellPerson : storywellPersonList) {
            profileList.add(storywellPerson.getBtProfile());
        }
        return profileList;
    }
}