package edu.neu.ccs.wellness.reflection;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hermansaksono on 1/16/19.
 */

public class FirebasePileRepository {
    private DatabaseReference firebaseDbRef = FirebaseDatabase.getInstance().getReference();

    private List<ResponsePile> responsePiles = new ArrayList<>();

    public FirebasePileRepository(String groupName, int reflectionIteration) {

    }

    public void refreshPile(String groupName) {
        this.refreshReflectionPileFromFirebase(groupName);
    }

    /* UPDATING REFLECTION PILES METHOD */
    public void refreshReflectionPileFromFirebase(String groupName) {
        this.firebaseDbRef
                .child(ResponsePileListFactory.FIREBASE_REFLECTION_PILE)
                .child(groupName)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        responsePiles = ResponsePileListFactory.newInstance(dataSnapshot);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        responsePiles.clear();
                    }
                });
    }
}
