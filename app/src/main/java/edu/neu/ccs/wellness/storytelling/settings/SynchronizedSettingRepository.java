package edu.neu.ccs.wellness.storytelling.settings;

import android.content.Context;

import com.google.firebase.database.ValueEventListener;

import edu.neu.ccs.wellness.setting.FirebaseSettingRepository;
import edu.neu.ccs.wellness.setting.SettingRepository;

/**
 * Created by hermansaksono on 1/23/19.
 */

public class SynchronizedSettingRepository {

    private static final String KEY_PATH = "group_storywell_setting";

    /**
     * Get local instance of {@link SynchronizedSetting}
     * @param context
     * @return
     */
    public static SynchronizedSetting getLocalInstance(Context context) {
        return getRepository(KEY_PATH).getLocalInstance(SynchronizedSetting.class, context);
    }

    /**
     * Save this instance {@link SynchronizedSetting} locally and remotely
     * @param storywellSetting
     * @param context
     */
    public static void saveLocalAndRemoteInstance(
            SynchronizedSetting storywellSetting, Context context) {
        getRepository(KEY_PATH).saveLocalAndRemoteInstance(storywellSetting, context);
    }

    /**
     * Update the local instance of {@link SynchronizedSetting} with the remote instance.
     * If there's no remote instance, then create a new one and save it locally and remotely.
     * @param listener
     * @param context
     */
    public static void updateLocalInstance(final ValueEventListener listener, Context context) {
        getRepository(KEY_PATH).updateLocalInstance(SynchronizedSetting.class, listener, context);
    }

    /* HELPER FUNCTIONS */
    private static FirebaseSettingRepository getRepository(String path) {
        return new FirebaseSettingRepository(path);
    }
}
