package edu.neu.ccs.wellness.storytelling;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import edu.neu.ccs.wellness.miband2.ActionCallback;
import edu.neu.ccs.wellness.miband2.MiBand;

public class DiscoverTrackersActivity extends AppCompatActivity {

    private static String[] PERMISSIONS = {Manifest.permission.ACCESS_COARSE_LOCATION};
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    private ListView trackerListView;
    private List<BluetoothDevice> listOfDevices;
    private DeviceListAdapter deviceListAdapter;
    private MiBand miBand;

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

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        this.listOfDevices = new ArrayList<>();
        this.deviceListAdapter = new DeviceListAdapter(getApplicationContext());
        this.trackerListView = findViewById(R.id.tracker_list_view);
        this.trackerListView.setAdapter(this.deviceListAdapter);

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

    /* BLUETOOTH SCANNING METHODS */
    private void startBluetoothScan() {
        MiBand.startScan(scanCallback);
    }

    private void stopBluetoothScan() { MiBand.stopScan(scanCallback); }

    private void tryAddThisDeviceToList(BluetoothDevice device) {
        if (MiBand.isThisDeviceCompatible(device)) {
            if (this.listOfDevices.contains(device) == false) {
                this.listOfDevices.add(device);
                this.deviceListAdapter.refreshList(this.listOfDevices);
                Log.d("SWELL", String.format("Tracker found: %s (%s).", device.getName(), device.getAddress()));
            }
        }
    }

    /* BLUETOOTH CONNECTION METHODS */
    private void connectToMiBand2(BluetoothDevice device) {
        this.miBand = new MiBand(this);
        this.miBand.connect(device, new ActionCallback() {
            @Override
            public void onSuccess(Object data){

            }

            @Override
            public void onFail(int errorCode, String msg){
                return;
            }
        });
    }

    private void pairMiBand2() {
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
