package edu.neu.ccs.wellness.storytelling.viewmodel;


import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.support.annotation.NonNull;
import android.util.Log;

import edu.neu.ccs.wellness.miband2.ActionCallback;
import edu.neu.ccs.wellness.miband2.MiBand;
import edu.neu.ccs.wellness.storytelling.utils.StorywellPerson;

/**
 * Created by hermansaksono on 7/19/18.
 */

public class FitnessSyncViewModel extends AndroidViewModel {

    private MutableLiveData<SyncStatus> status = null;

    private MiBand miBand;
    private StorywellPerson storywellPerson;

    public FitnessSyncViewModel(@NonNull Application application) {
        super(application);
    }

    /**
     * Connect to the device owned by StorywellPerson, perform pairing, download data from band,
     * then upload it to FitnessRepository.
     * INVARIANT: The device has been been authenticated for this Android device.
     *
     * @param storywellPerson The Person in which fitness synchronization will be performed.
     * @return
     */
    public LiveData<SyncStatus> perform(StorywellPerson storywellPerson) {
        this.miBand = new MiBand(this.getApplication());
        this.storywellPerson = storywellPerson;
        if (this.status == null) {
            this.status = new MutableLiveData<>();
            this.status.setValue(SyncStatus.UNINITIALIZED);
        }
        MiBand.startScan(getScanCallback());
        return this.status;
    }

    /* BLUETOOTH SCAN CALLBACK */
    private ScanCallback getScanCallback() {
        return new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                BluetoothDevice device = result.getDevice();
                if (MiBand.isThisTheDevice(device, storywellPerson.getBtProfile())) {
                    MiBand.publishDeviceFound(device, result);
                    connectToMiBand(device);
                    MiBand.stopScan(this);
                }
            }
        };
    }

    /* BLUETOOTH CONNECTION METHODS */

    /**
     * INVARIANT: The device has been been authenticated for this Android device.
     * @param device The device to connect to.
     */
    private void connectToMiBand(BluetoothDevice device) {
        this.miBand.connect(device, new ActionCallback() {
            @Override
            public void onSuccess(Object data){
                doPair();
            }

            @Override
            public void onFail(int errorCode, String msg){
                Log.e("SWELL", String.format("Connect failed (%d): %s", errorCode, msg));
            }
        });
    }
    /*
    private void doAuthAndPair() {
        if (!isPaired(this.miBand)) {
            this.doAuth();
        } else {
            this.doPair();
        }
    }

    private void doAuth() {
        this.miBand.auth(new ActionCallback() {
            @Override
            public void onSuccess(Object data){
                doPair();
                Log.d("SWELL", String.format("Paired: %s", data.toString()));
            }
            @Override
            public void onFail(int errorCode, String msg){
                Log.e("SWELL", String.format("Auth failed (%d): %s", errorCode, msg));
            }
        });
    }
    */
    private void doPair() {
        this.miBand.pair(new ActionCallback() {
            @Override
            public void onSuccess(Object data){
                doPostPair();
                Log.d("SWELL", String.format("Paired: %s", data.toString()));
            }
            @Override
            public void onFail(int errorCode, String msg){
                Log.e("SWELL", String.format("Pair failed (%d): %s", errorCode, msg));
            }
        });
    }

    private void doPostPair() {
        this.status.postValue(SyncStatus.DOWNLOADING);
    }

    /* BLUETOOTH HELPER METHODS */
    private static boolean isPaired(MiBand miBand) {
        return miBand.getDevice().getBondState() != BluetoothDevice.BOND_NONE;
    }

}
