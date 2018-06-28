package edu.neu.ccs.wellness.miband2.operations;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.util.Log;

import edu.neu.ccs.wellness.miband2.ActionCallback;
import edu.neu.ccs.wellness.miband2.MiBand;
import edu.neu.ccs.wellness.miband2.listeners.HeartRateNotifyListener;
import edu.neu.ccs.wellness.miband2.listeners.RealtimeStepsNotifyListener;
import edu.neu.ccs.wellness.miband2.model.MiBandProfile;

import java.util.Date;

/**
 * Created by hermansaksono on 6/22/18.
 */

public class MonitorRealtimeSteps {

    private BluetoothDevice device;
    private MiBand miBand;
    private MiBandProfile profile;
    private RealtimeStepsNotifyListener listener;
    private int steps = 0;

    public void connect(Context context, MiBandProfile profile, RealtimeStepsNotifyListener listener) {
        this.miBand = new MiBand(context);
        this.profile = profile;
        this.listener = listener;
        this.startScanAndMonitorSteps();
    }

    public void disconnect() {
        if (this.miBand != null) {
            this.miBand.disableRealtimeStepsNotify();
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
                getRealTimeStepsNotification();
            }
            @Override
            public void onFail(int errorCode, String msg){
                Log.d("SWELL","connect fail, code:"+errorCode+",mgs:"+msg);
            }
        });
    }

    private void getRealTimeStepsNotification() {
        this.miBand.setRealtimeStepsNotifyListener(new RealtimeStepsNotifyListener() {
            @Override
            public void onNotify(int steps){
                setSteps(steps);
                listener.onNotify(steps);
            }
        });
        this.miBand.enableRealtimeStepsNotify();
    }

    private void setSteps(int steps) {
        this.steps = steps;
    }

    public int getSteps() {
        return this.steps;
    }
}
