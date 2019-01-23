package edu.neu.ccs.wellness.fitness.challenges;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import edu.neu.ccs.wellness.reflection.ResponsePileListFactory;

/**
 * Created by hermansaksono on 1/22/19.
 */

public class ChallengesRepository {
    public static final String FIREBASE_COMPLETED_CHALLENGES = "group_completed_challenge";

    private DatabaseReference firebaseDbRef = FirebaseDatabase.getInstance().getReference();

    public void addUnlockedChallenge(String groupName, String challengeName) {
        this.firebaseDbRef
                .child(ResponsePileListFactory.FIREBASE_REFLECTION_PILE)
                .child(groupName)
                .push()
                .setValue(challengeName);
    }
}
