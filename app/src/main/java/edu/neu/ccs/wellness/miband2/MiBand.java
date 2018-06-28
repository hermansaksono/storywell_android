package edu.neu.ccs.wellness.miband2;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.util.Log;

import edu.neu.ccs.wellness.miband2.listeners.HeartRateNotifyListener;
import edu.neu.ccs.wellness.miband2.listeners.NotifyListener;
import edu.neu.ccs.wellness.miband2.listeners.RealtimeStepsNotifyListener;
import edu.neu.ccs.wellness.miband2.model.BatteryInfo;
import edu.neu.ccs.wellness.miband2.model.FitnessSample;
import edu.neu.ccs.wellness.miband2.model.LedColor;
import edu.neu.ccs.wellness.miband2.model.MiBandProfile;
import edu.neu.ccs.wellness.miband2.model.Profile;
import edu.neu.ccs.wellness.miband2.model.Protocol;
import edu.neu.ccs.wellness.miband2.utils.CalendarUtils;
import edu.neu.ccs.wellness.miband2.model.UserInfo;
import edu.neu.ccs.wellness.miband2.utils.TypeConversionUtils;
import edu.neu.ccs.wellness.miband2.model.VibrationMode;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

public class MiBand {

    public static final String MI_BAND_PREFIX = "MI Band" ;
    public static final int ACTIVITY_PACKET_LENGTH = 17;
    private static final String TAG = "miband-android";

    private Context context;
    private BluetoothIO io;


    public MiBand(Context context) {
        this.context = context;
        this.io = new BluetoothIO();
    }

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

    /**
     * 连接指定的手环
     *
     * @param callback
     */
    public void connect(BluetoothDevice device, final ActionCallback callback) {
        this.io.connect(context, device, callback);
    }

    public void disconnect() {
        this.io.disconnect();
    }

    public void setDisconnectedListener(NotifyListener disconnectedListener) {
        this.io.setDisconnectedListener(disconnectedListener);
    }

    /**
     * 和手环配对, 实际用途未知, 不配对也可以做其他的操作
     *
     * return data = null
     */
    public void pair(final ActionCallback callback) {
        ActionCallback ioCallback = new ActionCallback() {

            @Override
            public void onSuccess(Object data) {
                BluetoothGattCharacteristic characteristic = (BluetoothGattCharacteristic) data;
                Log.d(TAG, "pair result " + Arrays.toString(characteristic.getValue()));
                if (characteristic.getValue().length == 1 && characteristic.getValue()[0] == 2) {
                    callback.onSuccess(null);
                } else {
                    callback.onFail(-1, "respone values no succ!");
                }
            }

            @Override
            public void onFail(int errorCode, String msg) {
                callback.onFail(errorCode, msg);
            }
        };
        this.io.writeAndRead(Profile.UUID_CHAR_PAIR, Protocol.PAIR, ioCallback);
    }

    public BluetoothDevice getDevice() {
        return this.io.getDevice();
    }

    /**
     * 读取和连接设备的信号强度RSSI值
     *
     * param callback
     * eturn data : int, rssi值
     */
    public void readRssi(ActionCallback callback) {
        this.io.readRssi(callback);
    }

