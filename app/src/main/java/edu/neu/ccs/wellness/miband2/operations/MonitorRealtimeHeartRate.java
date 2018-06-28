package edu.neu.ccs.wellness.miband2.operations;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.util.Log;

import edu.neu.ccs.wellness.miband2.ActionCallback;
import edu.neu.ccs.wellness.miband2.MiBand;
import edu.neu.ccs.wellness.miband2.listeners.HeartRateNotifyListener;
import edu.neu.ccs.wellness.miband2.model.MiBandProfile;

/**
 * Created by hermansaksono on 6/22/18.
 */

public class MonitorRealtimeHeartRate {

    private BluetoothDevice device;
    private MiBand miBand;
    private MiBandProfile profile;
    private HeartRateNotifyListener listener;

    private android.os.Handler handler = new android.os.Handler();

    public void connect(Context context, MiBandProfile profile, HeartRateNotifyListener listener) {
        this.miBand = new MiBand(context);
        this.profile = profile;
        this.listener = listener;
        this.startScanAndFetch();
    }

    public void disconnect() {
        if (this.miBand != null) {
            this.miBand.stopHeartRateScan();
            this.miBand.disconnect();
        }
    }

    final ScanCallback scanCallback = new ScanCallback(){
        @Override
        public void onScanResult(int callbackType, ScanResult result){
            device = result.getDevice();
            if (MiBand.isThisTheDevice(device, profile)) {
                MiBand.publishDeviceFound(device, result);
                connectToMiBand(device);
            }
        }
    };

    private void startScanAndFetch() {
        MiBand.startScan(scanCallback);
    }

    private void connectToMiBand(BluetoothDevice device) {
        this.miBand.connect(device, new ActionCallback() {
            @Override
            public void onSuccess(Object data){
                Log.d("SWELL","connect success");
                MiBand.stopScan(scanCallback);
                enableHeartRateNotification();
            }
            @Override
            public void onFail(int errorCode, String msg){
                Log.d("SWELL","connect fail (code " + errorCode + "), mgs: "+msg);
            }
        });
    }

    private void enableHeartRateNotification() {
        this.miBand.startHeartRateScan();
        this.handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                enableHeartRateListener();
            }
        }, 1000);
    }

    private void enableHeartRateListener() {
        this.miBand.setHeartRateScanListener(listener);
    }
}
