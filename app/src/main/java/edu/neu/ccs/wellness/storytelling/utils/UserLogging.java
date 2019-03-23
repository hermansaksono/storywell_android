package edu.neu.ccs.wellness.storytelling.utils;

import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;

import edu.neu.ccs.wellness.logging.Param;
import edu.neu.ccs.wellness.logging.WellnessUserLogging;

/**
 * Created by hermansaksono on 3/12/19.
 */

public class UserLogging {

    public static void logButtonSync() {
        WellnessUserLogging userLogging = new WellnessUserLogging(getUid());
        Bundle bundle = new Bundle();
        bundle.putString(Param.BUTTON_NAME, "PLAY_ANIMATION");
        userLogging.logEvent("PLAY_ANIMATION_BUTTON_CLICK", bundle);
    }

    private static String getUid() {
        return FirebaseAuth.getInstance().getUid();
    }
}
