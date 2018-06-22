package com.hermansaksono.miband.operations;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.util.Log;

import com.hermansaksono.miband.ActionCallback;
import com.hermansaksono.miband.MiBand;
import com.hermansaksono.miband.listeners.RealtimeStepsNotifyListener;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by hermansaksono on 6/22/18.
 */

public class FetchTodaySteps {

    private static final int MINIMAL_NUM_FETCHES = 3;
    private BluetoothDevice device;
    private MiBand miBand;
    private String miBandAddress;
    private int steps = 0;
    private Date dateFetched;
    private int batteryPercent = 100;
    private int numFetches = 0;

    public void perform(Context context, String deviceAddress) {
        this.miBand = new MiBand(context);
        this.miBandAddress = deviceAddress;
        this.startScanAndFetchTodayStepData();
    }

    final ScanCallback scanCallback = new ScanCallback(){
        @Override
        public void onScanResult(int callbackType, ScanResult result){
            device = result.getDevice();
            if (isThisTheDevice(device)) {
                connectToMiBand(device);
                Log.d("SWELL","name:" + device.getName() + ",uuid:"
                        + device.getUuids() + ",add:"
                        + device.getAddress() + ",type:"
                        + device.getType() + ",bondState:"
                        + device.getBondState() + ",rssi:" + result.getRssi());
            }
        }
    };

    private void startScanAndFetchTodayStepData() {
        MiBand.startScan(scanCallback);
    }

    private boolean isThisTheDevice(BluetoothDevice device) {
        String name = device.getName();
        String address = device.getAddress();
        if (name !=  null && address != null) {
            return name.startsWith(MiBand.MI_BAND_PREFIX) && address.equals(this.miBandAddress);
        } else {
            return false;
        }
    }

    private void connectToMiBand(BluetoothDevice device) {
        this.miBand.connect(device, new ActionCallback() {
            @Override
            public void onSuccess(Object data){
                Log.d("SWELL","connect success");
                MiBand.stopScan(scanCallback);
                getRealTimeStepsNotification();
            }
            @Override
            public void onFail(int errorCode, String msg){
                Log.d("SWELL","connect fail, code:"+errorCode+",mgs:"+msg);
            }
        });
    }

    private void disconnectMiBand() {
        this.numFetches += 1;
        if (this.numFetches >= MINIMAL_NUM_FETCHES) {
            this.miBand.disableRealtimeStepsNotify();
            this.miBand.disconnect();
            this.numFetches = 0;
        }
    }

    private void getRealTimeStepsNotification() {
        this.miBand.setRealtimeStepsNotifyListener(new RealtimeStepsNotifyListener() {
            @Override
            public void onNotify(int steps){
                setSteps(steps);
                disconnectMiBand();
            }
        });
        this.miBand.enableRealtimeStepsNotify();
    }

    private void setSteps(int steps) {
        this.steps = steps;
        this.dateFetched = Calendar.getInstance().getTime();
        Log.d("SWELL", "Steps: " + this.steps + ", fetched on " + this.dateFetched.toString());
    }

    public int getSteps() {
        return this.steps;
    }
}
