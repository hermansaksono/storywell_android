package edu.neu.ccs.wellness.storytelling.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.util.Log;

import edu.neu.ccs.wellness.storytelling.DiscoverTrackersActivity;
import edu.neu.ccs.wellness.storytelling.R;


public class UserSettingFragment extends PreferenceFragment {

    public static final int PICK_BLUETOOTH_ADDRESS = 81007;
    public static final String KEY_UID = "KEY_UID";

    private Preference userBluetoothAddressPref;

    public UserSettingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pref_user_info);

        this.userBluetoothAddressPref = findPreference("string_bluetooth_address");
        this.userBluetoothAddressPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startDiscoverTrackersActivity();
                return true;
            }
        });
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == PICK_BLUETOOTH_ADDRESS) {
            if (resultCode == Activity.RESULT_OK) {
                String uid = getUidFromIntent(intent);
                String address = getBluetoothAddressFromIntent(intent);
                Log.d("SWELL", "Uid: " + uid);
                Log.d("SWELL", "Address: " + address);
                userBluetoothAddressPref.setSummary(address);
            }
        }
    }

    private void startDiscoverTrackersActivity() {
        Intent pickContactIntent = new Intent(getActivity(), DiscoverTrackersActivity.class);
        pickContactIntent.putExtra(KEY_UID, "uid");
        startActivityForResult(pickContactIntent, PICK_BLUETOOTH_ADDRESS);
    }

    private static String getBluetoothAddressFromIntent(Intent intent) {
        return intent.getExtras().getString(DiscoverTrackersActivity.KEY_PAIRED_BT_ADDRESS);
    }

    private static String getUidFromIntent(Intent intent) {
        return intent.getExtras().getString(KEY_UID);
    }
}
