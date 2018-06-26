package com.hermansaksono.miband.operations;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.hermansaksono.miband.ActionCallback;
import com.hermansaksono.miband.MiBand;
import com.hermansaksono.miband.listeners.FetchActivityListener;
import com.hermansaksono.miband.listeners.NotifyListener;
import com.hermansaksono.miband.model.MiBandProfile;
import com.hermansaksono.miband.utils.CalendarUtils;
import com.hermansaksono.miband.utils.TypeConversionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by hermansaksono on 6/22/18.
 */

public class FetchActivityFromDate {

    private static final int BTLE_DELAY_SMALL = 250;
    private static final int BTLE_DELAY_MODERATE = 750;
    private static final int ONE_MIN_ARRAY_SUBSET_LENGTH = 4;
    private static final int STEPS_DATA_INDEX = 3;

    private BluetoothDevice device;
    private MiBand miBand;
    private MiBandProfile profile;
    private GregorianCalendar startDate;
    private Handler handler = new Handler();
    private int expectedNumberOfSamples;
    private int expectedNumberOfPackets;
    private List<List<Integer>> rawPackets;
    private List<Integer> fitnessSamples;
    private FetchActivityListener fetchActivityListener;

    private NotifyListener notifyListener = new NotifyListener() {
        @Override
        public void onNotify(byte[] data) {
            processRawActivityData(data);
        }
    };

    public FetchActivityFromDate(MiBandProfile profile, FetchActivityListener notifyListener) {
        this.profile = profile;
        this.fetchActivityListener = notifyListener;
    }

    public void perform(Context context, GregorianCalendar date) {
        Calendar expectedEndDate = CalendarUtils.getRoundedMinutes(GregorianCalendar.getInstance());
        expectedEndDate.add(Calendar.MINUTE, -1);

        this.miBand = getMiBand(context);
        this.startDate = date;
        this.expectedNumberOfSamples = (int) CalendarUtils.getDurationInMinutes(date, expectedEndDate);
        this.expectedNumberOfPackets = (int) Math.ceil(this.expectedNumberOfSamples / 4f);
        this.rawPackets = new ArrayList<>();
        this.handler = new Handler();

        //Log.d("SWELL", String.format("Fetching activities From %s to %s", date.getTime().toString(), expectedEndDate.getTime().toString()));
        //Log.d("SWELL", String.format("Expecting to stop after %d samples, %d packets", expectedNumberOfSamples, expectedNumberOfPackets));

        this.startScanAndFetchFitnessData();
    }

    private MiBand getMiBand(Context context) {
        if (this.miBand == null) {
            this.miBand = new MiBand(context);
        }
        return this.miBand;
    }

    private void startScanAndFetchFitnessData() {
        MiBand.startScan(scanCallback);
    }

    final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            device = result.getDevice();
            if (isThisTheDevice(device, profile)) {
                connectToMiBand(device);
                Log.d("mi-band", "Mi Band 2 found. Name:" + device.getName() + ",uuid:"
                        + device.getUuids() + ", add:"
                        + device.getAddress() + ", type:"
                        + device.getType() + ", bondState:"
                        + device.getBondState() + ", rssi:" + result.getRssi());
            }
        }
    };

    private boolean isThisTheDevice(BluetoothDevice device, MiBandProfile profile) {
        String name = device.getName();
        String address = device.getAddress();
        if (name != null && address != null) {
            return name.startsWith(MiBand.MI_BAND_PREFIX) && address.equals(profile.getAddress());
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
        this.miBand.enableFitnessDataNotify(this.notifyListener);
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

    /* ACTIVITY DATA PROCESSING METHODS */
    private void processRawActivityData(byte[] data) {
        Log.d("mi-band-2", "Fitness " + Arrays.toString(data));
        rawPackets.add(Arrays.asList(TypeConversionUtils.byteArrayToIntegerArray(data)));

        if (rawPackets.size() == expectedNumberOfPackets) {
            fitnessSamples = getFitnessSamplesFromRawPackets(rawPackets);
            notifyFetchListener();
            // Log.d("FitnessSamples", fitnessSamples.toString());
        }
    }

    private void notifyFetchListener() {
        if (fetchActivityListener != null) {
            fetchActivityListener.OnFetchComplete(this.startDate, this.fitnessSamples);
        }
    }

    private static List<Integer> getFitnessSamplesFromRawPackets(List<List<Integer>> rawSamples) {
        List<Integer> fitnessSamples = new ArrayList<>();
        for (List<Integer> rawSample : rawSamples) {
            fitnessSamples.add(getSteps(rawSample, 0));
            fitnessSamples.add(getSteps(rawSample, 1));
            fitnessSamples.add(getSteps(rawSample, 2));
            fitnessSamples.add(getSteps(rawSample, 3));
        }
        return fitnessSamples;
    }

    private static int getSteps(List<Integer> rawSample, int subindex) {
        int rawSampleIndex = (subindex * ONE_MIN_ARRAY_SUBSET_LENGTH) + STEPS_DATA_INDEX;
        if (rawSampleIndex < rawSample.size()) {
            return rawSample.get(rawSampleIndex);
        } else {
            return 0;
        }
    }
}
