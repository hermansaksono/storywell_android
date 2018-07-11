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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import edu.neu.ccs.wellness.miband2.ActionCallback;
import edu.neu.ccs.wellness.miband2.MiBand;
import edu.neu.ccs.wellness.miband2.listeners.FetchActivityListener;
import edu.neu.ccs.wellness.miband2.listeners.HeartRateNotifyListener;
import edu.neu.ccs.wellness.miband2.listeners.NotifyListener;
import edu.neu.ccs.wellness.miband2.listeners.RealtimeStepsNotifyListener;
import edu.neu.ccs.wellness.miband2.model.BatteryInfo;
import edu.neu.ccs.wellness.miband2.model.MiBandProfile;
import edu.neu.ccs.wellness.miband2.model.UserInfo;
import edu.neu.ccs.wellness.miband2.model.VibrationMode;
import edu.neu.ccs.wellness.miband2.operations.MonitorRealtimeSteps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import edu.neu.ccs.wellness.fitness.interfaces.FitnessSample;
import edu.neu.ccs.wellness.fitness.storage.FitnessRepository;
import edu.neu.ccs.wellness.fitness.storage.OneDayFitnessSample;
import edu.neu.ccs.wellness.miband2.operations.MonitorRealtimeHeartRate;
import edu.neu.ccs.wellness.miband2.operations.MonitorSensorData;
import edu.neu.ccs.wellness.people.Person;
import edu.neu.ccs.wellness.utils.WellnessDate;

