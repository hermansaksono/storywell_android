package edu.neu.ccs.wellness.notifications;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
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
            int day, final ValueEventListener listenerToShowTheNotification) {
        DatabaseReference firebaseDbRef = FirebaseDatabase.getInstance().getReference().child(REF);
        firebaseDbRef.orderByChild(KEY_DAY)
                .equalTo(day)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Log.d("SWELL", dataSnapshot.toString());
                            listenerToShowTheNotification.onDataChange(dataSnapshot);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        listenerToShowTheNotification.onCancelled(databaseError);
                    }
                });
    }
}
