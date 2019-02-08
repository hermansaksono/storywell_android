package edu.neu.ccs.wellness.setting;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

/**
 * Created by hermansaksono on 2/7/19.
 */

public class FirebaseSettingRepository implements SettingRepository {

    public static final String DEFAULT_FIREBASE_KEY = "user_setting";
    private static final String SHARED_PREFS_NAME_FORMAT = "shared_prefs__%s";

    private String keyName;
    private String sharedPrefsName;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference firebaseDbRef;

    /* CONSTRUCTORS */
    public FirebaseSettingRepository() {
        this.keyName = DEFAULT_FIREBASE_KEY;
        this.sharedPrefsName = String.format(SHARED_PREFS_NAME_FORMAT, this.keyName);
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.firebaseDbRef = FirebaseDatabase.getInstance().getReference();
    }

    public FirebaseSettingRepository(String firebasePath) {
        this.keyName = firebasePath;
        this.sharedPrefsName = String.format(SHARED_PREFS_NAME_FORMAT, this.keyName);
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.firebaseDbRef = FirebaseDatabase.getInstance().getReference();
    }

    /* METHODS */
    /**
     * Get local instance of {@link T}
     * @param context
     * @return {@link T}
     */
    @Override
    public <T extends SyncableSetting> T getLocalInstance(Class<T> type, Context context) {

        SharedPreferences sharedPreferences = getSharedPref(context);
        String jsonString = sharedPreferences.getString(this.keyName, null);

        if (jsonString == null) {
            return getDefaultInstance(type);
        } else {
            return new Gson().fromJson(jsonString, type);
        }
    }

    /**
     * Save the given instance of {@link T} locally and remotely
     * @param syncedSetting
     * @param context
     */
    @Override
    public <T extends SyncableSetting> void saveLocalAndRemoteInstance(
            T syncedSetting, Context context) {
        saveLocalInstance(syncedSetting, context);
        saveRemoteInstance(syncedSetting);
    }

    private <T extends SyncableSetting> void saveLocalInstance(T syncedSetting, Context context) {
        SharedPreferences sharedPreferences = getSharedPref(context);
        String jsonString = new Gson().toJson(syncedSetting);
        sharedPreferences.edit().putString(this.keyName, jsonString).apply();
    }

    private <T extends SyncableSetting> void saveRemoteInstance(T syncedSetting) {
        this.firebaseDbRef
                .child(this.keyName)
                .child(this.firebaseAuth.getCurrentUser().getUid())
                .setValue(syncedSetting);
    }

    /**
     * * Update the local instance of {@link T} with the remote instance.
     * If there's no remote instance, then create a new one and save it locally and remotely.
     * @param type The type of {@link T}
     * @param listener
     * @param context
     */
    @Override
    public <T extends SyncableSetting> void updateLocalInstance(
            final Class<T> type, final ValueEventListener listener, Context context) {
        final Context application = context.getApplicationContext();
        this.firebaseDbRef
                .child(this.keyName)
                .child(this.firebaseAuth.getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        processDataSnapshot(dataSnapshot, type, application);
                        listener.onDataChange(dataSnapshot);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        listener.onCancelled(databaseError);
                    }
                });
    }

    private <T extends SyncableSetting> void processDataSnapshot(
            DataSnapshot dataSnapshot, Class<T> type, Context context) {
        if (dataSnapshot.exists()) {
            saveLocalInstance(dataSnapshot.getValue(type), context);
        } else {
            saveLocalAndRemoteInstance(getDefaultInstance(type), context);
        }
    }

    /* HELPER METHODS */
    private SharedPreferences getSharedPref(Context context) {
        return context.getSharedPreferences(this.sharedPrefsName, Context.MODE_PRIVATE);
    }

    private <T extends SyncableSetting> T getDefaultInstance(Class<T> type) {
        try {
            return type.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }
}