    /**
     * 读取手环电池信息
     *
     * return {@link BatteryInfo}
     */
    public void getBatteryInfo(final ActionCallback callback) {
        ActionCallback ioCallback = new ActionCallback() {

            @Override
            public void onSuccess(Object data) {
                BluetoothGattCharacteristic characteristic = (BluetoothGattCharacteristic) data;
                //Log.d(TAG, "getBatteryInfo result " + Arrays.toString(characteristic.getValue()));
                if (characteristic.getValue().length >= 2) {
                    BatteryInfo info = BatteryInfo.fromByteData(characteristic.getValue());
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

    public void setTime(Calendar cal, final ActionCallback callback) {
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
        byte[] timeInBytes = TypeConversionUtils.getTimeBytes(cal, TimeUnit.SECONDS);
        Log.d(TAG, "time in bytes" + Arrays.toString(timeInBytes));
        this.io.writeCharacteristic(Profile.UUID_CURRENT_TIME, timeInBytes, ioCallback);
    }

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
     * 让手环震动
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
     * 停止以模式Protocol.VIBRATION_PHONE 开始的震动
     */
    public void stopVibration() {
        this.io.writeCharacteristic(Profile.UUID_SERVICE_VIBRATION, Profile.UUID_CHAR_VIBRATION, Protocol.STOP_VIBRATION, null);
    }

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
        this.io.writeCharacteristic(Profile.UUID_CHAR_CONTROL_POINT, Protocol.COMMAND_SENSOR_FETCH, null);
    }

    /**
     * 关闭重力感应器数据通知
     */
    public void disableSensorDataNotify() {
        this.io.writeCharacteristic(Profile.UUID_CHAR_CONTROL_POINT, Protocol.DISABLE_SENSOR_DATA_NOTIFY, null);
    }

    /**
     * 实时步数通知监听器, 设置完之后需要另外使用 {@link MiBand#enableRealtimeStepsNotify} 开启 和
     *
     *
     * @param listener
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
     * 开启实时步数通知
     */
    public void enableRealtimeStepsNotify() {
        this.io.writeCharacteristic(Profile.UUID_CHAR_CONTROL_POINT, Protocol.ENABLE_REALTIME_STEPS_NOTIFY, null);
    }

    /**
     * 关闭实时步数通知
     */
    public void disableRealtimeStepsNotify() {
        this.io.writeCharacteristic(Profile.UUID_CHAR_CONTROL_POINT, Protocol.DISABLE_REALTIME_STEPS_NOTIFY, null);
        this.io.stopNotifyListener(Profile.UUID_SERVICE_MILI, Profile.UUID_CHAR_7_REALTIME_STEPS);
    }

    /* ACTIVITY FETCHING METHODS */
    public void disableFitnessDataNotify() {
        this.io.stopNotifyListener(Profile.UUID_SERVICE_MILI, Profile.UUID_CHAR_5_ACTIVITY);
    }

    public void enableFetchUpdatesNotify() {
        //
        this.io.setNotifyListener(Profile.UUID_SERVICE_MILI, Profile.UUID_CHAR_4_FETCH, new NotifyListener() {
            @Override
            public void onNotify(byte[] data) {
                //Log.d(TAG + "-fetch", Arrays.toString(data)); // DO NOTHING
            }
        });
    }

    public void sendCommandParams(GregorianCalendar sinceWhen) {
        byte[] paramStartTime = TypeConversionUtils.getTimeBytes(sinceWhen, TimeUnit.MINUTES);
        byte[] paramFetchCommand = TypeConversionUtils.join(Protocol.COMMAND_ACTIVITY_PARAMS, paramStartTime);

        Log.d(TAG, "Fetching activities from " + sinceWhen.getTime().toString() + " param commands " + Arrays.toString(paramFetchCommand));
        this.io.writeCharacteristic(Profile.UUID_CHAR_4_FETCH, paramFetchCommand, null);
    }

    public void enableFitnessDataNotify(NotifyListener listener) {
        this.io.setNotifyListener(Profile.UUID_SERVICE_MILI, Profile.UUID_CHAR_5_ACTIVITY, listener);

    }

    public void startNotifyingFitnessData() {
        this.io.writeCharacteristic(Profile.UUID_CHAR_4_FETCH, Protocol.COMMAND_ACTIVITY_FETCH, null);
    }

    /**
     * 设置led灯颜色
     */
    public void setLedColor(LedColor color) {
        byte[] protocal;
        switch (color) {
            case RED:
                protocal = Protocol.SET_COLOR_RED;
                break;
            case BLUE:
                protocal = Protocol.SET_COLOR_BLUE;
                break;
            case GREEN:
                protocal = Protocol.SET_COLOR_GREEN;
                break;
            case ORANGE:
                protocal = Protocol.SET_COLOR_ORANGE;
                break;
            default:
                return;
        }
        this.io.writeCharacteristic(Profile.UUID_CHAR_CONTROL_POINT, protocal, null);
    }


    public void getUserSetting(final ActionCallback callback) {
        this.io.setNotifyListener(Profile.UUID_SERVICE_MILI, Profile.UUID_CHAR_8_USER_SETTING, new NotifyListener() {
            @Override
            public void onNotify(byte[] data) {
                Log.d(TAG, "Get user info " + Arrays.toString(data));
            }
        });
    }

    /**
     * 设置用户信息
     *
     * @param userInfo
     */
    public void setUserInfo(UserInfo userInfo) {
        byte[] userInfoBytes = userInfo.getBytes();
        ActionCallback actionCallback = new ActionCallback() {
            @Override
            public void onSuccess(Object data) {
                BluetoothGattCharacteristic characteristic = (BluetoothGattCharacteristic) data;
                String response = Arrays.toString(characteristic.getValue());
                Log.d(TAG, String.format("Set user info success: %s", response));
            }

            @Override
            public void onFail(int errorCode, String msg) {
                Log.d(TAG, String.format("Set user info failed: %s", msg));
            }
        };
        this.io.writeCharacteristic(Profile.UUID_CHAR_8_USER_SETTING, userInfoBytes, actionCallback);
    }

    public void showServicesAndCharacteristics() {
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

    /* STATIC HELPER METHODS */
    public static boolean isThisTheDevice(BluetoothDevice device, MiBandProfile profile) {
        String name = device.getName();
        String address = device.getAddress();
        if (name != null && address != null) {
            return name.startsWith(MiBand.MI_BAND_PREFIX) && address.equals(profile.getAddress());
        } else {
            return false;
        }
    }

    public static void publishDeviceFound(BluetoothDevice device, ScanResult result) {
        Log.d(TAG,"MiBand found! name:" + device.getName() + ",uuid:"
                + device.getUuids() + ",add:"
                + device.getAddress() + ",type:"
                + device.getType() + ",bondState:"
                + device.getBondState() + ",rssi:" + result.getRssi());
    }

}
