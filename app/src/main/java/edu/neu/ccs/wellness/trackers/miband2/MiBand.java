package edu.neu.ccs.wellness.trackers.miband2;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import edu.neu.ccs.wellness.trackers.GenericTrackingDevice;
import edu.neu.ccs.wellness.trackers.StepsTrackingDevice;
import edu.neu.ccs.wellness.trackers.callback.ActionCallback;
import edu.neu.ccs.wellness.trackers.callback.BatteryInfoCallback;
import edu.neu.ccs.wellness.trackers.callback.FetchActivityListener;
import edu.neu.ccs.wellness.trackers.miband2.listeners.HeartRateNotifyListener;
import edu.neu.ccs.wellness.trackers.callback.NotifyListener;
import edu.neu.ccs.wellness.trackers.miband2.listeners.RealtimeStepsNotifyListener;
import edu.neu.ccs.wellness.trackers.miband2.model.MiBand2BatteryInfo;
import edu.neu.ccs.wellness.trackers.miband2.model.FitnessSample;
import edu.neu.ccs.wellness.trackers.miband2.model.Profile;
import edu.neu.ccs.wellness.trackers.miband2.model.Protocol;
import edu.neu.ccs.wellness.trackers.miband2.utils.CalendarUtils;
import edu.neu.ccs.wellness.trackers.UserInfo;
import edu.neu.ccs.wellness.trackers.miband2.utils.TypeConversionUtils;
import edu.neu.ccs.wellness.trackers.miband2.model.VibrationMode;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

public class MiBand implements GenericTrackingDevice, StepsTrackingDevice {

    /* CONSTANTS */
    public static final String MI_BAND_PREFIX = "MI Band" ;
    public static final int BTLE_DELAY_MODERATE = 1000;
    public static final int ACTIVITY_PACKET_LENGTH = 17;
    private static final String TAG = "miband-android";

    /* PROPERTIES */
    private Context context;
    private BluetoothIO io;
    private Handler handler;

    /* CONSTRUCTOR(S) */
    public MiBand(Context context) {
        this.context = context;
        this.io = new BluetoothIO();
        this.handler = new Handler();
    }

