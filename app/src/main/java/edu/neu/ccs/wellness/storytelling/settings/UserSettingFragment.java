package edu.neu.ccs.wellness.storytelling.settings;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.util.Log;

import edu.neu.ccs.wellness.storytelling.DiscoverTrackersActivity;
import edu.neu.ccs.wellness.storytelling.R;


public class UserSettingFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final int PICK_BLUETOOTH_ADDRESS = 81007;

    private Preference userBluetoothAddressPref;

    public UserSettingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pref_user_info);

        this.userBluetoothAddressPref = findPreference(Keys.CAREGIVER_BLUETOOTH_ADDR);
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
                setStringToPref(Keys.CAREGIVER_BLUETOOTH_ADDR, address);
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        Log.d("SPREF", "Key :" + key);
        if (key.equals(Keys.CAREGIVER_NICKNAME)) {
            this.updateSummaryToReflectChange(sharedPreferences, key);
        } else if (key.equals(Keys.CAREGIVER_BIRTH_YEAR)) {
            this.updateSummaryToHidden(key);
        } else if (key.equals(Keys.CAREGIVER_HEIGHT)) {
            this.updateSummaryToHidden(key);
        } else if (key.equals(Keys.CAREGIVER_WEIGHT)) {
            this.updateSummaryToHidden(key);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    /* PREFS METHODS */
    private void updateSummaryToReflectChange(SharedPreferences sharedPreferences, String key) {
        Preference pref = findPreference(key);
        pref.setSummary(sharedPreferences.getString(key, ""));
    }

    private void updateSummaryToHidden(String key) {
        Preference pref = findPreference(key);
        pref.setSummary(getString(R.string.pref_user_summary_hidden));
    }

    private void setStringToPref(String key, String address) {
        SharedPreferences.Editor prefEdit = getPreferenceScreen().getSharedPreferences().edit();
        prefEdit.putString(key, address);
        prefEdit.commit();
    }

    /* BLUETOOTH METHODS */
    private void startDiscoverTrackersActivity() {
        Intent pickContactIntent = new Intent(getActivity(), DiscoverTrackersActivity.class);
        pickContactIntent.putExtra(Keys.UID, "uid");
        startActivityForResult(pickContactIntent, PICK_BLUETOOTH_ADDRESS);
    }

    private static String getBluetoothAddressFromIntent(Intent intent) {
        return intent.getExtras().getString(DiscoverTrackersActivity.KEY_PAIRED_BT_ADDRESS);
    }

    private static String getUidFromIntent(Intent intent) {
        return intent.getExtras().getString(Keys.UID);
    }
}
