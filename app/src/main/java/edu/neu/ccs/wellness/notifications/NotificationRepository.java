package edu.neu.ccs.wellness.notifications;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by hermansaksono on 2/5/19.
 */

public class NotificationRepository {
    private static final String REF = "app_notifications/regular";
    private static final String KEY_DAY = "day";

    static void generateARegularNotification(
            int day, ValueEventListener listenerToShowTheNotification) {
        DatabaseReference firebaseDbRef = FirebaseDatabase.getInstance().getReference().child(REF);
        firebaseDbRef.orderByChild(KEY_DAY)
                .equalTo(String.valueOf(day))
                .addListenerForSingleValueEvent(listenerToShowTheNotification);
    }
}
