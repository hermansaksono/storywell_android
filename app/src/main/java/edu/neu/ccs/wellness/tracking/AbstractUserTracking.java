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
     * @param eventParameters The map of event parameters. Passing null indicates that the event
     *                        has no parameters. Parameter names can be up to 40 characters long
     *                        and must start with an alphabetic character and contain only
     *                        alphanumeric characters and underscores.
     */
    // TODO: HS
    // Note: Please be careful when changing the method's contract in abstract classes or
    // interfaces. These contracts are usually set by the development leader so that different
    // classes that extend an abstract class can remain compatible.
    // First, if you have UserTrackDetails.EventName` as the type of event's name, then it's
    // difficult for other project to use it. For example, a different project may need to add its
    // own event, but they will need to modify UserTrackDetails. I suggest to revert the type back
    // to String (just like how Firebase Analytics do it). I created a class called Event to store
    // possible event names.
    // Second, I appreciate that you add EventParameter as an additional input for this method.
    // This will be handy for future projects. Thanks! However, using String as an EventParameter
    // is inflexible. Some projects may need to store boolean or int or float values. Additionally,
    // some project may need to use event parameters not listed in EventParameters. I suggest to
    // use Android's bundle class. This is how Firebase Analytics do it.
    //
    // I comment out the old contract that you made, and added my suggested contract.
    public abstract void logEvent(String eventName, Bundle eventParameters);
    /*
    public abstract void logEvent(UserTrackDetails.EventName eventName,
                                  Map<UserTrackDetails.EventParameters, String> eventParameters);
    */
}
