package edu.neu.ccs.wellness.storytelling;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import edu.neu.ccs.wellness.fitness.storage.onDataUploadListener;
import edu.neu.ccs.wellness.storytelling.sync.FitnessSyncReceiver;
import edu.neu.ccs.wellness.storytelling.sync.FitnessSyncService;
import edu.neu.ccs.wellness.trackers.BatteryInfo;
import edu.neu.ccs.wellness.trackers.GenericScanner;
import edu.neu.ccs.wellness.trackers.callback.ActionCallback;
import edu.neu.ccs.wellness.trackers.callback.BatteryInfoCallback;
import edu.neu.ccs.wellness.trackers.miband2.MiBand;
import edu.neu.ccs.wellness.trackers.callback.FetchActivityListener;
import edu.neu.ccs.wellness.trackers.miband2.MiBandScanner;
import edu.neu.ccs.wellness.trackers.callback.HeartRateNotifyListener;
import edu.neu.ccs.wellness.trackers.callback.NotifyListener;
import edu.neu.ccs.wellness.trackers.callback.RealtimeStepsNotifyListener;
import edu.neu.ccs.wellness.trackers.miband2.MiBand2Profile;
import edu.neu.ccs.wellness.trackers.UserInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import edu.neu.ccs.wellness.fitness.storage.FitnessRepository;
import edu.neu.ccs.wellness.trackers.miband2.operations.MonitorSensorData;
import edu.neu.ccs.wellness.people.Person;

