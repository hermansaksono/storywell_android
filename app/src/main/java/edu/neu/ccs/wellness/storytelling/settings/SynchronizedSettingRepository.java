package edu.neu.ccs.wellness.storytelling.settings;

import android.content.Context;

import com.google.firebase.database.ValueEventListener;

import edu.neu.ccs.wellness.setting.SettingRepository;
import edu.neu.ccs.wellness.setting.FirebaseSettingRepository;

/**
 * Created by hermansaksono on 1/23/19.
 */

public class SynchronizedSettingRepository {

    private static final String KEY_STORYWELL_SETTING = "group_storywell_setting";

    /**
     * Get local instance of {@link SynchronizedSetting}
     * @param context
     * @return
     */
    public static SynchronizedSetting getLocalInstance(Context context) {
        /*
        SharedPreferences sharedPreferences = WellnessIO.getSharedPref(context);
        String jsonString = sharedPreferences.getString(KEY_STORYWELL_SETTING, null);

        if (jsonString == null) {
            return new SynchronizedSetting();
        } else {
            return new Gson().fromJson(jsonString, SynchronizedSetting.class);
        }
        */
        SettingRepository settingRepository =
                new FirebaseSettingRepository(KEY_STORYWELL_SETTING);
        return settingRepository.getLocalInstance(SynchronizedSetting.class, context);
    }

    /**
     * Save this instance {@link SynchronizedSetting} locally and remotely
     * @param storywellSetting
     * @param context
     */
    public static void saveLocalAndRemoteInstance(SynchronizedSetting storywellSetting, Context context) {
        /*
        Storywell storywell = new Storywell(context);
        saveLocalInstance(storywellSetting, context);
        saveRemoteInstance(storywellSetting, storywell.getGroup().getName());
        */
        SettingRepository settingRepository =
                new FirebaseSettingRepository(KEY_STORYWELL_SETTING);
        settingRepository.saveLocalAndRemoteInstance(storywellSetting, context);
    }

    /*
    private static void saveLocalInstance(SynchronizedSetting storywellSetting, Context context) {
        SharedPreferences sharedPreferences = WellnessIO.getSharedPref(context);
        String jsonString = new Gson().toJson(storywellSetting);
        sharedPreferences.edit().putString(KEY_STORYWELL_SETTING, jsonString).apply();
    }

    private static void saveRemoteInstance(SynchronizedSetting storywellSetting, String groupName) {
        DatabaseReference firebaseDbRef = FirebaseDatabase.getInstance().getReference();
        firebaseDbRef.child(KEY_STORYWELL_SETTING)
                .child(groupName)
                .setValue(storywellSetting);
    }
    */

    /**
     * Update the local instance of {@link SynchronizedSetting} with the remote instance.
     * If there's no remote instance, then create a new one and save it locally and remotely.
     * @param listener
     * @param context
     */
    public static void updateLocalInstance(final ValueEventListener listener, Context context) {
        /*
        final Context application = context.getApplicationContext();
        Storywell storywell = new Storywell(context);
        final String groupName = storywell.getGroup().getName();
        DatabaseReference firebaseDbRef = FirebaseDatabase.getInstance().getReference();
        firebaseDbRef.child(KEY_STORYWELL_SETTING)
                .child(groupName)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            saveLocalInstance(
                                    dataSnapshot.getValue(SynchronizedSetting.class), application);
                        } else {
                            saveLocalAndRemoteInstance(new SynchronizedSetting(), application);
                        }
                        listener.onDataChange(dataSnapshot);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        listener.onCancelled(databaseError);
                    }
                });
        */
        SettingRepository settingRepository = new FirebaseSettingRepository(
                KEY_STORYWELL_SETTING);
        settingRepository.updateLocalInstance(SynchronizedSetting.class, listener, context);
    }
}
