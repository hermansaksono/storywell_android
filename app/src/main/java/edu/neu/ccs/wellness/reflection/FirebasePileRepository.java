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
    private static final String FIREBASE_REFLECTION_PILE = "group_reflections_pile";
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
                .child(FIREBASE_REFLECTION_PILE)
                .child(groupName)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        responsePiles = processReflectionPile(dataSnapshot);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        responsePiles.clear();
                    }
                });
    }

    private static List<ResponsePile> processReflectionPile(DataSnapshot pilesOfIterations) {
        List<ResponsePile> reflectionPile = new ArrayList<>();
        if (pilesOfIterations.exists()) {
            for (DataSnapshot oneIteration : pilesOfIterations.getChildren()) {
                reflectionPile.addAll(getPilesFromOneIteration(oneIteration));
            }
        }
        return reflectionPile;
    }

    private static List<ResponsePile> getPilesFromOneIteration(DataSnapshot oneIteration) {
        List<ResponsePile> reflectionPileFromOneIncarnation = new ArrayList<>();
        if (oneIteration.exists()) {
            for (DataSnapshot oneStory : oneIteration.getChildren()) {
                int storyId = Integer.getInteger(oneStory.getKey());
                reflectionPileFromOneIncarnation.addAll(getPilesFromOneStory(oneStory, storyId));
            }
        }
        return reflectionPileFromOneIncarnation;
    }

    private static List<ResponsePile> getPilesFromOneStory(DataSnapshot dataSnapshot, int storyId) {
        List<ResponsePile> reflectionPileFromOneStory= new ArrayList<>();
        if (dataSnapshot.exists()) {
            for (DataSnapshot reflectionGroup : dataSnapshot.getChildren()) {
                ResponsePile pile = new ResponsePile(storyId, getPilesFromOneGroup(reflectionGroup));
                reflectionPileFromOneStory.add(pile);
            }
        }
        return reflectionPileFromOneStory;
    }

    private static Map<String, String> getPilesFromOneGroup(DataSnapshot dataSnapshot) {
        Map<String, String> reflectionList = new HashMap<>();
        if (dataSnapshot.exists()) {
            DataSnapshot responseDataSnapshot = dataSnapshot.child(ResponsePile.KEY_RESPONSE_PILE);
            for (DataSnapshot oneReflection : responseDataSnapshot.getChildren()) {
                reflectionList.put(oneReflection.getKey(), (String) oneReflection.getValue());
            }
        }
        return reflectionList;
    }
}
