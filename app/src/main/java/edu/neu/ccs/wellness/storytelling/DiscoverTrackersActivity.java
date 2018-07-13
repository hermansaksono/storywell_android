package edu.neu.ccs.wellness.storytelling;

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
import android.util.Log;
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

import edu.neu.ccs.wellness.miband2.ActionCallback;
import edu.neu.ccs.wellness.miband2.MiBand;
import edu.neu.ccs.wellness.storytelling.settings.UserSettingFragment;

public class DiscoverTrackersActivity extends AppCompatActivity {

    public static final int SCANNING_DURATION_MILLISEC = 60000;
    public static final String KEY_PAIRED_BT_ADDRESS = "KEY_PAIRED_BT_ADDRESS";
    private static String[] PERMISSIONS = {Manifest.permission.ACCESS_COARSE_LOCATION};
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    private Menu menu;
    private ListView trackerListView;
    private List<BluetoothDevice> listOfDevices;
    private DeviceListAdapter deviceListAdapter;
    private ViewAnimator viewAnimator;
    private MiBand miBand;
    private boolean isBluetoothScanOn = false;
    private String currentDeviceAddress;
    private String uid;
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
        setTitle(getActivityTitle());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        this.uid = getIntent().getStringExtra(UserSettingFragment.KEY_UID);

        this.listOfDevices = new ArrayList<>();

        this.viewAnimator = findViewById(R.id.tracker_view_animator);
        this.deviceListAdapter = new DeviceListAdapter(getApplicationContext());
        this.trackerListView = findViewById(R.id.tracker_list_view);
        this.trackerListView.setAdapter(this.deviceListAdapter);
        this.trackerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                BluetoothDevice device = listOfDevices.get(i);
                connectToDevice(device);
            }
        });

        findViewById(R.id.button_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doCancelPairing();
            }
        });

        findViewById(R.id.button_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSavePairing();
            }
        });

        this.handler = new Handler();

        this.tryRequestPermission();
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
                //Log.d("SWELL", String.format("Tracker found: %s (%s).", device.getName(), device.getAddress()));
            }
        }
    }

    /* BLUETOOTH CONNECTION METHODS */
    private void connectToDevice(BluetoothDevice device) {
        this.stopBluetoothScan();
        this.currentDeviceAddress = device.getAddress();
        this.showPairingView();
        this.miBand = new MiBand(this);
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

    private void disconnectDevice() {
        if (this.miBand != null) {
            this.miBand.disconnect();
        }
    }

    private void doSavePairing() {
        if (this.currentDeviceAddress != null) {
            this.miBand.disconnect();
            passAddressAndFinishActivity(this.currentDeviceAddress);
        }
    }

    private void doCancelPairing() {
        this.currentDeviceAddress = null;
        this.miBand.disconnect();
        this.startBluetoothScan();
        this.showListView();
    }

    private void doPostConnectOperations() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                doAuthAndPair();
            }
        });
    }

    private void doAuthAndPair() {
        boolean isPaired = miBand.getDevice().getBondState() != BluetoothDevice.BOND_NONE;
        if (isPaired == false) {
            this.doAuth();
            showPairingAuth();
        } else {
            this.doPair();
        }
    }

    private void doAuth() {
        this.miBand.auth(new ActionCallback() {
            @Override
            public void onSuccess(Object data){
                doPostAuth();
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
                doShowPairingComplete();
                Log.d("SWELL", String.format("Paired: %s", data.toString()));
            }
            @Override
            public void onFail(int errorCode, String msg){
                Log.d("SWELL", String.format("Pair failed (%d): %s", errorCode, msg));
            }
        });
    }

    private void doShowPairingComplete() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showPairingComplete();
            }
        });
    }

    private void passAddressAndFinishActivity(String currentDeviceAddress) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(KEY_PAIRED_BT_ADDRESS, currentDeviceAddress);
        resultIntent.putExtra(UserSettingFragment.KEY_UID, this.uid);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
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
                android.Manifest.permission.ACCESS_COARSE_LOCATION);
        return permissionCoarseLocation == PackageManager.PERMISSION_GRANTED;
    }

    /* UI METHODS */
    private void showListView() {
        this.viewAnimator.setInAnimation(this, R.anim.view_move_right_prev);
        this.viewAnimator.setOutAnimation(this, R.anim.view_move_right_current);
        this.viewAnimator.setDisplayedChild(0);
    }

    private void showPairingView() {
        showConnectProgress();
        this.viewAnimator.setInAnimation(this, R.anim.view_move_left_next);
        this.viewAnimator.setOutAnimation(this, R.anim.view_move_left_current);
        this.viewAnimator.setDisplayedChild(1);
    }

    private void showConnectProgress() {
        findViewById(R.id.step1).setVisibility(View.VISIBLE);
        findViewById(R.id.step2).setVisibility(View.GONE);
        findViewById(R.id.step3).setVisibility(View.GONE);
        findViewById(R.id.button_save).setVisibility(View.INVISIBLE);
    }

    private void showPairingAuth() {
        findViewById(R.id.step1).setVisibility(View.GONE);
        findViewById(R.id.step2).setVisibility(View.VISIBLE);
        findViewById(R.id.step3).setVisibility(View.GONE);
        findViewById(R.id.button_save).setVisibility(View.INVISIBLE);
    }

    private void showPairingComplete() {
        findViewById(R.id.step1).setVisibility(View.GONE);
        findViewById(R.id.step2).setVisibility(View.GONE);
        findViewById(R.id.step3).setVisibility(View.VISIBLE);
        findViewById(R.id.button_save).setVisibility(View.VISIBLE);
    }

    private String getActivityTitle() {
        return String.format(getString(R.string.title_activity_discover_trackers_var), "Anna");
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
}
