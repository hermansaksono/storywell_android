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

import java.util.Calendar;
import java.util.Locale;

import edu.neu.ccs.wellness.people.Person;
import edu.neu.ccs.wellness.storytelling.R;
import edu.neu.ccs.wellness.storytelling.Storywell;
import edu.neu.ccs.wellness.utils.FeetInchesPreference;
import edu.neu.ccs.wellness.utils.YearPreference;


public class UserSettingFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final int PICK_BLUETOOTH_ADDRESS = 8123;
    public static final int DEFAULT_UID = 0;
    public static final int DEFAULT_YEAR = 2000;
    public static final int DEFAULT_AGE = 17;
    public static final int DEFAULT_SEX = 0;
    public static final int DEFAULT_HEIGHT_CM = 170;
    public static final int DEFAULT_WEIGHT_KG = 70;
    public static final int DEFAULT_BATTERY_LEVEL = 50;

    private Preference caregiverBluetoothAddressPref;
    private Preference childBluetoothAddressPref;
    private Storywell storywell;
    private SynchronizedSetting setting;
    private Person caregiver;
    private Person child;

    public UserSettingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pref_user_info);

        this.storywell = new Storywell(this.getActivity());
        this.setting = this.storywell.getSynchronizedSetting();
        this.caregiver = storywell.getCaregiver();
        this.child = storywell.getChild();

        //updatePreferences(getPreferenceScreen().getSharedPreferences());

        String caregiverAddress = this.setting
                .getFitnessSyncInfo().getCaregiverDeviceInfo().getBtAddress();
        this.caregiverBluetoothAddressPref = findPreference(Keys.CAREGIVER_BLUETOOTH_ADDR);
        this.caregiverBluetoothAddressPref.setSummary(caregiverAddress);
        this.caregiverBluetoothAddressPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startDiscoverTrackersActivity(Keys.ROLE_CAREGIVER);
                return true;
            }
        });

        String childAddress = this.setting
                .getFitnessSyncInfo().getChildDeviceInfo().getBtAddress();
        this.childBluetoothAddressPref = findPreference(Keys.CHILD_BLUETOOTH_ADDR);
        this.childBluetoothAddressPref.setSummary(childAddress);
        this.childBluetoothAddressPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startDiscoverTrackersActivity(Keys.ROLE_CHILD);
                return true;
            }
        });
        initPreferencesSummary(getPreferenceScreen());
    }

    private void updatePreferences(SharedPreferences sharedPreferences) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        UserBioInfo caregiverBio = setting.getFitnessSyncInfo().getCaregiverBio();
        UserBioInfo childBio = setting.getFitnessSyncInfo().getChildBio();
        editor.putInt(Keys.CAREGIVER_BIRTH_YEAR, caregiverBio.getBirthYear());
        editor.putInt(Keys.CAREGIVER_WEIGHT, caregiverBio.getWeightKg());
        editor.putFloat(Keys.CAREGIVER_HEIGHT, caregiverBio.getHeightCm());
        editor.putInt(Keys.CHILD_BIRTH_YEAR, childBio.getBirthYear());
        editor.putInt(Keys.CHILD_WEIGHT, childBio.getWeightKg());
        editor.putFloat(Keys.CAREGIVER_HEIGHT, childBio.getHeightCm());
        editor.commit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == PICK_BLUETOOTH_ADDRESS) {
            retrieveBluetoothAddressIntent(resultCode, intent);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        updateSynchronizedSetting(sharedPreferences, key);
        updateSummarySelectively(findPreference(key));
    }

    private void updateSynchronizedSetting(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case Keys.CAREGIVER_BIRTH_YEAR:
                this.setting
                        .getFitnessSyncInfo()
                        .getCaregiverBio()
                        .setBirthYear(sharedPreferences.getInt(key, DEFAULT_YEAR));
                break;
            case Keys.CAREGIVER_WEIGHT:
                this.setting
                        .getFitnessSyncInfo()
                        .getCaregiverBio()
                        .setWeightKg(sharedPreferences.getInt(key, DEFAULT_WEIGHT_KG));
                break;
            case Keys.CAREGIVER_HEIGHT:
                this.setting
                        .getFitnessSyncInfo()
                        .getCaregiverBio()
                        .setHeightCm(sharedPreferences.getFloat(key, DEFAULT_HEIGHT_CM));
                break;
            case Keys.CHILD_BIRTH_YEAR:
                this.setting
                        .getFitnessSyncInfo()
                        .getChildBio()
                        .setBirthYear(sharedPreferences.getInt(key, DEFAULT_YEAR));
                break;
            case Keys.CHILD_WEIGHT:
                this.setting
                        .getFitnessSyncInfo()
                        .getChildBio()
                        .setWeightKg(sharedPreferences.getInt(key, DEFAULT_WEIGHT_KG));
                break;
            case Keys.CHILD_HEIGHT:
                this.setting
                        .getFitnessSyncInfo()
                        .getChildBio()
                        .setHeightCm(sharedPreferences.getFloat(key, DEFAULT_HEIGHT_CM));
                break;
        }
        SynchronizedSettingRepository
                .saveLocalAndRemoteInstance(this.setting, getActivity());
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
        SynchronizedSettingRepository.saveLocalAndRemoteInstance(
                this.setting, getActivity());
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
        } else if (preference instanceof FeetInchesPreference) {
            if (isNotSet((FeetInchesPreference) preference)) {
                preference.setSummary(getString(R.string.pref_user_summary_not_set));
            } else {
                preference.setSummary(getString(R.string.pref_user_summary_hidden));
            }
        } else if (preference instanceof YearPreference) {
            if (isNotSet((YearPreference) preference)) {
                preference.setSummary(getString(R.string.pref_user_summary_not_set));
            } else {
                preference.setSummary(getString(R.string.pref_user_summary_hidden));
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

    private static boolean isNotSet(FeetInchesPreference pref) {
        return pref.getValue() == FeetInchesPreference.DEFAULT_VALUE;
    }

    private static boolean isNotSet(YearPreference pref) {
        return pref.getValue() == YearPreference.DEFAULT_VALUE;
    }

    /* BLUETOOTH DISCOVERY METHODS */
    private void startDiscoverTrackersActivity(String role) {
        /*
        SharedPreferences prefs = getPreferenceScreen().getSharedPreferences();
        int uid = -1;
        int gender = 0;
        int age = DEFAULT_AGE;
        int heightCm = DEFAULT_HEIGHT_CM;
        int weightKg = DEFAULT_WEIGHT_KG;
        String name = "";
        if (Keys.ROLE_CAREGIVER.equals(role)) {
            uid = this.caregiver.getId();
            age = WellnessDate.getYear() - prefs.getInt("caregiver_birth_year", 1970);
            heightCm = (int) prefs.getFloat("caregiver_height", DEFAULT_HEIGHT_CM);
            weightKg = Integer.valueOf(prefs.getString("caregiver_weight", String.valueOf(DEFAULT_HEIGHT_CM)));
            name = this.caregiver.getName();
        } else if (Keys.ROLE_CHILD.equals(role)) {
            uid = this.child.getId();
            age = WellnessDate.getYear() - prefs.getInt("child_birth_year", 2000);
            heightCm = (int) prefs.getFloat("child_height", DEFAULT_HEIGHT_CM);
            weightKg = Integer.valueOf(prefs.getString("child_weight", String.valueOf(DEFAULT_HEIGHT_CM)));
            name = this.child.getName();
        }

        Intent pickContactIntent = new Intent(getActivity(), DiscoverTrackersActivity.class);
        pickContactIntent.putExtra(Keys.UID, uid);
        pickContactIntent.putExtra(Keys.ROLE, role);
        pickContactIntent.putExtra(Keys.GENDER, gender);
        pickContactIntent.putExtra(Keys.AGE, age);
        pickContactIntent.putExtra(Keys.HEIGHT_CM, heightCm);
        pickContactIntent.putExtra(Keys.WEIGHT_KG, weightKg);
        pickContactIntent.putExtra(Keys.NAME, name);
        startActivityForResult(pickContactIntent, PICK_BLUETOOTH_ADDRESS);
        */
        UserBioInfo userBioInfo;
        int uid;
        String name;

        switch (role) {
            case Person.ROLE_PARENT:
                userBioInfo = this.setting.getFitnessSyncInfo().getCaregiverBio();
                uid = this.caregiver.getId();
                name = this.caregiver.getName();
                break;
            case Person.ROLE_CHILD:
                userBioInfo = this.setting.getFitnessSyncInfo().getChildBio();
                uid = this.child.getId();
                name = this.child.getName();
                break;
            default:
                return;
        }

        Intent pickContactIntent = new Intent(getActivity(), DiscoverTrackersActivity.class);
        pickContactIntent.putExtra(Keys.UID, uid);
        pickContactIntent.putExtra(Keys.NAME, name);
        pickContactIntent.putExtra(Keys.ROLE, role);
        pickContactIntent.putExtra(Keys.GENDER, userBioInfo.getGender());
        pickContactIntent.putExtra(Keys.AGE, userBioInfo.getAge());
        pickContactIntent.putExtra(Keys.HEIGHT_CM, userBioInfo.getHeightCm());
        pickContactIntent.putExtra(Keys.WEIGHT_KG, userBioInfo.getWeightKg());
        startActivityForResult(pickContactIntent, PICK_BLUETOOTH_ADDRESS);

    }

    /* BLUETOOTH INTENT RECEIVER METHODS */
    private void retrieveBluetoothAddressIntent(int resultCode, Intent intent) {
        if (resultCode == Activity.RESULT_OK) {
            /*

            Context context = getActivity().getApplicationContext();
            String address = getBluetoothAddressFromIntent(intent);
            String role = getRoleFromIntent(intent);
            int batteryLevel = getBatterylevelFromIntent(intent);
            if (Keys.ROLE_CAREGIVER.equals(role)) {
                caregiverBluetoothAddressPref.setSummary(address);
                setLastSyncTime(caregiver, context);
                setBatteryLevel(caregiver, context, getBatterylevelFromIntent(intent));
                setStringToPref(Keys.CAREGIVER_BLUETOOTH_ADDR, address);
            } else if (Keys.ROLE_CHILD.equals(role)) {
                childBluetoothAddressPref.setSummary(address);
                setLastSyncTime(child, context);
                setBatteryLevel(child, context, getBatterylevelFromIntent(intent));
                setStringToPref(Keys.CHILD_BLUETOOTH_ADDR, address);
            }
            */
            String address = intent.getStringExtra(Keys.PAIRED_BT_ADDRESS);
            String role = intent.getExtras().getString(Keys.ROLE);
            int batteryLevel = intent.getIntExtra(Keys.BATTERY_LEVEL, DEFAULT_BATTERY_LEVEL);
            long timestamp = Calendar.getInstance(Locale.US).getTimeInMillis();

            SynchronizedSetting setting = storywell.getSynchronizedSetting();
            DeviceInfo deviceInfo = new DeviceInfo();

            switch (role) {
                case Keys.ROLE_CAREGIVER:
                    caregiverBluetoothAddressPref.setSummary(address);
                    deviceInfo = setting.getFitnessSyncInfo().getCaregiverDeviceInfo();
                    break;
                case Keys.ROLE_CHILD:
                    childBluetoothAddressPref.setSummary(address);
                    deviceInfo = setting.getFitnessSyncInfo().getChildDeviceInfo();
                    break;
                default:
                    // Don't do anything
                    break;
            }

            deviceInfo.setLastSyncTime(timestamp);
            deviceInfo.setBtBatteryLevel(batteryLevel);
            deviceInfo.setBtAddress(address);

            switch (role) {
                case Keys.ROLE_CAREGIVER:
                    setting.getFitnessSyncInfo().setCaregiverDeviceInfo(deviceInfo);
                    break;
                case Keys.ROLE_CHILD:
                    setting.getFitnessSyncInfo().setChildDeviceInfo(deviceInfo);
                    break;
                default:
                    // Don't do anything
                    break;
            }

            SynchronizedSettingRepository.saveLocalAndRemoteInstance(setting, getActivity());
        }
    }

    /*
    private static int getUidFromIntent(Intent intent) {
        return intent.getExtras().getInt(Keys.UID);
    }

    private static String getRoleFromIntent(Intent intent) {
        return intent.getExtras().getString(Keys.ROLE);
    }

    public static String getBluetoothAddressFromIntent(Intent intent) {
        return intent.getStringExtra(Keys.PAIRED_BT_ADDRESS);
    }

    private static int getBatterylevelFromIntent(Intent intent) {
        return intent.getIntExtra(Keys.BATTERY_LEVEL, DEFAULT_BATTERY_LEVEL);
    }

    private static void setLastSyncTime(Person person, Context context) {
        StorywellPerson storywellPerson = StorywellPerson.newInstance(person, context);
        storywellPerson.setLastSyncTime(context, WellnessDate.getNow());
    }

    private static void setBatteryLevel(Person person, Context context, int percent) {
        StorywellPerson storywellPerson = StorywellPerson.newInstance(person, context);
        storywellPerson.setBatteryLevel(context, percent);
    }
    */
}
