package edu.neu.ccs.wellness.tracking;

import android.os.Bundle;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by hermansaksono on 7/11/18.
 */

public class WellnessUserTracking extends AbstractUserTracking {

    private static final String FIREBASE_USER_TRACKING_ROOT = "user_tracking";
    private DatabaseReference databaseReference;
    private DatabaseReference userTrackingRoot;
    private UserTrackDetails userTrackDetails; // TODO
                                               // HS: Why do you have this as class variable? Do
                                               // you plan to use it in other methods? Can you
                                               // just define it as a local variable under the
                                               // logEvent method?
    private DatabaseReference userId;
    private DatabaseReference randomKeyRef;


    /**
     * Constructor.
     *
     * @param uid Anonymized user id. Must not contain any personally identifiable information.
     */
    public WellnessUserTracking(String uid) {
        super(uid);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        userTrackingRoot = databaseReference.child(FIREBASE_USER_TRACKING_ROOT);
    }

    // TODO:
    // HS The purpose statement here needs to be updated to show the new structure in Firebase.
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
    public void logEvent(UserTrackDetails.EventName eventName, Map<UserTrackDetails.EventParameters, String> eventParameters) {
        String uid = this.getUid();
        String randomKey = getRandomString();
        userTrackDetails = new UserTrackDetails(eventName, eventParameters);
        /*
        userTrackDetails = new UserTrackDetails(eventName, eventParameters);
        userId = userTrackingRoot.child(uid);
        String randomKey = getRandomString();
        randomKeyRef = userId.child(randomKey);
        randomKeyRef.setValue(userTrackDetails);
        */
        // HS: I reorganized the code as follow:
        userTrackingRoot
                .child(uid)
                .child(randomKey) // TODO
                                  // HS: Why do you use a random key. Why not use timestamp? If you
                                  // use timestamp, Firebase will create the index automatically.
                .setValue(userTrackDetails);

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

    protected String getRandomString() {
        String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder randomString = new StringBuilder();
        Random rnd = new Random();
        while (randomString.length() < 18) { // length of the random string.
            int index = (int) (rnd.nextFloat() * CHARS.length());
            randomString.append(CHARS.charAt(index));
        }
        return randomString.toString();

    }
}
