package edu.neu.ccs.wellness.miband2.operations;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import edu.neu.ccs.wellness.miband2.ActionCallback;
import edu.neu.ccs.wellness.miband2.MiBand;
import edu.neu.ccs.wellness.miband2.listeners.FetchActivityListener;
import edu.neu.ccs.wellness.miband2.listeners.NotifyListener;
import edu.neu.ccs.wellness.miband2.model.MiBandProfile;
import edu.neu.ccs.wellness.miband2.utils.CalendarUtils;
import edu.neu.ccs.wellness.miband2.utils.TypeConversionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by hermansaksono on 6/22/18.
 */

public class FetchActivityFromDate {

    /*
    private static final int BTLE_DELAY_MODERATE = 1000;
    private static final int BTLE_DELAY_LONG = 3000;
    private static final int ONE_MIN_ARRAY_SUBSET_LENGTH = 4;
    private static final int STEPS_DATA_INDEX = 3;
    private static final int NUM_PACKETS_INTEGRITY = 128;

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
        Log.d("SWELL", String.format("Expecting to stop after %d samples, %d packets", expectedNumberOfSamples, expectedNumberOfPackets));

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
            if (MiBand.isThisTheDevice(device, profile)) {
                connectToMiBand(device);
                Log.d("mi-band", "Mi Band 2 found. Name:" + device.getName() + ",uuid:"
                        + device.getUuids() + ", add:"
                        + device.getAddress() + ", type:"
                        + device.getType() + ", bondState:"
                        + device.getBondState() + ", rssi:" + result.getRssi());
            }
        }
    };

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
    */

    /* ACTIVITY DATA PROCESSING METHODS */
    /*
    private void processRawActivityData(byte[] data) {
        rawPackets.add(Arrays.asList(TypeConversionUtils.byteArrayToIntegerArray(data)));
        Log.d("MiBand activity fetch", String.format("Fitness packet %d: %s", rawPackets.size(), Arrays.toString(data)));

        if (rawPackets.size() == NUM_PACKETS_INTEGRITY) {
            waitAndComputeSamples(rawPackets.size());
        }
    }

    private void waitAndComputeSamples(final int numSamplesPreviously) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (rawPackets.size() > numSamplesPreviously) {
                    Log.d("MiBand activity fetch", String.format("Continue fetching after %d/%d packets", rawPackets.size(), expectedNumberOfPackets ));
                    waitAndComputeSamples(rawPackets.size());
                } else {
                    Log.d("MiBand activity fetch", String.format("Stopping fetch after %d/%d packets", rawPackets.size(), expectedNumberOfPackets ));
                    fitnessSamples = getFitnessSamplesFromRawPackets(rawPackets);
                    notifyFetchListener();
                    // Log.d("FitnessSamples", fitnessSamples.toString());
                }
            }
        }, BTLE_DELAY_LONG);
    }

    private void notifyFetchListener() {
        if (fetchActivityListener != null) {
            fetchActivityListener.OnFetchComplete(this.startDate, this.fitnessSamples);
        }
        this.miBand.disableFitnessDataNotify();
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
    */
}
