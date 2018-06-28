package edu.neu.ccs.wellness.miband2.operations;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import edu.neu.ccs.wellness.miband2.ActionCallback;
import edu.neu.ccs.wellness.miband2.MiBand;
import edu.neu.ccs.wellness.miband2.listeners.NotifyListener;
import edu.neu.ccs.wellness.miband2.listeners.RealtimeStepsNotifyListener;
import edu.neu.ccs.wellness.miband2.model.MiBandProfile;

/**
 * Created by hermansaksono on 6/22/18.
 */

public class MonitorSensorData {

    private static final int BTLE_DELAY_MODERATE = 1000;

    private BluetoothDevice device;
    private MiBand miBand;
    private MiBandProfile profile;
    private NotifyListener listener;

    private Handler handler = new android.os.Handler();

    public void connect(Context context, MiBandProfile profile, NotifyListener listener) {
        this.miBand = new MiBand(context);
        this.profile = profile;
        this.listener = listener;
        this.startScanAndMonitorSteps();
    }

    public void disconnect() {
        if (this.miBand != null) {
            this.miBand.disableSensorDataNotify();
            this.miBand.disconnect();
            this.miBand = null;
        }
    }

    final ScanCallback scanCallback = new ScanCallback(){
        @Override
        public void onScanResult(int callbackType, ScanResult result){
            device = result.getDevice();
            if (MiBand.isThisTheDevice(device, profile)) {
                connectToMiBand(device);
                MiBand.publishDeviceFound(device, result);
            }
        }
    };

    private void startScanAndMonitorSteps() {
        MiBand.startScan(scanCallback);
    }

    private void connectToMiBand(BluetoothDevice device) {
        this.miBand.connect(device, new ActionCallback() {
            @Override
            public void onSuccess(Object data){
                Log.d("SWELL","connect success");
                MiBand.stopScan(scanCallback);
                startMonitorSensorData();
            }
            @Override
            public void onFail(int errorCode, String msg){
                Log.d("SWELL","connect fail, code:"+errorCode+",mgs:"+msg);
            }
        });
    }

    private void startMonitorSensorData() {
        doTurnOffOneTimeHeartRateSensor();
    }

    private void doTurnOffOneTimeHeartRateSensor() {
        this.miBand.turnOnOneTimeHeartRateSensor();
        this.handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                doTurnOnContinuousHeartRateSensor();
            }
        }, BTLE_DELAY_MODERATE);
    }

    private void doTurnOnContinuousHeartRateSensor() {
        this.miBand.turnOnContinuousHeartRateSensor();
        this.handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                enableSensorNotifications();
            }
        }, BTLE_DELAY_MODERATE);
    }

    private void enableSensorNotifications() {
        this.miBand.enableSensorNotifications();
        this.handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                enableHeartRateNotifications();
            }
        }, BTLE_DELAY_MODERATE);
    }

    private void enableHeartRateNotifications() {
        this.miBand.enableHeartRateNotifications(new NotifyListener() {
            @Override
            public void onNotify(byte[] data) {
                listener.onNotify(data);
            }
        });

        this.handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startHeartRateNotifications();
            }
        }, BTLE_DELAY_MODERATE);
    }

    private void startHeartRateNotifications() {
        this.miBand.startHeartRateNotifications();
        this.handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startFetching();
            }
        }, BTLE_DELAY_MODERATE);
    }

    private void startFetching(){
        this.miBand.startSensingNow();
    }
}
