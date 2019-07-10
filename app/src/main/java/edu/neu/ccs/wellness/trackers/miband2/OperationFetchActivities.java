package edu.neu.ccs.wellness.trackers.miband2;

import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import edu.neu.ccs.wellness.trackers.callback.FetchActivityListener;
import edu.neu.ccs.wellness.trackers.callback.NotifyListener;
import edu.neu.ccs.wellness.trackers.miband2.model.Profile;
import edu.neu.ccs.wellness.trackers.miband2.model.Protocol;
import edu.neu.ccs.wellness.trackers.miband2.utils.CalendarUtils;
import edu.neu.ccs.wellness.trackers.miband2.utils.TypeConversionUtils;

/**
 * Created by hermansaksono on 6/22/18.
 */

public class OperationFetchActivities {

    private static final int BTLE_DELAY_MODERATE = 1000;
    private static final int BTLE_WAIT_FOR_ALL_SAMPLES = 2000;
    private static final int BTLE_DELAY_LONG = 3000;
    private static final int BTLE_DELAY_SHORT = 500;
    private static final int ONE_MIN_ARRAY_SUBSET_LENGTH = 4;
    private static final int STEPS_DATA_INDEX = 3;
    private static final int BROADCAST_PROGRESS_NTH_PACKET = 60;
    private static final int MAX_FETCHING_TRIALS = 10;
    private static final String TAG = "mi-band-activities";

    private BluetoothIO io;
    private GregorianCalendar startDateFromDevice;
    private int numberOfSamplesFromDevice;
    private int numberOfPacketsFromDevice;
    private List<List<Integer>> rawPackets;
    private int numberOfFetchingTrials = 0;

    private Handler handler;
    private FetchActivityListener fetchActivityListener;
    private NotifyListener notifyListener = new NotifyListener() {
        @Override
        public void onNotify(byte[] data) {
            processRawActivityData(data);
        }
    };
    private Runnable packetsWaitingRunnable;

    /** Constructor.
     * @param fetchActivityListener The callback that will notify fetching completion.
     * @param handler
     */
    public OperationFetchActivities(FetchActivityListener fetchActivityListener, Handler handler) {
        this.fetchActivityListener = fetchActivityListener;
        this.handler = handler;
        this.rawPackets = new ArrayList<>();
    }

    /**
     * Start fetching fitness data from the Bluetooth device.
     * @param io The BLE object that will handle bluetooth communication.
     * @param date The start date of the fetching.
     */
    public void perform(BluetoothIO io, GregorianCalendar date) {
        Calendar expectedEndDate = CalendarUtils.getRoundedMinutes(GregorianCalendar.getInstance());
        expectedEndDate.add(Calendar.MINUTE, -1);
        this.io = io;

        int expectedNumberOfSamples = (int) CalendarUtils.getDurationInMinutes(date, expectedEndDate);
        int expectedNumberOfPackets = (int) Math.ceil(expectedNumberOfSamples / 4f);

        Log.v(TAG, String.format("Expecting to stop after %d samples, %d packets",
                expectedNumberOfSamples, expectedNumberOfPackets));
        startFetchingFitnessData(date);
    }

    /**
     * First, set fitness data fetching listener (1/4).
     * @param startDate
     */
    private void startFetchingFitnessData(GregorianCalendar startDate) {
        this.io.stopNotifyListener(Profile.UUID_SERVICE_MILI, Profile.UUID_CHAR_5_ACTIVITY);
        this.io.setNotifyListener(Profile.UUID_SERVICE_MILI, Profile.UUID_CHAR_4_FETCH,
                new NotifyListener() {
            @Override
            public void onNotify(byte[] data) {
                Log.d(TAG, Arrays.toString(data));
                processResponseFromFetchingRequest(data);
            }
        });
        this.sendCommandParamsDelayed(startDate);
    }

