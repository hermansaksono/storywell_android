package edu.neu.ccs.wellness.storytelling.settings;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
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

        initPreferencesSummary(getPreferenceScreen());
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
        updateSummarySelectively(findPreference(key));
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

    /* UI METHODS */
    private void initPreferencesSummary(Preference preference) {
        if (preference instanceof PreferenceGroup) {
            initPreferenceGroup((PreferenceGroup) preference);
        } else {
            updateSummarySelectively(preference);
        }
    }

    private void initPreferenceGroup(PreferenceGroup preferenceGroup) {
        for (int i = 0; i < preferenceGroup.getPreferenceCount(); i++) {
            initPreferencesSummary(preferenceGroup.getPreference(i));
        }
    }

    /* PREFS METHODS */
    private void updateSummarySelectively(Preference preference) {
        String key = preference.getKey();

        if (key.equals(Keys.CAREGIVER_BIRTH_YEAR)) {
            this.updateSummaryToHidden(preference);
        } else if (key.equals(Keys.CAREGIVER_HEIGHT)) {
            this.updateSummaryToHidden(preference);
        } else if (key.equals(Keys.CAREGIVER_WEIGHT)) {
            this.updateSummaryToHidden(preference);
        } else if (key.equals(Keys.CHILD_BIRTH_YEAR)) {
            this.updateSummaryToHidden(preference);
        } else if (key.equals(Keys.CHILD_WEIGHT)) {
            this.updateSummaryToHidden(preference);
        } else if (key.equals(Keys.CHILD_HEIGHT)) {
            this.updateSummaryToHidden(preference);
        } else {
            this.updateSummaryFromPref(preference);
        }
    }

    private void updateSummaryFromPref(Preference pref) {
        if (pref instanceof ListPreference) {
            ListPreference listPref = (ListPreference) pref;
            pref.setSummary(listPref.getEntry());
        } else if (pref instanceof EditTextPreference) {
            EditTextPreference editTextPreference = (EditTextPreference) pref;
            if (isNotSet(editTextPreference)) {
                editTextPreference.setSummary(getString(R.string.pref_user_summary_not_set));
            } else if (isPassword(editTextPreference)) {
                pref.setSummary("******");
            } else {
                pref.setSummary(editTextPreference.getText());
            }
        } else if (pref instanceof MultiSelectListPreference) {
            EditTextPreference editTextPref = (EditTextPreference) pref;
            pref.setSummary(editTextPref.getText());
        }
    }

    private void updateSummaryToHidden(Preference preference) {
        if (preference instanceof EditTextPreference) {
            EditTextPreference editTextPreference = (EditTextPreference) preference;
            if (isNotSet(editTextPreference)) {
                editTextPreference.setSummary(getString(R.string.pref_user_summary_not_set));
            } else if (isPassword(editTextPreference)) {
                editTextPreference.setSummary("******");
            } else {
                editTextPreference.setSummary(getString(R.string.pref_user_summary_hidden));
            }
        }
    }

    private void setStringToPref(String key, String address) {
        SharedPreferences.Editor prefEdit = getPreferenceScreen().getSharedPreferences().edit();
        prefEdit.putString(key, address);
        prefEdit.commit();
    }

    private static boolean isPassword(EditTextPreference editTextPreference) {
        return editTextPreference.getTitle().toString().toLowerCase().contains("password");
    }

    private static boolean isNotSet(EditTextPreference editTextPreference) {
        return editTextPreference.getText() == null;
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
