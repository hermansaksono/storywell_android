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
import edu.neu.ccs.wellness.miband2.model.BatteryInfo;
import edu.neu.ccs.wellness.miband2.model.MiBandProfile;
import edu.neu.ccs.wellness.miband2.model.VibrationMode;
import edu.neu.ccs.wellness.miband2.operations.FetchActivityFromDate;
import edu.neu.ccs.wellness.miband2.operations.FetchTodaySteps;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import edu.neu.ccs.wellness.fitness.interfaces.FitnessSample;
import edu.neu.ccs.wellness.fitness.storage.FitnessRepository;
import edu.neu.ccs.wellness.fitness.storage.OneDayFitnessSample;
import edu.neu.ccs.wellness.people.Person;
import edu.neu.ccs.wellness.utils.WellnessDate;

public class FitnessSyncActivity extends AppCompatActivity {

    private static final String MI_BAND_PREFIX = "MI Band" ;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private MiBand miBand;
    private String[] permission = {Manifest.permission.ACCESS_COARSE_LOCATION};
    private Button btnFindDevices;
    private FetchTodaySteps fetchTodaySteps;

    final ScanCallback scanCallback = new ScanCallback(){
        @Override
        public void onScanResult(int callbackType, ScanResult result){
            BluetoothDevice device = result.getDevice();
            if (isMiBand(device)) {
                connectToMiBand(device);
                Log.d("SWELL","name:" + device.getName() + ",uuid:"
                        + device.getUuids() + ",add:"
                        + device.getAddress() + ",type:"
                        + device.getType() + ",bondState:"
                        + device.getBondState() + ",rssi:" + result.getRssi());
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
                tryStartBluetoothScan();
            }
        });

        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doOneVibration();
            }
        });

        findViewById(R.id.button4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getBatteryInfo();
            }
        });

        findViewById(R.id.button5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchTodaySteps = new FetchTodaySteps();
                fetchTodaySteps.perform(getApplicationContext(), "F4:31:FA:D1:D6:90");
                //getRealTimeStepsNotification();
            }
        });

        findViewById(R.id.button6).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopRealTimeStepsNotification();
            }
        });

        findViewById(R.id.button7).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GregorianCalendar startDate = (GregorianCalendar) getDummyDate();
                MiBandProfile profile = new MiBandProfile("F4:31:FA:D1:D6:90");
                FetchActivityListener fetchActivityListener = new FetchActivityListener() {
                    @Override
                    public void OnFetchComplete(Calendar startDate, List<Integer> steps) {
                        insertIntradayStepsToRepo(startDate, steps);
                    }
                };

                FetchActivityFromDate fetchActivityFromDate = new FetchActivityFromDate(profile,
                        fetchActivityListener);
                fetchActivityFromDate.perform(getApplicationContext(), startDate);
            }
        });

        findViewById(R.id.button8).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getCurrentTime();
            }
        });


        findViewById(R.id.button9).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
        });

        findViewById(R.id.button10).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
        });


        findViewById(R.id.button11).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FitnessRepository repo = new FitnessRepository();
                Person man = new Person(1, "Herman", "P");
                Calendar cal = getDummyDate();
                repo.updateDailyFitness(man, cal.getTime());
                /*
                FitnessRepository repo = new FitnessRepository();
                Person man = new Person(1, "Herman", "P");
                List<FitnessSample> samples = new ArrayList<>();
                Calendar cal = getDummyDate();

                for (int i = 0; i < 1440; i++) {
                    cal.add(Calendar.MINUTE, 1);
                    samples.add(new OneDayFitnessSample(cal.getTime(), i+1));
                }

                repo.insertIntradayFitness(man, getDummyDate().getTime(), samples);
                */
            }
        });

        /*
        findViewById(R.id.button12).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FitnessRepository repo = new FitnessRepository();
                Person man = new Person(1, "Herman", "P");
                Calendar cal = getDummyDate();

                Log.d("SWELL", String.format("getting data from %s", cal.getTime().toString()));
                repo.fetchIntradayFitness(man, cal.getTime(), new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d("SWELL",
                                FitnessRepository.getIntradayFitnessSamples(dataSnapshot, 15).toString());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
        */
    }

    @Override
    protected void onResume() {
        super.onResume();
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

    private void tryStartBluetoothScan() {
        MiBand.startScan(scanCallback);
    }

    private boolean isMiBand(BluetoothDevice device) {
        String name = device.getName();
        if (name !=  null) {
            return name.startsWith(MI_BAND_PREFIX);
        } else {
            return false;
        }
    }

    private void connectToMiBand(BluetoothDevice device) {
        this.miBand.connect(device, new ActionCallback() {
            @Override
            public void onSuccess(Object data){
                Log.d("SWELL","connect success");
                btnFindDevices.setText("Connected");
                MiBand.stopScan(scanCallback);
            }
            @Override
            public void onFail(int errorCode, String msg){
                Log.d("SWELL","connect fail, code:"+errorCode+",mgs:"+msg);
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
                Log.d("SWELL" , "readRssi fail: " + msg);
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
                Log.d("SWELL" , "readRssi fail: " + msg);
            }
        });
    }

    private void doOneVibration() {
        this.miBand.startVibration(VibrationMode.VIBRATION_WITH_LED);
    }

    private void stopRealTimeStepsNotification() {
        this.miBand.disableRealtimeStepsNotify();
    }

    private void insertIntradayStepsToRepo(Calendar startDate, List<Integer> steps) {
        FitnessRepository repo = new FitnessRepository();
        Person man = new Person(1, "Herman", "P");
        List<FitnessSample> samples = new ArrayList<>();
        Calendar cal = WellnessDate.getClone(startDate);

        for (int i = 0; i < steps.size(); i++) {
            samples.add(new OneDayFitnessSample(cal.getTime(), steps.get(i)));
            cal.add(Calendar.MINUTE, 1);
        }

        repo.insertIntradayFitness(man, startDate.getTime(), samples);
        repo.updateDailyFitness(man, startDate.getTime()); // TODO should we do this at the end of the insertion completion?
    }

    private static Calendar getDummyDate() {
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTimeZone(TimeZone.getDefault());
        calendar.set(Calendar.YEAR, 2018);
        calendar.set(Calendar.MONTH, Calendar.JUNE);
        calendar.set(Calendar.DAY_OF_MONTH, 25);
        calendar.set(Calendar.HOUR_OF_DAY, 16);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

}
