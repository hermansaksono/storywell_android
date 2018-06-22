package com.hermansaksono.miband.operations;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.hermansaksono.miband.ActionCallback;
import com.hermansaksono.miband.MiBand;
import com.hermansaksono.miband.listeners.NotifyListener;

import java.util.Arrays;
import java.util.GregorianCalendar;

/**
 * Created by hermansaksono on 6/22/18.
 */

public class FetchActivityFromDate {

    private static final int BTLE_DELAY_SMALL = 250;
    private static final int BTLE_DELAY_MODERATE = 1000;

    private BluetoothDevice device;
    private MiBand miBand;
    private String miBandAddress;
    private GregorianCalendar startDate;
    private Handler handler = new Handler();
    private NotifyListener listener = new NotifyListener() {
        @Override
        public void onNotify(byte[] data) {
            Log.d("mi-band-2", "Fitness " + Arrays.toString(data));
        }
    };;

    public void perform(Context context, String deviceAddress, GregorianCalendar date) {
        this.miBand = new MiBand(context);
        this.miBandAddress = deviceAddress;
        this.startDate = date;
        this.handler = new Handler();
        this.startScanAndFetchFitnessData();
    }

    private void startScanAndFetchFitnessData() {
        MiBand.startScan(scanCallback);
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
                startFetchingFitnessData();
            }

            @Override
            public void onFail(int errorCode, String msg) {
                Log.d("SWELL", "connect fail, code:" + errorCode + ",mgs:" + msg);
            }
        });
    }

    private void startFetchingFitnessData() {
        this.miBand.disableFitnessDataNotify();
        this.handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                enableFetchUpdatesNotify();
            }
        }, BTLE_DELAY_MODERATE);
    }

    private void enableFetchUpdatesNotify() {
        this.miBand.enableFetchUpdatesNotify();
        this.handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                sendCommandParams();
            }
        }, BTLE_DELAY_MODERATE);
    }

    private void sendCommandParams() {
        this.miBand.sendCommandParams(this.startDate);
        this.handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                enableFitnessDataNotify();
            }
        },BTLE_DELAY_MODERATE);
    }

    private void enableFitnessDataNotify() {
        this.miBand.enableFitnessDataNotify(this.listener);
        this.handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startNotifyingFitnessData();
            }
        },BTLE_DELAY_MODERATE);
    }

    private void startNotifyingFitnessData() {
        this.miBand.startNotifyingFitnessData();
    }
}
