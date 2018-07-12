package edu.neu.ccs.wellness.tracking;

import android.os.Bundle;

/**
 * Created by hermansaksono on 7/11/18.
 */

public class WellnessUserTracking extends AbstractUserTracking {

    private static final String FIREBASE_ROOT = "user_tracking";

    /**
     * Constructor.
     *
     * @param uid Anonymized user id. Must not contain any personally identifiable information.
     */
    public WellnessUserTracking(String uid) {
        super(uid);
    }

    /**
     * Logs an app event in Firebase under the user's uid and for the current timestamp in UTC.
     * Example: Suppose that uid = 717, and timestamp = 1531281600000 (2018-07-11 4:00:00 AM).
     * Calling logEvent("USER_REFLECTION") will add the following entry to Firebase:
     * + user_tracking
     * +---717
     * +-----USER_REFLECTION
     * +-------2018-07-11 04:00:00
     * +---------timestamp: 1531281600000
     *
     *
     * @param eventName The name of the event. Should contain 1 to 40 alphanumeric characters or
     *                  underscores. The name must start with an alphabetic character.
     */
    @Override
    public void logEvent(String eventName) {
        String uid = this.getUid();
    }

    /**
     * Not implemented
     * @param eventName The name of the event. Should contain 1 to 40 alphanumeric characters or
     *                  underscores. The name must start with an alphabetic character.
     * @param bundle The map of event parameters. Passing null indicates that the event has no
     *               parameters. Parameter names can be up to 40 characters long and must start
     *               with an alphabetic character and contain only alphanumeric characters and
     */
    @Override
    public void logEvent(String eventName, Bundle bundle) {
        // DO NOTHING
    }
}
