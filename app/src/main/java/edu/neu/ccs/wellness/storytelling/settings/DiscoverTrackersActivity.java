package edu.neu.ccs.wellness.storytelling.settings;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewAnimator;

import java.util.ArrayList;
import java.util.List;

import edu.neu.ccs.wellness.storytelling.R;
import edu.neu.ccs.wellness.trackers.UserInfo;
import edu.neu.ccs.wellness.trackers.miband2.MiBand;
import edu.neu.ccs.wellness.trackers.miband2.model.MiBand2BatteryInfo;
import edu.neu.ccs.wellness.utils.WellnessUnit;

public class DiscoverTrackersActivity extends AppCompatActivity {

    public static final int SCANNING_DURATION_MILLISEC = 60000;
    private static String[] PERMISSIONS = {Manifest.permission.ACCESS_COARSE_LOCATION};
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    private Menu menu;
    private ListView trackerListView;
    private List<BluetoothDevice> listOfDevices;
    private DeviceListAdapter deviceListAdapter;
    private boolean isBluetoothScanOn = false;

    private int uid;
    private String role;
    private UserInfo userInfo;

    private Handler handler;

    final ScanCallback scanCallback = new ScanCallback(){
        @Override
        public void onScanResult(int callbackType, ScanResult result){
            BluetoothDevice device = result.getDevice();
            tryAddThisDeviceToList(device);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover_trackers);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        this.uid = getIntent().getIntExtra(Keys.UID, UserSettingFragment.DEFAULT_UID);
        this.role = getIntent().getStringExtra(Keys.ROLE);
        this.userInfo = getuserInfo(getIntent());

        this.listOfDevices = new ArrayList<>();

        this.deviceListAdapter = new DeviceListAdapter(getApplicationContext());
        this.trackerListView = findViewById(R.id.tracker_list_view);
        this.trackerListView.setAdapter(this.deviceListAdapter);
        this.trackerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                BluetoothDevice device = listOfDevices.get(i);
                startPairingTrackerActivity(device, userInfo);
            }
        });

        this.handler = new Handler();

        this.tryRequestPermission();

        this.setTitle(getActivityTitleByRole(this.role));
    }

    private int getActivityTitleByRole(String role) {
        if (Keys.ROLE_CAREGIVER.equals(role)) {
            return R.string.title_activity_discover_trackers_caregiver;
        } else if (Keys.ROLE_CHILD.equals(role)) {
            return R.string.title_activity_discover_trackers_child;
        } else {
            return R.string.title_activity_discover_trackers_generic;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startBluetoothScan();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopBluetoothScan();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_discover_trackers, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_toggle_scan:
                toggleBluetoothScan();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == UserSettingFragment.PICK_BLUETOOTH_ADDRESS) {
            finishActivityAndPassResult(resultCode, intent);
        }
    }

    /* INTER-ACTIVITY COMMUNICATIONS */
    private void startPairingTrackerActivity(BluetoothDevice device, UserInfo userInfo) {
        Intent pairingActivityIntent = new Intent(this, PairingTrackerActivity.class);
        pairingActivityIntent.putExtra(Keys.UID, this.uid);
        pairingActivityIntent.putExtra(Keys.ROLE, this.role);
        pairingActivityIntent.putExtra(Keys.USER_INFO, this.userInfo);
        pairingActivityIntent.putExtra(Keys.BLE_DEVICE, device);
        startActivityForResult(pairingActivityIntent, UserSettingFragment.PICK_BLUETOOTH_ADDRESS);
    }

    private void finishActivityAndPassResult(int resultCode, Intent intent) {
        if (resultCode == Activity.RESULT_OK) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra(Keys.UID, this.uid);
            resultIntent.putExtra(Keys.ROLE, this.role);
            resultIntent.putExtra(Keys.PAIRED_BT_ADDRESS, intent.getStringExtra(Keys.PAIRED_BT_ADDRESS));
            resultIntent.putExtra(Keys.BATTERY_LEVEL, intent.getIntExtra(Keys.BATTERY_LEVEL, UserSettingFragment.DEFAULT_BATTERY_LEVEL));
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        }
    }

    /* BLUETOOTH SCANNING METHODS */
    private void toggleBluetoothScan() {
        if (this.isBluetoothScanOn == false) {
            startBluetoothScan();
            MenuItem menuItem = this.menu.findItem(R.id.menu_toggle_scan);
            menuItem.setTitle(R.string.bluetooth_scan_stop);
        } else {
            stopBluetoothScan();
            MenuItem menuItem = this.menu.findItem(R.id.menu_toggle_scan);
            menuItem.setTitle(R.string.bluetooth_scan_start);
        }
    }

    private void startBluetoothScan() {
        MiBand.startScan(scanCallback);
        this.isBluetoothScanOn = true;
        this.handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toggleBluetoothScan();
            }
        }, SCANNING_DURATION_MILLISEC);
    }

    private void stopBluetoothScan() {
        if (this.isBluetoothScanOn) {
            MiBand.stopScan(scanCallback);
            this.isBluetoothScanOn = false;
        }
    }

    private void tryAddThisDeviceToList(BluetoothDevice device) {
        if (MiBand.isThisDeviceCompatible(device)) {
            if (this.listOfDevices.contains(device) == false) {
                this.listOfDevices.add(device);
                this.deviceListAdapter.refreshList(this.listOfDevices);
            }
        }
    }

    /* BLUETOOTH PERMISSIONS */
    private void tryRequestPermission() {
        if (!isCoarseLocationAllowed()) {
            ActivityCompat.requestPermissions(this, PERMISSIONS,
                    PERMISSION_REQUEST_COARSE_LOCATION);
        }
    }

    private boolean isCoarseLocationAllowed() {
        int permissionCoarseLocation = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        return permissionCoarseLocation == PackageManager.PERMISSION_GRANTED;
    }

    /* LIST ADAPTER */
    public class DeviceListAdapter extends BaseAdapter {
        private LayoutInflater inflater;
        private List<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();

        public DeviceListAdapter(Context context) {
            this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return this.devices.size();
        }

        @Override
        public Object getItem(int position) {
            return this.devices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            BluetoothDevice device = this.devices.get(position);
            View rowView = inflater.inflate(R.layout.item_device, parent, false);

            TextView deviceNameTv = rowView.findViewById(R.id.device_name);
            deviceNameTv.setText(device.getName());

            TextView deviceInfoTv = rowView.findViewById(R.id.device_info);
            deviceInfoTv.setText(device.getAddress());

            return rowView;
        }

        public void refreshList(List<BluetoothDevice> devices) {
            this.devices.clear();
            this.devices.addAll(devices);
            notifyDataSetChanged();
        }
    }

    /* HELPER METHODS */
    private static UserInfo getuserInfo(Intent intent) {
        int uid = intent.getIntExtra(Keys.UID, UserSettingFragment.DEFAULT_UID);
        String name = intent.getStringExtra(Keys.NAME);
        int age = intent.getIntExtra(Keys.AGE, UserSettingFragment.DEFAULT_AGE);
        int heightCm = intent.getIntExtra(Keys.HEIGHT_CM, UserSettingFragment.DEFAULT_HEIGHT_CM);
        int weightLbs = intent.getIntExtra(Keys.WEIGHT_KG, UserSettingFragment.DEFAULT_WEIGHT_KG);
        int weightKgs = (int) WellnessUnit.getKgsFromLbs(weightLbs);
        int sex = UserSettingFragment.DEFAULT_SEX;
        return new UserInfo(uid, sex, age, heightCm, weightKgs, name, 1);
    }
}
