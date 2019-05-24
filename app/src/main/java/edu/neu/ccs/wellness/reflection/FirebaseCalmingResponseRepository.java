package edu.neu.ccs.wellness.reflection;

/**
 * Created by hermansaksono on 3/4/19.
 */

public class FirebaseCalmingResponseRepository extends FirebaseReflectionRepository {

    private static final String FIREBASE_ROOT = "group_calming_history";
    private static final String FIRESTORE_FILENAME_FORMAT = "calming%s_content%s %s.3gp";

    public FirebaseCalmingResponseRepository(
            String groupName, String storyId, int reflectionIteration, long reflectionMinEpoch) {
        super(groupName, storyId, reflectionIteration, reflectionMinEpoch,
                FIREBASE_ROOT, FIRESTORE_FILENAME_FORMAT, TreasureItemType.CALMING_PROMPT);
    }
}