    /**
     * Second, send the command params to te BLE device (2/4).
     * If the number of fetching trials is greater than the predefined MAX_FETCHING_TRIALS, then
     * complete the fetching process. Otherwise, start the fetching process by sending send the
     * command parameters after {@link #BTLE_DELAY_MODERATE} milliseconds. After that,
     * increment the fetching trial count by one.
     * Note: sending command parameters needs delay to make sure the BLE device is ready.
     * @param startDate
     */
    private void sendCommandParamsDelayed(final GregorianCalendar startDate) {
        if (this.numberOfFetchingTrials > MAX_FETCHING_TRIALS) {
            this.completeFetchingProcess();
        } else {
            this.numberOfFetchingTrials += 1;
            this.handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    sendCommandParams(startDate);
                }
            }, BTLE_DELAY_MODERATE);
        }
    }

    /**
     * Send the command parameters to the BLE device. The parameters is a bit array in the following
     * format: [1, 1, -29, 7, 7, 8, 7, 45, 0, -16].
     * @param startDate The start date and time of the fitness data that will be fetched. This will
     *                  be stored in bit 2-8 in the parameters' bit array.
     */
    private void sendCommandParams(GregorianCalendar startDate) {
        byte[] params = getFetchingParams(startDate);
        Log.d(TAG, String.format(
                "Requesting fitness data. \nStart date: %s.\nParams: %s\nAttempt number: %d",
                startDate.getTime().toString(),
                Arrays.toString(params),
                this.numberOfFetchingTrials));
        this.io.writeCharacteristic(Profile.UUID_CHAR_4_FETCH, params, null);
        this.startRetrievingFitnessData();
    }

    /**
     * Third, ask the BLE device to start sending fitness data (3/4).
     * Note: this does not need delay.
     */
    private void startRetrievingFitnessData() {
        this.io.setNotifyListener(
                Profile.UUID_SERVICE_MILI, Profile.UUID_CHAR_5_ACTIVITY, this.notifyListener);
    }

    /* ACTIVITY DATA NOTIFICATION METHODS */
    /**
     * Given the fetching request response from the BLE device do the appropriate actions.
     * If the response is [16, 1, 1, *], then start fetching in {@link #BTLE_DELAY_MODERATE}
     * milliseconds.
     * If the response is [16, 2, 1], then stop the fetching process because the BLE device has
     * sent all of the data it has.
     * @param responseArray
     */
    private void processResponseFromFetchingRequest(byte[] responseArray) {
        if (isDataTransferReady(responseArray)) {
            // Response is like [16, 1, 1, 5, 0, 0, 0, -30, 7, 8, 3, 14, 31, 0, -16]
            this.startDateFromDevice = getDateFromDeviceByteArray(responseArray);
            this.numberOfSamplesFromDevice = getNumSamplesFromByteArray(responseArray);
            this.numberOfPacketsFromDevice = (int) Math.ceil(this.numberOfSamplesFromDevice / 4f);
            this.startDelayedFetch();
        } else if (isAllDataTransferred(responseArray)) {
            // Response is [16, 2, 1]
            this.completeOrRestartFetching();
        }
    }

    /**
     * Start fetching fitness data in {@link #BTLE_DELAY_MODERATE} milliseconds, then handle the
     * responses from the BLE device using {@link #dataFetchRunnable} runnable.
     */
    private void startDelayedFetch() {
        if (this.dataFetchRunnable != null) {
            this.handler.removeCallbacks(this.dataFetchRunnable); // todo fix this
        }

        this.handler.postDelayed(this.dataFetchRunnable, BTLE_DELAY_MODERATE);
    }

    /**
     * A runnable that starts the data fetching.
     */
    private Runnable dataFetchRunnable = new Runnable() {
        @Override
        public void run() {
            startFetchingData();
        }
    };

    /**
     * Finally, tell the BLE device to send the fitness data packets (4/4).
     * If the device says it will send more than zero samples, then the fetching will commence.
     * Otherwise, if the device says it will send zero samples, the fetching will be stopped,
     * and the operation will be completed.
     */
    private void startFetchingData() {
        Log.v(TAG, String.format(
                "Receiving fitness data. \nStart date: %s.\nExpecting: %d samples, %d packets.",
                this.startDateFromDevice.getTime().toString(),
                this.numberOfSamplesFromDevice,
                this.numberOfPacketsFromDevice));
        this.io.writeCharacteristic(
                Profile.UUID_CHAR_4_FETCH, Protocol.COMMAND_ACTIVITY_FETCH, null);

        if (this.numberOfSamplesFromDevice <= 0) {
            Log.d(TAG, "Aborting fetch. Device says zero samples were available.");
            this.completeFetchingProcess();
        }
    }

    /**
     * This is called when the BLE device indicated that it has sent all of the samples.
     * If the there was 0 packets was received, then there must be an error, and the steps 2-4 must
     * be repeated.
     * Otherwise, the data fetching is okay. The fetching will be stopped and the operation will be
     * completed.
     *
     * INVARIANT: the BLE device indicated that it will send more than zero samples.
     */
    private void completeOrRestartFetching() {
        this.handler.removeCallbacks(packetsWaitingRunnable);

        if (this.rawPackets.isEmpty()) {
            this.sendCommandParamsDelayed(this.startDateFromDevice);
        } else {
            this.completeFetchingProcess();
        }
    }

    /* ACTIVITY DATA PROCESSING METHODS */

    /**
     * Put the raw data from BLE device into {@link  #rawPackets} array. Then start the timeout
     * handle, if it's not started already.
     * @param rawData The raw data from the BLE device.
     */
    private void processRawActivityData(byte[] rawData) {
        this.rawPackets.add(Arrays.asList(TypeConversionUtils.byteArrayToIntegerArray(rawData)));
        this.broadcastProgress();
        Log.d(TAG, String.format(
                "Fitness packet %d: %s", rawPackets.size(), Arrays.toString(rawData)));

        if (this.packetsWaitingRunnable == null) {
            this.waitAndComputeSamples(rawPackets.size());
        }
    }

    private void broadcastProgress() {
        if (this.rawPackets.size() % BROADCAST_PROGRESS_NTH_PACKET == 0) {
            this.fetchActivityListener.OnFetchProgress(
                    this.rawPackets.size(), this.numberOfPacketsFromDevice);
        }
    }

    /**
     * Start the timeout handler. If the size of {@link #rawPackets} array increased from the last
     * time the timeout handler was called, that means the fetching process is not stalling.
     * Therefore, the fetching process must be continued AND the timeout handler must be restarted.
     * Otherwise, it means that the size of {@link #rawPackets} does not increase from the last
     * time the timeout handler was called. It means the fetching process has stalled. Therefore,
     * the fetching process must be terminated.
     * @param numSamplesPreviously The number of samples from the last time the Runnable was called.
     */
    private void waitAndComputeSamples(final int numSamplesPreviously) {
        this.packetsWaitingRunnable = new Runnable() {
            @Override
            public void run() {
                if (rawPackets.size() > numSamplesPreviously) {
                    Log.v(TAG, String.format("Continue fetching after %d/%d packets",
                            rawPackets.size(), numberOfPacketsFromDevice ));
                    waitAndComputeSamples(rawPackets.size());
                } else {
                    Log.e(TAG, String.format("Abort fetching after %d/%d packets",
                            rawPackets.size(), numberOfPacketsFromDevice ));
                    completeFetchingProcess();
                }
            }
        };
        this.handler.postDelayed(this.packetsWaitingRunnable, BTLE_WAIT_FOR_ALL_SAMPLES);
    }

    /**
     * Terminate the fitness data fetching process. Generate fitness samples from the raw
     * {@link #rawPackets} array, than pass the resulting fitness samples to the callback {@link
     * #fetchActivityListener}. Finally, stop the notify listener, remove the callbacks, and delete
     * the timeout handler runnable.
     */
    private void completeFetchingProcess() {
        List<Integer> fitnessSamples = getFitnessSamplesFromRawPackets(
                rawPackets, numberOfSamplesFromDevice);
        this.fetchActivityListener.OnFetchComplete(
                this.startDateFromDevice, this.numberOfSamplesFromDevice, fitnessSamples);
        this.io.stopNotifyListener(Profile.UUID_SERVICE_MILI, Profile.UUID_CHAR_5_ACTIVITY);
        this.handler.removeCallbacks(this.packetsWaitingRunnable);
        this.packetsWaitingRunnable = null;
    }


    /* PARAM METHODS */
    /**
     * Generate the fetching parameters. The parameters are a bit array in the following format:
     * [1, 1, -29, 7, 7, 8, 7, 45, 0, -16].
     * @param startDate The start date and time of the fitness data that will be fetched. This will
     *                  be stored in bit 2-8 in the parameters' bit array.
     * @return Bit array containing the fetching parameters.
     */
    private static byte[] getFetchingParams(Calendar startDate) {
        byte[] paramStartTime = TypeConversionUtils.getTimeBytes(startDate, TimeUnit.MINUTES);
        return TypeConversionUtils.join(Protocol.COMMAND_ACTIVITY_PARAMS, paramStartTime);
    }

    private static boolean isDataTransferReady(byte[] byteArrayFromDevice) {
        return byteArrayFromDevice[1] == 1;
    }

    private static boolean isAllDataTransferred(byte[] byteArrayFromDevice) {
        return byteArrayFromDevice[1] == 2;
    }

    private static GregorianCalendar getDateFromDeviceByteArray(byte[] byteArrayFromDevice) {
        return (GregorianCalendar) CalendarUtils.bytesToCalendar(
                Arrays.copyOfRange(byteArrayFromDevice, 7, byteArrayFromDevice.length));
    }

    private static int getNumSamplesFromByteArray(byte[] byteArrayFromDevice) {
        return TypeConversionUtils.byteToInt(byteArrayFromDevice[3])
                + (TypeConversionUtils.byteToInt(byteArrayFromDevice[4]) * 256);
    }

    /* FITNESS SAMPLES METHODS */
    private static List<Integer> getFitnessSamplesFromRawPackets(List<List<Integer>> rawPackets,
                                                                 int numberOfSamplesFromDevice) {
        if (rawPackets.isEmpty()) {
            Log.d(TAG, String.format("Data completed, missing %s samples.",
                    numberOfSamplesFromDevice));
            return new ArrayList<>();
        } else {
            Log.d(TAG, String.format("Data completed with %d packets.", rawPackets.size()));
            return getFitnessSamples(rawPackets);
        }
    }

    private static List<Integer> getFitnessSamples(List<List<Integer>> rawPackets) {
        List<Integer> fitnessSamples = new ArrayList<>();
        for (List<Integer> rawSample : rawPackets) {
            addSteps(fitnessSamples, rawSample, 0);
            addSteps(fitnessSamples, rawSample, 1);
            addSteps(fitnessSamples, rawSample, 2);
            addSteps(fitnessSamples, rawSample, 3);
        }
        return fitnessSamples;
    }

    private static void addSteps(
            List<Integer> fitnessSamples, List<Integer> rawSamples, int subindex) {
        int rawSampleIndex = getRawSampleIndex(subindex);
        if (isFitnessSampleExists(rawSamples, rawSampleIndex)) {
            fitnessSamples.add(rawSamples.get(rawSampleIndex));
        }
    }

    private static int getRawSampleIndex(int subindex) {
        return (subindex * ONE_MIN_ARRAY_SUBSET_LENGTH) + STEPS_DATA_INDEX;
    }

    private static boolean isFitnessSampleExists(List<Integer> rawSample, int rawSampleIndex) {
        return rawSampleIndex < rawSample.size();
    }
}
