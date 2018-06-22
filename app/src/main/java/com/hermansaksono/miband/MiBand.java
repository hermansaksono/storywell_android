package com.hermansaksono.miband;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.hermansaksono.miband.listeners.HeartRateNotifyListener;
import com.hermansaksono.miband.listeners.NotifyListener;
import com.hermansaksono.miband.listeners.RealtimeStepsNotifyListener;
import com.hermansaksono.miband.model.BatteryInfo;
import com.hermansaksono.miband.model.FitnessSample;
import com.hermansaksono.miband.model.LedColor;
import com.hermansaksono.miband.model.Profile;
import com.hermansaksono.miband.model.Protocol;
import com.hermansaksono.miband.utils.CalendarUtils;
import com.hermansaksono.miband.model.UserInfo;
import com.hermansaksono.miband.utils.TypeConversionUtils;
import com.hermansaksono.miband.model.VibrationMode;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

public class MiBand {

    public static final String MI_BAND_PREFIX = "MI Band" ;
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

    public void getCurrentTime(final ActionCallback callback) {
        ActionCallback ioCallback = new ActionCallback() {
            @Override
            public void onSuccess(Object data) {
                BluetoothGattCharacteristic characteristic = (BluetoothGattCharacteristic) data;
                //Log.d(TAG, "getCurrentTime result " + Arrays.toString(characteristic.getValue()));
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
            case VIBRATION_WITH_LED:
                protocal = Protocol.VIBRATION_WITH_LED;
                break;
            case VIBRATION_10_TIMES_WITH_LED:
                protocal = Protocol.VIBRATION_10_TIMES_WITH_LED;
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
     * 停止以模式Protocol.VIBRATION_10_TIMES_WITH_LED 开始的震动
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
        this.io.setNotifyListener(Profile.UUID_SERVICE_MILI, Profile.UUID_CHAR_SENSOR_DATA, new NotifyListener() {

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
                //Log.d(TAG, Arrays.toString(data));
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



    //this.io.writeCharacteristic(Profile.UUID_CHAR_CONTROL_POINT, Protocol.ENABLE_SENSOR_DATA_NOTIFY, null);
    // characteristicActivityData = getCharacteristic(MiBand2Service.UUID_CHARACTERISTIC_5_ACTIVITY_DATA);
    // characteristicFetch = getCharacteristic(MiBand2Service.UUID_UNKNOWN_CHARACTERISTIC4);
    // builder.write(characteristicFetch, BLETypeConversions.join(new byte[]
    // { MiBand2Service.COMMAND_ACTIVITY_DATA_START_DATE,
    // MiBand2Service.COMMAND_ACTIVITY_DATA_TYPE_ACTIVTY },
    // getSupport().getTimeBytes(sinceWhen, TimeUnit.MINUTES)));
    // builder.notify(characteristicActivityData, true);
    // builder.write(characteristicFetch, new byte[] { MiBand2Service.COMMAND_FETCH_DATA});
	/**
		D/BluetoothGatt: setCharacteristicNotification() - uuid: 00000005-0000-3512-2118-0009af100700 enable: false
		D/nodomain.freeyourgadget.gadgetbridge.service.btle.BtLEQueue: descriptor write: 00002902-0000-1000-8000-00805f9b34fb (success)
		D/BluetoothGatt: setCharacteristicNotification() - uuid: 00000004-0000-3512-2118-0009af100700 enable: true
		D/nodomain.freeyourgadget.gadgetbridge.service.btle.BtLEQueue: descriptor write: 00002902-0000-1000-8000-00805f9b34fb (success)
		D/nodomain.freeyourgadget.gadgetbridge.service.btle.actions.WriteAction: writing to characteristic: 00000004-0000-3512-2118-0009af100700: 0x01 0x01 0xe2 0x07 0x06 0x15 0x14 0x2f 0x00 0xec
		D/BluetoothGatt: setCharacteristicNotification() - uuid: 00000005-0000-3512-2118-0009af100700 enable: true
		D/nodomain.freeyourgadget.gadgetbridge.service.btle.BtLEQueue: characteristic changed: 00000004-0000-3512-2118-0009af100700 value: 0x10 0x01 0x01 0xc2 0x03 0x00 0x00 0xe2 0x07 0x06 0x15 0x15 0x2f 0x00 0xf0
		D/nodomain.freeyourgadget.gadgetbridge.service.btle.actions.WriteAction: writing to characteristic: 00000004-0000-3512-2118-0009af100700: 0x02
		D/nodomain.freeyourgadget.gadgetbridge.service.btle.BtLEQueue: characteristic changed: 00000005-0000-3512-2118-0009af100700 value: 0x00 0x70 0x00 0x00 0xff 0x70 0x00 0x00 0xff 0x7a 0x14 0x00 0xff 0x7a 0x14 0x00 0xff
		
		D/BluetoothGatt: setCharacteristicNotification() - uuid: 00000004-0000-3512-2118-0009af100700 enable: false
		D/BluetoothGatt: setCharacteristicNotification() - uuid: 00000005-0000-3512-2118-0009af100700 enable: false
		*/

    public void fetchActivityData() {
        Calendar dummyDate = getDummyDate();
        GregorianCalendar sinceWhen = (GregorianCalendar) dummyDate;
        Log.d(TAG, "fetching data from " + sinceWhen.getTime().toString());
        this.io.setNotifyListener(Profile.UUID_SERVICE_MILI, Profile.UUID_CHAR_4_FETCH, new NotifyListener() {
            @Override
            public void onNotify(byte[] data) {
                //Log.d(TAG + "-fetch", Arrays.toString(data));
            }
        });
        byte[] paramStartTime = TypeConversionUtils.getTimeBytes(sinceWhen, TimeUnit.MINUTES);
        byte[] paramFetchCommand = TypeConversionUtils.join(Protocol.COMMAND_ACTIVITY_PARAMS, paramStartTime);
		
		Log.d(TAG, " param command:" + Arrays.toString(paramFetchCommand));
		//paramFetchCommand = Protocol.COMMAND_FETCH_ACTIVITY;
        this.io.writeCharacteristic(Profile.UUID_CHAR_4_FETCH, paramFetchCommand, null);
        this.io.setNotifyListener(Profile.UUID_SERVICE_MILI, Profile.UUID_CHAR_5_ACTIVITY, new NotifyListener() {
            @Override
            public void onNotify(byte[] data) {
                Log.d(TAG + "-fitness", Arrays.toString(data));
            }
        });
        this.io.writeCharacteristic(Profile.UUID_CHAR_4_FETCH, Protocol.COMMAND_ACTIVITY_FETCH, null);
    }

    boolean isParamSent = false;
    boolean isListening = false;
    Handler myHandler;

    public void startFetchingActivityData() {
        //this.io.stopNotifyListener(Profile.UUID_SERVICE_MILI, Profile.UUID_CHAR_5_ACTIVITY);
        this.io.setNotifyListener(Profile.UUID_SERVICE_MILI, Profile.UUID_CHAR_4_FETCH, new NotifyListener() {
            @Override
            public void onNotify(byte[] data) {
                //Log.d(TAG + "-fetch", Arrays.toString(data));
            }
        });
        myHandler = new Handler();
        myHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                sendCommandParams();
            }
        },1000);
    }

    private void sendCommandParams() {
        Calendar dummyDate = getDummyDate();
        GregorianCalendar sinceWhen = (GregorianCalendar) dummyDate;
        byte[] paramStartTime = TypeConversionUtils.getTimeBytes(sinceWhen, TimeUnit.MINUTES);
        //byte[] paramFetchCommand = Protocol.COMMAND_FETCH_ACTIVITY;
        byte[] paramFetchCommand = TypeConversionUtils.join(Protocol.COMMAND_ACTIVITY_PARAMS, paramStartTime);

        Log.d(TAG, "fetching data from " + sinceWhen.getTime().toString());
        Log.d(TAG, "param command:" + Arrays.toString(paramFetchCommand));
        this.io.writeCharacteristic(Profile.UUID_CHAR_4_FETCH, paramFetchCommand, null);

        myHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                initListeningActivityData();
            }
        }, 1000);
    }

    public void initListeningActivityData() {
        this.io.setNotifyListener(Profile.UUID_SERVICE_MILI, Profile.UUID_CHAR_5_ACTIVITY, new NotifyListener() {
            @Override
            public void onNotify(byte[] data) {
                Log.d(TAG + "-fitness", Arrays.toString(data));
            }
        });

        myHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startListeningActivityData();
            }
        }, 1000);
    }

    public void startListeningActivityData() {
        this.io.writeCharacteristic(Profile.UUID_CHAR_4_FETCH, Protocol.COMMAND_ACTIVITY_FETCH, null);
    }

    public void enableActivityDataNotify() {
        //this.io.writeCharacteristic(Profile.UUID_CHAR_CONTROL_POINT, Protocol.ENABLE_SENSOR_DATA_NOTIFY, null);
    }

    public void disableActivityDataNotify() {
        //this.io.writeCharacteristic(Profile.UUID_CHAR_CONTROL_POINT, Protocol.DISABLE_SENSOR_DATA_NOTIFY, null);
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
        /*
        ActionCallback ioCallback = new ActionCallback() {
            @Override
            public void onSuccess(Object data) {
                BluetoothGattCharacteristic characteristic = (BluetoothGattCharacteristic) data;
                Log.d(TAG, "User setting: " + Arrays.toString(characteristic.getValue()));
                callback.onSuccess(data);
            }

            @Override
            public void onFail(int errorCode, String msg) {
                callback.onFail(errorCode, msg);
            }
        };
        this.io.readCharacteristic(Profile.UUID_CHAR_8_USER_SETTING, ioCallback);
        */
        UserInfo user = new UserInfo(99999, UserInfo.GENDER_MALE, 37, 166, 70,"Herman", 1);
        setUserInfo(user);
        this.io.setNotifyListener(Profile.UUID_SERVICE_MILI, Profile.UUID_CHAR_8_USER_SETTING, new NotifyListener() {
            @Override
            public void onNotify(byte[] data) {
                Log.d(TAG + "-user", Arrays.toString(data));
            }
        });
    }

    /**
     * 设置用户信息
     *
     * @param userInfo
     */
    public void setUserInfo(UserInfo userInfo) {
        BluetoothDevice device = this.io.getDevice();
        byte[] data = userInfo.getBytes(device.getAddress());
        this.io.writeCharacteristic(Profile.UUID_CHAR_USER_INFO, data, null);
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

    /* DATE HELPER METHODS */
    private static Date getTodayDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2017);  // TODO UPDATE THIS to reflect the current day
        calendar.set(Calendar.MONTH, Calendar.JUNE);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private static Calendar getDummyDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2018);
        calendar.set(Calendar.MONTH, Calendar.JUNE);
        calendar.set(Calendar.DAY_OF_MONTH, 22);
        calendar.set(Calendar.HOUR_OF_DAY, 1);
        calendar.set(Calendar.MINUTE, 48);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

}
