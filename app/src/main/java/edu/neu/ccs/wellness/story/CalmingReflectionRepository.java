package edu.neu.ccs.wellness.story;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by hermansaksono on 3/2/19.
 */

public class CalmingReflectionRepository {

    public static final String FIREBASE_ROOT = "app_calming_prompt";

    public static void createRootForCalmingReflectionRepository() {
        DatabaseReference query= FirebaseDatabase.getInstance().getReference().child(FIREBASE_ROOT);
        query.child(CalmingReflectionSet.DEFAULT_ID)
                .setValue(new CalmingReflectionSet(CalmingReflectionSet.DEFAULT_ID, CalmingReflectionSet.DEFAULT_NAME));
    }

    public static DatabaseReference getDefaultDatabaseReference() {
        return getDatabaseReference(CalmingReflectionSet.DEFAULT_ID);
    }

    public static DatabaseReference getDatabaseReference(String calmingReflectionSetId) {
        DatabaseReference query= FirebaseDatabase.getInstance().getReference().child(FIREBASE_ROOT);
        return query.child(calmingReflectionSetId);
    }

    public static CalmingReflectionSet getCalmingReflectionSet(DataSnapshot dataSnapshot) {
        if (dataSnapshot.exists()) {
            return dataSnapshot.getValue(CalmingReflectionSet.class);
        } else {
            return new CalmingReflectionSet(
                    CalmingReflectionSet.DEFAULT_ID, CalmingReflectionSet.DEFAULT_NAME);
        }
    }

}
