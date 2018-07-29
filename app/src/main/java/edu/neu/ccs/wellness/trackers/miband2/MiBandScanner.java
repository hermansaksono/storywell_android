package edu.neu.ccs.wellness.trackers.miband2;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import edu.neu.ccs.wellness.trackers.GenericScanner;

/**
 * Created by hermansaksono on 7/29/18.
 */

public class MiBandScanner implements GenericScanner {

    private static final String TAG = "miband-scanner";

    private List<ScanFilter> scanFilterList;
    private ScanSettings scanSettings;

    /* CONSTRUCTOR */
    public MiBandScanner() {
        this.scanFilterList = getScanFilterList();
        this.scanSettings = getScanSetting(ScanSettings.SCAN_MODE_LOW_POWER);
    }

    /**
     * Initializes the MiBand 2 BLE scanner. Only looks for devices with name = "MI Band 2" and
     * address that has a match in the address list. The scanner will run in low-power scan mode
     * (i.e., {@link ScanSettings}.SCAN_MODE_LOW_POWER.
     * @param addressList List of addresses that the scanner should look for.
     */
    public MiBandScanner(List<String> addressList) {
        this.scanFilterList = getScanFilterList(addressList);
        this.scanSettings = getScanSetting(ScanSettings.SCAN_MODE_LOW_POWER);
    }

    /**
     * Initializes the MiBand 2 BLE scanner. Only looks for devices with name = "MI Band 2" and
     * address that has a match in the address list. The scanner will run in user defined scan mode.
     * @param addressList List of addresses that the scanner should look for.
     * @param scanMode Scan mode as defined in {@link ScanSettings}.
     */
    public MiBandScanner(List<String> addressList, int scanMode) {
        this.scanFilterList = getScanFilterList(addressList);
        this.scanSettings = getScanSetting(scanMode);
    }

    /**
     * Start MiBand 2 BLE devices scan, then perform the callback on each discovered devices.
     *
     * @param callback
     */
    @Override
    public void startScan(ScanCallback callback) {
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
        scanner.startScan(scanFilterList, scanSettings, callback);
    }

    /**
     * Stop MiBand 2 BLE devices scan.
     * @param callback
     */
    @Override
    public void stopScan(ScanCallback callback) {

    }

    /* HELPER METHODS */
    private ScanSettings getScanSetting(int scanMode) {
        ScanSettings.Builder builder = new ScanSettings.Builder();
        //builder.setNumOfMatches(numDevices);
        builder.setScanMode(scanMode);
        return builder.build();
    }

    private List<ScanFilter> getScanFilterList() {
        List<ScanFilter> scanFilterList = new ArrayList<>();
        scanFilterList.add(getScanFilter());
        return scanFilterList;
    }

    private static List<ScanFilter> getScanFilterList(List<String> addressList) {
        List<ScanFilter> scanFilterList = new ArrayList<>();
        for (String address : addressList) {
            scanFilterList.add(getScanFilterWithAddress(address));
        }
        return scanFilterList;
    }

    private static ScanFilter getScanFilter() {
        ScanFilter.Builder builder = new ScanFilter.Builder();
        builder.setDeviceName(MiBand.DEVICE_NAME);
        return builder.build();
    }

    private static ScanFilter getScanFilterWithAddress(String address) {
        ScanFilter.Builder builder = new ScanFilter.Builder();
        builder.setDeviceName(MiBand.DEVICE_NAME);
        builder.setDeviceAddress(address);
        return builder.build();
    }
}
