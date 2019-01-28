package edu.neu.ccs.wellness.storytelling.settings;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import edu.neu.ccs.wellness.storytelling.Storywell;
import edu.neu.ccs.wellness.utils.WellnessIO;

/**
 * Created by hermansaksono on 1/23/19.
 */

public class SynchronizedSettingRepository {

    private static final String KEY_STORYWELL_SETTING = "storywell_setting";

    /**
     * Check if the {@link SynchronizedSetting} has been initialized
     * @param context
     * @return
     */
    public static boolean isLocalExists(Context context) {
        SharedPreferences sharedPreferences = WellnessIO.getSharedPref(context);
        return sharedPreferences.contains(KEY_STORYWELL_SETTING);
    }

    /**
     * Initialize the {@link SynchronizedSetting} if it has not been set already
     */
    public static void initialize(Context context) {
        if (!isLocalExists(context)) {
            saveLocalAndRemoteInstance(new SynchronizedSetting(), context);
        }
    }

    /**
     * Get local instance of {@link SynchronizedSetting}
     * @param context
     * @return
     */
    public static SynchronizedSetting getLocalInstance(Context context) {
        SharedPreferences sharedPreferences = WellnessIO.getSharedPref(context);
        String jsonString = sharedPreferences.getString(KEY_STORYWELL_SETTING, null);

        if (jsonString == null) {
            return new SynchronizedSetting();
        } else {
            return new Gson().fromJson(jsonString, SynchronizedSetting.class);
        }
    }

    /**
     * Save this instance {@link SynchronizedSetting} locally and remotely
     * @param storywellSetting
     * @param context
     */
    public static void saveLocalAndRemoteInstance(SynchronizedSetting storywellSetting, Context context) {
        Storywell storywell = new Storywell(context);
        saveLocalInstance(storywellSetting, context);
        saveRemoteInstance(storywellSetting, storywell.getGroup().getName());
    }

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

    /**
     * Update the local instance of {@link SynchronizedSetting} with the remote instance.
     * If there's no remote instance, then create a new one and save it locally and remotely.
     * @param listener
     * @param context
     */
    public static void updateLocalInstance(final ValueEventListener listener, Context context) {
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
    }
}
