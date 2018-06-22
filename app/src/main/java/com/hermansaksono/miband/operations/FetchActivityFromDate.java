package com.hermansaksono.miband.operations;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.util.Log;

import com.hermansaksono.miband.ActionCallback;
import com.hermansaksono.miband.MiBand;

import java.util.GregorianCalendar;

/**
 * Created by hermansaksono on 6/22/18.
 */

public class FetchActivityFromDate {

    private BluetoothDevice device;
    private MiBand miBand;
    private String miBandAddress;
    private GregorianCalendar sinceWhen;

    public void perform(Context context, String deviceAddress, GregorianCalendar sinceWhen) {
        this.miBand = new MiBand(context);
        this.miBandAddress = deviceAddress;
    }

    final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            device = result.getDevice();
            if (isThisTheDevice(device)) {
                connectToMiBand(device);
                Log.d("SWELL", "name:" + device.getName() + ",uuid:"
                        + device.getUuids() + ",add:"
                        + device.getAddress() + ",type:"
                        + device.getType() + ",bondState:"
                        + device.getBondState() + ",rssi:" + result.getRssi());
            }
        }
    };

    private boolean isThisTheDevice(BluetoothDevice device) {
        String name = device.getName();
        String address = device.getAddress();
        if (name != null && address != null) {
            return name.startsWith(MiBand.MI_BAND_PREFIX) && address.equals(this.miBandAddress);
        } else {
            return false;
        }
    }

    private void connectToMiBand(BluetoothDevice device) {
        this.miBand.connect(device, new ActionCallback() {
            @Override
            public void onSuccess(Object data) {
                Log.d("SWELL", "connect success");
                MiBand.stopScan(scanCallback);
                startFetchingActivityData();
            }

            @Override
            public void onFail(int errorCode, String msg) {
                Log.d("SWELL", "connect fail, code:" + errorCode + ",mgs:" + msg);
            }
        });
    }

    public void startFetchingActivityData() {

    }
}
