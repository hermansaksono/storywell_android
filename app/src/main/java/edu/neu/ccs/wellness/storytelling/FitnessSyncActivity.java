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

import com.hermansaksono.miband.ActionCallback;
import com.hermansaksono.miband.MiBand;
import com.hermansaksono.miband.listeners.RealtimeStepsNotifyListener;
import com.hermansaksono.miband.model.BatteryInfo;
import com.hermansaksono.miband.model.VibrationMode;
import com.hermansaksono.miband.operations.FetchActivityFromDate;
import com.hermansaksono.miband.operations.FetchTodaySteps;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;

public class FitnessSyncActivity extends AppCompatActivity {

    public static final UUID UUID_BATTERY = UUID.fromString(String.format("0000ff0c-0000-1000-8000-00805f9b34fb"));
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
                FetchActivityFromDate fetchActivityFromDate = new FetchActivityFromDate();
                fetchActivityFromDate.perform(getApplicationContext(),
                        "F4:31:FA:D1:D6:90", startDate);
            }
        });

        findViewById(R.id.button8).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getCurrentTime();
            }
        });


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

    private static Calendar getDummyDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2018);
        calendar.set(Calendar.MONTH, Calendar.JUNE);
        calendar.set(Calendar.DAY_OF_MONTH, 22);
        calendar.set(Calendar.HOUR_OF_DAY, 22);
        calendar.set(Calendar.MINUTE, 22);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

}