public class FitnessSyncActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private MiBand miBand;
    private String[] permission = {Manifest.permission.ACCESS_COARSE_LOCATION};
    private Button btnFindDevices;
    private MonitorRealtimeSteps stepsMonitor;
    private MonitorRealtimeHeartRate hrMonitor;
    private MonitorSensorData sensorMonitor;
    //private MiBandProfile profile = new MiBandProfile("F4:31:FA:D1:D6:90");
    //private MiBandProfile profile = new MiBandProfile("EF:2B:B8:7B:76:F0");
    private MiBandProfile profile = new MiBandProfile("FE:3D:67:43:B8:F5");

    final ScanCallback scanCallback = new ScanCallback(){
        @Override
        public void onScanResult(int callbackType, ScanResult result){
            BluetoothDevice device = result.getDevice();
            if (MiBand.isThisTheDevice(device, profile)) {
                MiBand.stopScan(scanCallback);
                MiBand.publishDeviceFound(device, result);
                connectToMiBand(device);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.firstrun_ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        tryRequestPermission();

        setContentView(R.layout.activity_fitness_sync);

        this.miBand = new MiBand(this);

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
        MiBand.startScan(scanCallback);
    }

    private void connectToMiBand(BluetoothDevice device) {
        this.miBand.connect(device, new ActionCallback() {
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

    private void doPostConnectOperations() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                doPair();
                btnFindDevices.setText("Connected");
            }
        });
    }

    private void disconnectMiBand() {
        if (this.miBand != null) {
            this.miBand.disconnect();
            this.btnFindDevices.setText("Get MiBand");
        }
    }

    private void doPair() {
        boolean isPaired = miBand.getDevice().getBondState() != BluetoothDevice.BOND_NONE;
        this.miBand.pair(isPaired, new ActionCallback() {
            @Override
            public void onSuccess(Object data){
                //Log.d("SWELL", String.format("Pair success: %s", data.toString()));
            }
            @Override
            public void onFail(int errorCode, String msg){
                //Log.d("SWELL", String.format("Pair failed (%d): %s", errorCode, msg));
            }
        });
    }

    private void getBatteryInfo() {
        this.miBand.getBatteryInfo(new ActionCallback() {
            @Override
            public void onSuccess(Object data){
                BatteryInfo info = (BatteryInfo) data;
                Log.d("SWELL", "Battery: " + info.toString());
            }
            @Override
            public void onFail(int errorCode, String msg){
                Log.d("SWELL" , "Battery info failed: " + msg);
            }
        });
    }

    private void getCurrentTime() {
        this.miBand.getCurrentTime(new ActionCallback() {
            @Override
            public void onSuccess(Object data){
                Date currentTime = (Date) data;
                Log.d("SWELL", "Current time: " + currentTime.toString());
            }
            @Override
            public void onFail(int errorCode, String msg){
                Log.d("SWELL" , "Get current time failed: " + msg);
            }
        });
    }

    private void setTime() {
        this.miBand.setTime(getCurrentDate(), new ActionCallback() {
            @Override
            public void onSuccess(Object data){
                Log.d("SWELL", "Set current time: " + data.toString());
            }
            @Override
            public void onFail(int errorCode, String msg){
                Log.d("SWELL" , "Set current time failed: " + msg);
            }
        });
    }

    private void setUserData() {
        UserInfo userInfo = new UserInfo(1, UserInfo.GENDER_FEMALE, 37, 166, 72, "Herbert", 1);
        this.miBand.setUserInfo(userInfo);
    }

    private void doVibration() {
        this.miBand.startVibration(VibrationMode.VIBRATION_ONLY);
    }

    private void monitorRealTimeSteps() {
        if (this.stepsMonitor == null) {
            this.stepsMonitor = new MonitorRealtimeSteps();
            this.stepsMonitor.connect(getApplicationContext(), profile, new RealtimeStepsNotifyListener() {
                @Override
                public void onNotify(int steps){
                    Log.d("SWELL", String.format("Steps: %d", steps));
                }
            });
        } else {
            this.stepsMonitor.disconnect();
            this.stepsMonitor = null;
        }
    }

    private void monitorRealtimeHeartRate() {
        if (this.hrMonitor == null) {
            this.hrMonitor = new MonitorRealtimeHeartRate();
            this.hrMonitor.connect(getApplicationContext(), profile, new HeartRateNotifyListener() {
                @Override
                public void onNotify(int heartRate)
                {
                    Log.d("SWELL", "Heart rate: "+ heartRate);
                }
            });
        } else {
            this.hrMonitor.disconnect();
            this.hrMonitor = null;
        }
    }

    private void monitorRealtimeSensor() {
        if (this.sensorMonitor == null) {
            this.sensorMonitor = new MonitorSensorData();
            this.sensorMonitor.connect(getApplicationContext(), profile, new NotifyListener() {
                @Override
                public void onNotify(byte[] data) {
                    Log.d("SWELL", "Sensor: "+ Arrays.toString(data));
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
            public void OnFetchComplete(Calendar startDate, List<Integer> steps) {
                insertIntradayStepsToRepo(startDate, steps);
            }
        };
        /*
        FetchActivityFromDate fetchActivityFromDate = new FetchActivityFromDate(profile,
                fetchActivityListener);
        fetchActivityFromDate.perform(getApplicationContext(), startDate);*/
        this.miBand.fetchActivityData(startDate, fetchActivityListener);
    }

    private static void insertIntradayStepsToRepo(Calendar startDate, List<Integer> steps) {
        FitnessRepository repo = new FitnessRepository();
        Person man = new Person(4, "Herman", "P");
        List<FitnessSample> samples = new ArrayList<>();
        Calendar cal = WellnessDate.getClone(startDate);

        for (int i = 0; i < steps.size(); i++) {
            samples.add(new OneDayFitnessSample(cal.getTime(), steps.get(i)));
            cal.add(Calendar.MINUTE, 1);
        }

        repo.insertIntradayFitness(man, startDate.getTime(), samples);
        repo.updateDailyFitness(man, startDate.getTime()); // TODO should we do this at the end of the insertion completion?
    }

    /* FIREBASE METHODS */
    private static void insertDummyDailyDataToFirebase() {
        FitnessRepository repo = new FitnessRepository();
        Person man = new Person(1, "Herman", "P");
        List<FitnessSample> samples = new ArrayList<>();
        Calendar cal = getDummyDate();

        samples.add(new OneDayFitnessSample(cal.getTime(), 500));
        cal.add(Calendar.DAY_OF_YEAR, 1);
        samples.add(new OneDayFitnessSample(cal.getTime(), 600));
        cal.add(Calendar.DAY_OF_YEAR, 1);
        samples.add(new OneDayFitnessSample(cal.getTime(), 700));
        cal.add(Calendar.DAY_OF_YEAR, 1);
        samples.add(new OneDayFitnessSample(cal.getTime(), 800));

        repo.insertDailyFitness(man, samples);
    }

    private static void updateDailyDataUsingIntraday() {
        FitnessRepository repo = new FitnessRepository();
        Person man = new Person(1, "Herman", "P");
        Calendar cal = getDummyDate();
        repo.updateDailyFitness(man, cal.getTime());
    }

    public static void getDailyFitnessSamplesFromRange() {
        FitnessRepository repo = new FitnessRepository();
        Person man = new Person(1, "Herman", "P");
        List<FitnessSample> samples = new ArrayList<>();
        Calendar cal = getDummyDate();
        Calendar cal2 = getDummyDate();
        cal2.add(Calendar.DAY_OF_YEAR, 1);

        Log.d("SWELL", String.format("getting data from %s to %s", cal.getTime().toString(), cal2.getTime().toString()));
        repo.fetchDailyFitness(man, cal.getTime(), cal2.getTime(), new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("SWELL",
                        FitnessRepository.getDailyFitnessSamples(dataSnapshot).toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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
