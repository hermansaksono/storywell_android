package edu.neu.ccs.wellness.tracking;

import android.os.Bundle;

/**
 * Created by hermansaksono on 7/11/18.
 */

abstract class AbstractUserTracking {

    private String uid;

    /**
     * Constructor.
     *
     * @param uid Anonymized user id. Must not contain any personally identifiable information.
     */
    public AbstractUserTracking(String uid) {
        this.uid = uid;
    }

    /**
     * Get the uid.
     * @return The User's id.
     */
    public final String getUid() {
        return this.uid;
    }

    /**
     * Logs an app event under the user's uid and for the current timestamp in UTC.
     *
     * @param eventName The name of the event. Should contain 1 to 40 alphanumeric characters or
     *                  underscores. The name must start with an alphabetic character.
     */
    public abstract void logEvent(String eventName);

    /**
     * Not implemented
     * @param eventName The name of the event. Should contain 1 to 40 alphanumeric characters or
     *                  underscores. The name must start with an alphabetic character.
     * @param bundle The map of event parameters. Passing null indicates that the event has no
     *               parameters. Parameter names can be up to 40 characters long and must start
     *               with an alphabetic character and contain only alphanumeric characters and
     *               underscores.
     */
    public abstract void logEvent(String eventName, Bundle bundle);
}
