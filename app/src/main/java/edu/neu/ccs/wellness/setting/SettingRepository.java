package edu.neu.ccs.wellness.setting;

import android.content.Context;

import com.google.firebase.database.ValueEventListener;

/**
 * Created by hermansaksono on 2/7/19.
 */

public interface SettingRepository {
    /**
     * Get local instance of {@link T}
     * @param context
     * @return {@link T}
     */
    <T extends SyncableSetting> T getLocalInstance(Class<T> type, Context context);

    /**
     * Save the given instance of {@link T} locally and remotely
     * @param syncedSetting
     * @param context
     */
    <T extends SyncableSetting> void saveLocalAndRemoteInstance(T syncedSetting, Context context);

    /**
     * * Update the local instance of {@link T} with the remote instance.
     * If there's no remote instance, then create a new one and save it locally and remotely.
     * @param type The type of {@link T}
     * @param listener
     * @param context
     */
    <T extends SyncableSetting> void updateLocalInstance(
            final Class<T> type, final ValueEventListener listener, Context context);
}
