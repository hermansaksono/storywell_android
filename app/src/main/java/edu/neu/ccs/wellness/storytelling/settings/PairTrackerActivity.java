package edu.neu.ccs.wellness.storytelling.settings;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ViewAnimator;

import java.util.Calendar;
import java.util.TimeZone;

import edu.neu.ccs.wellness.people.Person;
import edu.neu.ccs.wellness.storytelling.R;
import edu.neu.ccs.wellness.trackers.BatteryInfo;
import edu.neu.ccs.wellness.trackers.UserInfo;
import edu.neu.ccs.wellness.trackers.callback.ActionCallback;
import edu.neu.ccs.wellness.trackers.callback.BatteryInfoCallback;
import edu.neu.ccs.wellness.trackers.miband2.MiBand;
import edu.neu.ccs.wellness.trackers.miband2.model.MiBand2BatteryInfo;

public class PairTrackerActivity extends AppCompatActivity {

    private static final int SCREEN_CONNECTING = 0;
    private static final int SCREEN_PAIRING_AND_TAP = 1;
    private static final int SCREEN_SETTING_UP = 2;
    private static final int SCREEN_COMPLETE = 3;

    private BluetoothDevice bluetoothDevice;
    private ViewAnimator viewAnimator;
    private MiBand miBand;

    private String currentDeviceAddress;
    private int uid;
    private String role;
    private UserInfo userInfo;
    private MiBand2BatteryInfo batteryInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pair_tracker);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        this.viewAnimator = findViewById(R.id.pairing_view_animator);
        this.viewAnimator.setInAnimation(this, R.anim.view_move_left_next);
        this.viewAnimator.setOutAnimation(this, R.anim.view_move_left_current);

        this.uid = getIntent().getIntExtra(Keys.UID, UserSettingFragment.DEFAULT_AGE);
        this.role = getIntent().getStringExtra(Keys.ROLE);
        this.userInfo = getIntent().getExtras().getParcelable(Keys.USER_INFO);
        this.bluetoothDevice = getIntent().getExtras().getParcelable(Keys.BLE_DEVICE);

        this.setTitle(getActivityTitleByRole(this.role));

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
    }

    private int getActivityTitleByRole(String role) {
        if (Person.ROLE_PARENT.equals(role)) {
            return R.string.title_activity_pair_tracker_caregiver;
        } else if (Person.ROLE_CHILD.equals(role)) {
            return R.string.title_activity_pair_tracker_child;
        } else {
            return R.string.title_activity_pair_tracker_generic;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        this.connectToDevice(this.bluetoothDevice);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (this.miBand != null) {
            this.miBand.disconnect();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /* UI METHODS */
    private void doSavePairing() {
        if (this.currentDeviceAddress != null) {
            finishActivityAndPassAddress(this.currentDeviceAddress);
        }
    }

    private void doCancelPairing() {
        finish();
    }

    private void finishActivityAndPassAddress(String currentDeviceAddress) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(Keys.UID, this.uid);
        resultIntent.putExtra(Keys.ROLE, this.role);
        resultIntent.putExtra(Keys.PAIRED_BT_ADDRESS, currentDeviceAddress);
        resultIntent.putExtra(Keys.BATTERY_LEVEL, this.batteryInfo.getLevel());
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    /* STEP 1: CONNECT TO THE DEVICE */
    private void connectToDevice(BluetoothDevice device) {
        this.currentDeviceAddress = device.getAddress();
        this.showConnectProgress();
        this.miBand = new MiBand();
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

    private void disconnectDevice() {
        if (this.miBand != null) {
            this.miBand.disconnect();
        }
    }

    /* STEP 2: AUTH AND PAIR TO DEVICE */
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
                doSetUpBand();
                Log.d("SWELL", String.format("Paired: %s", data.toString()));
            }
            @Override
            public void onFail(int errorCode, String msg){
                Log.d("SWELL", String.format("Pair failed (%d): %s", errorCode, msg));
            }
        });
    }

    /* STEP 3: SET UP BAND */
    /**
     * Send {@link UserInfo} to the band on a separate thread.
     */
    private void doSetUpBand() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                sendUserInfo();
            }
        });
    }

    private void sendUserInfo() {
        showSettingUp();
        this.miBand.setUserInfo(this.userInfo, new ActionCallback() {
            @Override
            public void onSuccess(Object data){
                doSetDateTime();
                Log.d("SWELL", String.format("Set up success: %s", data.toString()));
            }
            @Override
            public void onFail(int errorCode, String msg){
                Log.d("SWELL", String.format("Set up failed (%d): %s", errorCode, msg));
            }
        });
    }

    /**
     * Send the current datetime to the band on a separate thread.
     */
    private void doSetDateTime() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                sendDateTime();
            }
        });
    }

    private void sendDateTime() {
        this.miBand.setTime(getCurrentCalendar(), new ActionCallback() {
            @Override
            public void onSuccess(Object data) {
                Log.d("SWELL", String.format("Set time successful." ));
                doGetBatteryLevel();
            }
            @Override
            public void onFail(int errorCode, String msg){
                Log.d("SWELL", String.format("Set time failed (%d): %s", errorCode, msg));
            }
        });
    }

    /**
     * Retrieve battery level from the band on a separate thread.
     */
    private void doGetBatteryLevel() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                sendBatteryInfoRequest();
            }
        });
    }

    private void sendBatteryInfoRequest() {
        this.miBand.getBatteryInfo(new BatteryInfoCallback() {
            @Override
            public void onSuccess(BatteryInfo batteryInfo) {
                doReceiveBatteryInfo(batteryInfo);
                Log.d("SWELL", String.format("Battery Info: %s", batteryInfo.toString()));
            }
            @Override
            public void onFail(int errorCode, String msg){
                Log.d("SWELL", String.format("Get battery failed (%d): %s", errorCode, msg));
            }
        });
    }

    private void doReceiveBatteryInfo(BatteryInfo info) {
        this.batteryInfo = (MiBand2BatteryInfo) info;
        doShowPairingComplete();
    }


    /* STEP 4: COMPLETION AND OFFER SAVING */
    private void doShowPairingComplete() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showPairingComplete();
            }
        });
    }

    /* UI METHODS */
    private void showConnectProgress() {
        this.viewAnimator.setDisplayedChild(SCREEN_CONNECTING);
        findViewById(R.id.button_save).setVisibility(View.INVISIBLE);
    }

    private void showPairingAuth() {
        this.viewAnimator.setDisplayedChild(SCREEN_PAIRING_AND_TAP);
        findViewById(R.id.button_save).setVisibility(View.INVISIBLE);
    }

    private void showSettingUp() {
        this.viewAnimator.setDisplayedChild(SCREEN_SETTING_UP);
        findViewById(R.id.button_save).setVisibility(View.INVISIBLE);
    }

    private void showPairingComplete() {
        this.viewAnimator.setDisplayedChild(SCREEN_COMPLETE);
        findViewById(R.id.button_save).setVisibility(View.VISIBLE);
    }

    /* HELPER METHODS */
    private static Calendar getCurrentCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getDefault());
        return calendar;
    }
}