    /* SCANNING METHODS */
    /**
     * Start Bluetooth LE devices scan, then perform the callback on each discovered devices.
     *
     * @param callback
     */
    public static void startScan(ScanCallback callback) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (null == adapter) {
            Log.e(TAG, "BluetoothAdapter is null");
            return;
        }
        BluetoothLeScanner scanner = adapter.getBluetoothLeScanner();
        if (null == scanner) {
            Log.e(TAG, "BluetoothLeScanner is null");
            return;
        }
        scanner.startScan(callback);
    }

    /**
     * Stop Bluetooth LE devices scan.
     * @param callback
     */
    public static void stopScan(ScanCallback callback) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (null == adapter) {
            Log.e(TAG, "BluetoothAdapter is null");
            return;
        }
        BluetoothLeScanner scanner = adapter.getBluetoothLeScanner();
        if (null == scanner) {
            Log.e(TAG, "BluetoothLeScanner is null");
            return;
        }
        scanner.stopScan(callback);
    }

    /** Connect to a specific device.
     *
     * @param device The {@link BluetoothDevice} to be connected.
     * @param callback An {@link ActionCallback} that is executed after the device is connected.
     */
    @Override
    public void connect(BluetoothDevice device, final ActionCallback callback) {
        ActionCallback actionCallback = new ActionCallback() {
            @Override
            public void onSuccess(Object data){
                Log.d(TAG,"Connect success");
                callback.onSuccess(data);
            }
            @Override
            public void onFail(int errorCode, String msg){
                Log.d(TAG, String.format("Connect failed (%d): %s", errorCode, msg));
                callback.onFail(errorCode, msg);
            }
        };
        this.io.connect(context, device, actionCallback);
    }

    /**
     * Disconnect the currently connected device.
     */
    @Override
    public void disconnect() {
        this.io.disconnect();
    }

    /**
     * Set the disconnected listener..
     */
    @Override
    public void setDisconnectedListener(NotifyListener disconnectedListener) {
        this.io.setDisconnectedListener(disconnectedListener);
    }

    /**
     * Perform Auth initialization on the currently connected device.
     *
     * @param callback An {@link ActionCallback} that is executed after the device has been paired.
     */
    @Override
    public void auth(final ActionCallback callback) {
        ActionCallback actionCallback = new ActionCallback() {
            @Override
            public void onSuccess(Object data){
                Log.d(TAG, String.format("Auth success: %s", data.toString()));
                callback.onSuccess(data);
            }
            @Override
            public void onFail(int errorCode, String msg){
                Log.d(TAG, String.format("Auth failed (%d): %s", errorCode, msg));
                callback.onFail(errorCode, msg);
            }
        };

        if (this.io.isConnected()) {
            OperationPair pairOperation = new OperationPair(this.io, this.handler);
            pairOperation.auth(actionCallback);
        } else {
            Log.e(TAG, "Bluetooth device is not connected yet");
        }
    }

    /**
     * Perform Bluetooth pairing on the currently connected device.
     *
     * @param callback An {@link ActionCallback} that is executed after the device has been paired.
     */
    @Override
    public void pair(final ActionCallback callback) {
        ActionCallback actionCallback = new ActionCallback() {
            @Override
            public void onSuccess(Object data){
                Log.d(TAG, String.format("Pair success: %s", data.toString()));
                publishDevice(getDevice());
                callback.onSuccess(data);
            }
            @Override
            public void onFail(int errorCode, String msg){
                Log.d(TAG, String.format("Pair failed (%d): %s", errorCode, msg));
                callback.onFail(errorCode, msg);
            }
        };

        if (this.io.isConnected()) {
            OperationPair pairOperation = new OperationPair(this.io, this.handler);
            pairOperation.pair(actionCallback);
        } else {
            Log.e(TAG, "Bluetooth device is not connected yet");
        }
    }

    /* METHOD FOR RETRIEVING AND SETTING BASIC INFORMATION FROM THE DEVICE */
    /**
     * Get the current device.
     * @return {@Link BluetoothDevice} the Bluetooth device
     */
    public BluetoothDevice getDevice() {
        return this.io.getDevice();
    }

    /**
     * Reading the device's signal strength RSSI value
     *
     * @param callback An {@link ActionCallback} that handles the returned RSSI value.
     */
    @Override
    public void readRssi(ActionCallback callback) {
        this.io.readRssi(callback);
    }

    /**
     * Read device's Battery information
     *
     * return {@link MiBand2BatteryInfo}
     */
    @Override
    public void getBatteryInfo(final BatteryInfoCallback callback) {
        ActionCallback ioCallback = new ActionCallback() {

            @Override
            public void onSuccess(Object data) {
                BluetoothGattCharacteristic characteristic = (BluetoothGattCharacteristic) data;
                //Log.d(TAG, "getBatteryInfo result " + Arrays.toString(characteristic.getValue()));
                if (MiBand2BatteryInfo.isBatteryInfo(characteristic)) {
                    MiBand2BatteryInfo info = MiBand2BatteryInfo.fromByteData(characteristic.getValue());
                    callback.onSuccess(info);
                } else {
                    callback.onFail(-1, "result format wrong!");
                }
            }

            @Override
            public void onFail(int errorCode, String msg) {
                callback.onFail(errorCode, msg);
            }
        };
        this.io.readCharacteristic(Profile.UUID_CHAR_6_BATTERY, ioCallback);
    }

    /**
     * Set the device's current date and time.
     *
     * @param calendar A {@link Calendar} object that indicates the date and time for the device.
     * @param callback An {@link ActionCallback} listener that handles notification on date change.
     */
    @Override
    public void setTime(Calendar calendar, final ActionCallback callback) {
        ActionCallback ioCallback = new ActionCallback() {
            @Override
            public void onSuccess(Object data) {
                BluetoothGattCharacteristic characteristic = (BluetoothGattCharacteristic) data;
                Log.d(TAG, "SetCurrentTime result " + Arrays.toString(characteristic.getValue()));
                Date currentTime = CalendarUtils.bytesToDate(characteristic.getValue());
                callback.onSuccess(currentTime);
            }

            @Override
            public void onFail(int errorCode, String msg) {
                callback.onFail(errorCode, msg);
            }
        };
        byte[] timeInBytes = TypeConversionUtils.getTimeBytes(calendar, TimeUnit.SECONDS);
        this.io.writeCharacteristic(Profile.UUID_CURRENT_TIME, timeInBytes, ioCallback);
    }

    /**
     * Get the device's current time.
     *
     * @param callback An {@link ActionCallback} listener that handles notification on the date.
     */
    @Override
    public void getCurrentTime(final ActionCallback callback) {
        ActionCallback ioCallback = new ActionCallback() {
            @Override
            public void onSuccess(Object data) {
                BluetoothGattCharacteristic characteristic = (BluetoothGattCharacteristic) data;
                Log.d(TAG, "Current time: " + Arrays.toString(characteristic.getValue()));
                Date currentTime = CalendarUtils.bytesToDate(characteristic.getValue());
                callback.onSuccess(currentTime);
            }

            @Override
            public void onFail(int errorCode, String msg) {
                callback.onFail(errorCode, msg);
            }
        };
        this.io.readCharacteristic(Profile.UUID_CURRENT_TIME, ioCallback);
    }

    /**
     * Prints Services and Characteristics available on the connected device.
     */
    @Override
    public void showServicesAndCharacteristics() {
        this.io.gatt.discoverServices();
        for (BluetoothGattService service : this.io.gatt.getServices()) {
            Log.d(TAG, "onServicesDiscovered:" + service.getUuid());

            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                Log.d(TAG, "  char:" + characteristic.getUuid());

                for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
                    Log.d(TAG, "    descriptor:" + descriptor.getUuid());
                }
            }
        }
    }

    /* USER INFORMATION METHODS */
    /**
     * Sets user information
     *
     * @param userInfo A {@link UserInfo} object that describes the user.
     */
    @Override
    public void setUserInfo(UserInfo userInfo, final ActionCallback callback) {
        byte[] userInfoBytes = userInfo.getBytes();
        ActionCallback actionCallback = new ActionCallback() {
            @Override
            public void onSuccess(Object data) {
                BluetoothGattCharacteristic characteristic = (BluetoothGattCharacteristic) data;
                String response = Arrays.toString(characteristic.getValue());
                Log.d(TAG, String.format("Set user info success: %s", response));
                callback.onSuccess(data);
            }

            @Override
            public void onFail(int errorCode, String msg) {
                Log.d(TAG, String.format("Set user info failed: %s", msg));
                callback.onFail(errorCode, msg);
            }
        };
        this.io.writeCharacteristic(Profile.UUID_CHAR_8_USER_SETTING, userInfoBytes, actionCallback);
    }

    /**
     * Retrieves the user's information. Currently not functional.
     *
     * @param callback
     */
    public void getUserSetting(final ActionCallback callback) {
        this.io.setNotifyListener(Profile.UUID_SERVICE_MILI, Profile.UUID_CHAR_8_USER_SETTING,
                new NotifyListener() {
            @Override
            public void onNotify(byte[] data) {
                Log.d(TAG, "Get user info " + Arrays.toString(data));
            }
        });
    }

    /* VIBRATION METHODS */
    /**
     * Set the vibration alert on the device.
     *
     * @param mode A {@link VibrationMode} object that determines the type of vibrations.
     */
    public void startVibration(VibrationMode mode) {
        byte[] protocal;
        switch (mode) {
            case VIBRATION_MESSAGE:
                protocal = Protocol.VIBRATION_MESSAGE;
                break;
            case VIBRATION_PHONE_CALL:
                protocal = Protocol.VIBRATION_PHONE;
                break;
            case VIBRATION_ONLY:
                protocal = Protocol.VIBRATION_ONLY;
                break;
            case VIBRATION_WITHOUT_LED:
                protocal = Protocol.VIBRATION_WITHOUT_LED;
                break;
            default:
                return;
        }
        this.io.writeCharacteristic(Profile.UUID_SERVICE_VIBRATION, Profile.UUID_CHAR_VIBRATION, protocal, null);
    }

    /**
     * Stop the vibration when VIBRATION_PHONE_CALL was activated.
     */
    public void stopVibration() {
        this.io.writeCharacteristic(Profile.UUID_SERVICE_VIBRATION, Profile.UUID_CHAR_VIBRATION, Protocol.STOP_VIBRATION, null);
    }

    /* REAL TIME STEPS NOTIFICATION METHODS */
    /**
     * Set up the real-time steps count notification listener.
     *
     * @param listener An {@link NotifyListener} listener that handles every step count update.
     */
    public void setRealtimeStepsNotifyListener(final RealtimeStepsNotifyListener listener) {
        this.io.setNotifyListener(Profile.UUID_SERVICE_MILI, Profile.UUID_CHAR_7_REALTIME_STEPS, new NotifyListener() {

            @Override
            public void onNotify(byte[] data) {
                if (data.length == 13) {
                    FitnessSample sample = new FitnessSample(data);
                    listener.onNotify(sample.getSteps());
                }
            }
        });
    }

    /**
     * Turn on real time step notification
     */
    public void enableRealtimeStepsNotify() {
        this.io.writeCharacteristic(Profile.UUID_CHAR_CONTROL_POINT, Protocol.ENABLE_REALTIME_STEPS_NOTIFY, null);
    }

    /**
     * Turn off real time step notification
     */
    public void disableRealtimeStepsNotify() {
        this.io.writeCharacteristic(Profile.UUID_CHAR_CONTROL_POINT, Protocol.DISABLE_REALTIME_STEPS_NOTIFY, null);
        this.io.stopNotifyListener(Profile.UUID_SERVICE_MILI, Profile.UUID_CHAR_7_REALTIME_STEPS);
    }

    /* HEART RATE METHODS*/
    public void setHeartRateScanListener(final HeartRateNotifyListener listener) {
        this.io.setNotifyListener(Profile.UUID_SERVICE_HEARTRATE, Profile.UUID_NOTIFICATION_HEARTRATE, new NotifyListener() {
            @Override
            public void onNotify(byte[] data) {
                Log.d(TAG, Arrays.toString(data));
                if (data.length == 2 && (data[0] == 6 || data[0] == 0)) {
                    int heartRate = data[1] & 0xFF;
                    listener.onNotify(heartRate);
                }
            }
        });
    }

    public void startHeartRateScan() {
        MiBand.this.io.writeCharacteristic(Profile.UUID_SERVICE_HEARTRATE, Profile.UUID_CHAR_HEARTRATE, Protocol.START_HEART_RATE_SCAN, null);
    }

    public void stopHeartRateScan() {
        MiBand.this.io.writeCharacteristic(Profile.UUID_SERVICE_HEARTRATE, Profile.UUID_CHAR_HEARTRATE, Protocol.STOP_HEART_RATE_SCAN, null);
    }

    /* REALTIME SENSOR DATA UPDATE */
    public void disableOneTimeHeartRateSensor() {
        this.io.writeCharacteristic(Profile.UUID_SERVICE_HEARTRATE,
                Profile.UUID_CHAR_HEARTRATE,
                Protocol.STOP_ONE_TIME_HEART_RATE, null);
    }

    public void disableContinuousHeartRateSensor() {
        this.io.writeCharacteristic(Profile.UUID_SERVICE_HEARTRATE,
                Profile.UUID_CHAR_HEARTRATE,
                Protocol.STOP_HEART_RATE_SCAN, null);
    }

    public void enableAccelerometerSensor() {
        this.io.writeCharacteristic(Profile.UUID_CHAR_1_SENSOR,
                Protocol.ENABLE_SENSOR_DATA_NOTIFY, null);
    }

    public void enableAccelerometerNotifications(final NotifyListener listener) {
        this.io.setNotifyListener(Profile.UUID_SERVICE_MILI, Profile.UUID_CHAR_1_SENSOR, new NotifyListener() {
            @Override
            public void onNotify(byte[] data) {
                listener.onNotify(data);
                Log.d(TAG, "Data " + Arrays.toString(data));
            }
        });
    }

    public void startHeartRateNotifications () {
        this.io.writeCharacteristic(Profile.UUID_SERVICE_HEARTRATE,
                Profile.UUID_CHAR_HEARTRATE,
                Protocol.START_HEART_RATE_SCAN, null);
    }

    public void startSensingNow () {
        // char_sensor.write(b'\x02')
        this.io.writeCharacteristic(Profile.UUID_CHAR_CONTROL_POINT, Protocol.START_SENSOR_FETCH, null);
    }


    /**
     *
     * @param listener
     */
    public void setNormalNotifyListener(NotifyListener listener) {
        this.io.setNotifyListener(Profile.UUID_SERVICE_MILI, Profile.UUID_CHAR_NOTIFICATION, listener);
    }

    /**
     * 重力感应器数据通知监听, 设置完之后需要另外使用 {@link MiBand#enableRealtimeStepsNotify} 开启 和
     *
     *
     * @param listener
     */
    public void setSensorDataNotifyListener(final NotifyListener listener) {
        this.io.setNotifyListener(Profile.UUID_SERVICE_MILI, Profile.UUID_CHAR_1_SENSOR, new NotifyListener() {

            @Override
            public void onNotify(byte[] data) {
                listener.onNotify(data);
            }
        });
    }

    /**
     * 开启重力感应器数据通知
     */
    public void enableSensorDataNotify() {
        this.io.writeCharacteristic(Profile.UUID_CHAR_CONTROL_POINT, Protocol.ENABLE_SENSOR_DATA_NOTIFY, null);
    }

    public void startNotifyingSensorData() {
        this.io.writeCharacteristic(Profile.UUID_CHAR_CONTROL_POINT, Protocol.START_SENSOR_FETCH, null);
    }

    /**
     * 关闭重力感应器数据通知
     */
    public void disableSensorDataNotify() {
        this.io.writeCharacteristic(Profile.UUID_CHAR_CONTROL_POINT, Protocol.DISABLE_SENSOR_DATA_NOTIFY, null);
    }

    /* ACTIVITY FETCHING METHODS */
    /**
     * Fetch steps count data from the device.
     * @param startTime Determines the start time of the activity data that will be fetched.
     * @param fetchActivityListener This listener will take care of the data once the MI Band
     *                              completed the request.
     */
    @Override
    public void fetchActivityData(GregorianCalendar startTime,
                                  FetchActivityListener fetchActivityListener) {
        if (this.io.isConnected()) {
            OperationFetchActivities operation = new OperationFetchActivities(fetchActivityListener, this
                    .handler);
            operation.perform(this.io, startTime);
        }
    }

    /* STATIC HELPER METHODS */
    public static boolean isThisTheDevice(BluetoothDevice device, MiBand2Profile profile) {
        String name = device.getName();
        String address = device.getAddress();
        if (name != null && address != null) {
            return name.startsWith(MiBand.MI_BAND_PREFIX) && address.equals(profile.getAddress());
        } else {
            return false;
        }
    }

    public static boolean isThisDeviceCompatible(BluetoothDevice device) {
        String name = device.getName();
        if (name != null) {
            return name.startsWith(MI_BAND_PREFIX);
        } else {
            return false;
        }
    }

    public static void publishDeviceFound(BluetoothDevice device, ScanResult result) {
        Log.d(TAG,"MiBand found! name: " + device.getName()
                + ", uuid:" + device.getUuids()
                + ", add:" + device.getAddress()
                + ", type:" + device.getType()
                + ", bondState:" + device.getBondState()
                + ", rssi:" + result.getRssi());
    }

    public static void publishDevice(BluetoothDevice device) {
        Log.d(TAG,"MiBand 2 connected. Name: " + device.getName()
                + ", uuid:" + device.getUuids()
                + ", add:" + device.getAddress()
                + ", type:" + device.getType()
                + ", bondState:" + device.getBondState()
                + ".");
    }

}