public class FitnessSyncActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private GenericScanner miBand2Scanner;
    private MiBand miBand;
    private String[] permission = {Manifest.permission.ACCESS_COARSE_LOCATION};
    private Button btnFindDevices;
    private MonitorSensorData sensorMonitor;
    private MiBand2Profile profile = new MiBand2Profile("F4:31:FA:D1:D6:90");
    //private MiBand2Profile profile = new MiBand2Profile("EF:2B:B8:7B:76:F0");
    //private MiBand2Profile profile = new MiBand2Profile("FE:3D:67:43:B8:F5");

    private boolean isRealtimeStepsActive = false;
    private boolean isHeartRateNotificationActive = false;

    private FitnessRepository repo = new FitnessRepository();

    final ScanCallback scanCallback = new ScanCallback(){
        @Override
        public void onScanResult(int callbackType, ScanResult result){
            BluetoothDevice device = result.getDevice();
            if (MiBand.isThisTheDevice(device, profile)) {
                miBand2Scanner.stopScan(scanCallback);
                MiBand.publishDeviceFound(device, result);
                connectToMiBand(device);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // FitnessSyncReceiver.scheduleFitnessSync(this, FitnessSyncReceiver.SYNC_INTERVAL);
        // FitnessSyncService.scheduleFitnessSync(FitnessSyncActivity.this);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.firstrun_ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        tryRequestPermission();

        setContentView(R.layout.activity_fitness_sync);

        this.miBand = new MiBand();

        this.btnFindDevices = findViewById(R.id.button2);
        this.btnFindDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startBluetoothScan();
            }
        });

        findViewById(R.id.button_disconnect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                disconnectMiBand();
            }
        });

        findViewById(R.id.button_pair).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doPair();
            }
        });

        findViewById(R.id.button_vibrate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doVibration();
            }
        });

        findViewById(R.id.button_battery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getBatteryInfo();
            }
        });

        findViewById(R.id.button_get_time).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getCurrentTime();
            }
        });

        findViewById(R.id.button_sync_time).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTime();
            }
        });

        findViewById(R.id.button_monitor_steps).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                monitorRealTimeSteps();
            }
        });

        findViewById(R.id.button_realtime_heartrate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                monitorRealtimeHeartRate();
            }
        });

        findViewById(R.id.button_fetch_activities).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchActivityFromDate();
            }
        });

        findViewById(R.id.button_sensor).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                monitorRealtimeSensor();
            }
        });

        findViewById(R.id.button_set_user_info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setUserData();
            }
        });
    }

    private void tryRequestPermission() {
        if (!isCoarseLocationAllowed()) {
            ActivityCompat.requestPermissions(this, permission,
                    PERMISSION_REQUEST_COARSE_LOCATION);
        }
    }

    private boolean isCoarseLocationAllowed() {
        int permissionRecordAudio = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        return permissionRecordAudio == PackageManager.PERMISSION_GRANTED;
    }

    private void startBluetoothScan() {
        this.miBand2Scanner = new MiBandScanner(getApplicationContext());
        this.miBand2Scanner.startScan(scanCallback);
    }

    private void connectToMiBand(BluetoothDevice device) {
        this.miBand.connect(device, getApplicationContext(), new ActionCallback() {
            @Override
            public void onSuccess(Object data){
                doPostConnectOperations();
            }

            @Override
            public void onFail(int errorCode, String msg){
                return;
            }
        });
    }

    private void disconnectMiBand() {
        if (this.miBand != null) {
            this.miBand.disconnect();
            this.btnFindDevices.setText("Get MiBand");
        }
    }

    private void doPostConnectOperations() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                doAuthAndPair();
                btnFindDevices.setText("Connected");
            }
        });
    }

    private void doAuthAndPair() {
        boolean isPaired = miBand.getDevice().getBondState() != BluetoothDevice.BOND_NONE;
        if (isPaired == false) {
            this.doAuth();
        } else {
            this.doPair();
        }
    }

    private void doAuth() {
        this.miBand.auth(new ActionCallback() {
            @Override
            public void onSuccess(Object data){
                doPostAuth();
                // DO NOTHING
            }
            @Override
            public void onFail(int errorCode, String msg){
                // DO NOTHING
            }
        });
    }

    private void doPostAuth() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                doPair();
            }
        });
    }

    private void doPair() {
        this.miBand.pair(new ActionCallback() {
            @Override
            public void onSuccess(Object data){
                Log.d("mi-band", String.format("Paired: %s", data.toString()));
            }
            @Override
            public void onFail(int errorCode, String msg){
                Log.d("mi-band", String.format("Pair failed (%d): %s", errorCode, msg));
            }
        });
    }

    private void getBatteryInfo() {
        this.miBand.getBatteryInfo(new BatteryInfoCallback() {
            @Override
            public void onSuccess(BatteryInfo batteryInfo){
                Log.d("mi-band", "Battery: " + batteryInfo.toString());
            }
            @Override
            public void onFail(int errorCode, String msg){
                Log.d("mi-band" , "Battery info failed: " + msg);
            }
        });
    }

    private void getCurrentTime() {
        this.miBand.getCurrentTime(new ActionCallback() {
            @Override
            public void onSuccess(Object data){
                Date currentTime = (Date) data;
                Log.d("mi-band", "Current time: " + currentTime.toString());
            }
            @Override
            public void onFail(int errorCode, String msg){
                Log.d("mi-band" , "Get current time failed: " + msg);
            }
        });
    }

    private void setTime() {
        this.miBand.setTime(getCurrentDate(), new ActionCallback() {
            @Override
            public void onSuccess(Object data){
                Log.d("mi-band", "Set current time: " + data.toString());
            }
            @Override
            public void onFail(int errorCode, String msg){
                Log.d("mi-band" , "Set current time failed: " + msg);
            }
        });
    }

    private void setUserData() {
        UserInfo userInfo = new UserInfo(
                1,
                UserInfo.BIOLOGICAL_SEX_FEMALE,
                37,
                166,
                72,
                "Herbert",
                1);
        this.miBand.setUserInfo(userInfo, new ActionCallback() {
            @Override
            public void onSuccess(Object data){
                Log.d("mi-band", String.format("Set up success: %s", data.toString()));
            }
            @Override
            public void onFail(int errorCode, String msg){
                Log.d("mi-band", String.format("Set up failed (%d): %s", errorCode, msg));
            }
        });
    }

    private void doVibration() {
        this.miBand.doOneVibration();
    }

    private void monitorRealTimeSteps() {
        this.miBand.startRealtimeStepsNotification(new RealtimeStepsNotifyListener() {
            @Override
            public void onNotify(int steps){
                Log.d("mi-band", String.format("Steps: %d", steps));
            }
        });
    }

    private void monitorRealtimeHeartRate() {
        this.miBand.setHeartRateScanListener(new HeartRateNotifyListener() {
            @Override
            public void onNotify(int heartRate)
            {
                Log.d("mi-band", "Heart rate: "+ heartRate);
            }
        });
    }

    private void monitorRealtimeSensor() {
        if (this.sensorMonitor == null) {
            this.sensorMonitor = new MonitorSensorData();
            this.sensorMonitor.connect(getApplicationContext(), profile, new NotifyListener() {
                @Override
                public void onNotify(byte[] data) {
                    Log.d("mi-band", "Sensor: "+ Arrays.toString(data));
                }
            });
        } else {
            this.sensorMonitor.disconnect();
            this.sensorMonitor = null;
        }
    }

    /* ACTIVITY FETCHING METHODS */
    private void fetchActivityFromDate() {
        GregorianCalendar startDate = (GregorianCalendar) getDummyDate();
        FetchActivityListener fetchActivityListener = new FetchActivityListener() {

            @Override
            public void OnFetchComplete(Calendar startDate, int expectedSamples, List<Integer> steps) {
                insertIntradayStepsToRepo(startDate, steps);
            }

            @Override
            public void OnFetchProgress(int index, int numData) {

            }
        };
        /*
        FetchActivityFromDate fetchActivityFromDate = new FetchActivityFromDate(profile,
                fetchActivityListener);
        fetchActivityFromDate.perform(getApplicationContext(), startDate);*/
        this.miBand.fetchActivityData(startDate, fetchActivityListener);
    }

    private void insertIntradayStepsToRepo(Calendar startDate, List<Integer> steps) {
        final Person man = new Person(4, "Herman", "P");
        final Date date = startDate.getTime();

        repo.insertIntradaySteps(man, startDate.getTime(), steps, new onDataUploadListener() {
            @Override
            public void onSuccess() {
                updateDailyFitness(man, date);
            }

            @Override
            public void onFailed() {

            }
        });
    }

    private void updateDailyFitness(Person man, Date date) {
        repo.updateDailyFitness(man, date, new onDataUploadListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailed() {

            }
        });
    }

    /* HELPER METHODS */
    private static Calendar getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getDefault());
        return calendar;
    }

    private static Calendar getDummyDate() {
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTimeZone(TimeZone.getDefault());
        calendar.set(Calendar.YEAR, 2018);
        calendar.set(Calendar.MONTH, Calendar.JULY);
        calendar.set(Calendar.DAY_OF_MONTH, 11);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }
}
