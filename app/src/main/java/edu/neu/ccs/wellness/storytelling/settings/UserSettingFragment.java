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

import edu.neu.ccs.wellness.storytelling.DiscoverTrackersActivity;
import edu.neu.ccs.wellness.storytelling.R;


public class UserSettingFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final int PICK_BLUETOOTH_ADDRESS = 81007;

    private Preference caregiverBluetoothAddressPref;
    private Preference childBluetoothAddressPref;

    public UserSettingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pref_user_info);

        this.caregiverBluetoothAddressPref = findPreference(Keys.CAREGIVER_BLUETOOTH_ADDR);
        this.caregiverBluetoothAddressPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startDiscoverTrackersActivity(Keys.ROLE_CAREGIVER);
                return true;
            }
        });

        this.childBluetoothAddressPref = findPreference(Keys.CHILD_BLUETOOTH_ADDR);
        this.childBluetoothAddressPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startDiscoverTrackersActivity(Keys.ROLE_CHILD);
                return true;
            }
        });

        initPreferencesSummary(getPreferenceScreen());
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == PICK_BLUETOOTH_ADDRESS) {
            retrieveBluetoothAddressIntent(resultCode, intent);
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
        }  else if (pref instanceof MultiSelectListPreference) {
            EditTextPreference editTextPref = (EditTextPreference) pref;
            pref.setSummary(editTextPref.getText());
        } else if (pref instanceof EditTextPreference) {
            EditTextPreference editTextPreference = (EditTextPreference) pref;
            if (isNotSet(editTextPreference)) {
                editTextPreference.setSummary(getString(R.string.pref_user_summary_not_set));
            } else if (isPassword(editTextPreference)) {
                pref.setSummary("******");
            } else {
                pref.setSummary(editTextPreference.getText());
            }
        } else {
            updateBasicPreferenceSummary(pref);
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

    private static void updateBasicPreferenceSummary(Preference preference) {
        String key = preference.getKey();
        SharedPreferences sharedPrefs = preference.getSharedPreferences();
        String value = sharedPrefs.getString(key, "");

        if (value.length() == 0) {
            preference.setSummary(R.string.pref_user_summary_not_set);
        } else {
            preference.setSummary(value);
        }
    }

    private static boolean isNotSet(EditTextPreference editTextPreference) {
        return editTextPreference.getText() == null;
    }

    /* BLUETOOTH DISCOVERY METHODS */
    private void startDiscoverTrackersActivity(String role) {
        Intent pickContactIntent = new Intent(getActivity(), DiscoverTrackersActivity.class);
        pickContactIntent.putExtra(Keys.ROLE, role);
        startActivityForResult(pickContactIntent, PICK_BLUETOOTH_ADDRESS);
    }

    private static String getRoleFromIntent(Intent intent) {
        return intent.getExtras().getString(Keys.ROLE);
    }

    private static String getBluetoothAddressFromIntent(Intent intent) {
        return intent.getExtras().getString(Keys.PAIRED_BT_ADDRESS);
    }

    private static String getUidFromIntent(Intent intent) {
        return intent.getExtras().getString(Keys.UID);
    }

    /* BLUETOOTH INTENT RECEIVER METHODS */
    private void retrieveBluetoothAddressIntent(int resultCode, Intent intent) {
        if (resultCode == Activity.RESULT_OK) {
            String uid = getUidFromIntent(intent);
            String address = getBluetoothAddressFromIntent(intent);
            String role = getRoleFromIntent(intent);

            if (Keys.ROLE_CAREGIVER.equals(role)) {
                caregiverBluetoothAddressPref.setSummary(address);
                setStringToPref(Keys.CAREGIVER_BLUETOOTH_ADDR, address);
            } else if (Keys.ROLE_CHILD.equals(role)) {
                childBluetoothAddressPref.setSummary(address);
                setStringToPref(Keys.CHILD_BLUETOOTH_ADDR, address);
            }
        }
    }
}
