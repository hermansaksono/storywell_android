package edu.neu.ccs.wellness.reflection;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by hermansaksono on 3/2/19.
 */

public class FirebaseTreasureRepository {

    public static final String FIREBASE_ROOT = "group_treasure";
    public static final String KEY_CONTENTS = "content";
    private DatabaseReference firebaseDbRef = FirebaseDatabase.getInstance().getReference();

    public FirebaseTreasureRepository() {

    }

    /**
     * Add the story reflection as a {@link TreasureItem} in Firebase DB.
     * @param groupName
     * @param storyId
     * @param pageId
     * @param pageGroup
     * @param title
     * @param audioUri
     * @param timestamp
     * @param reflectionIteration
     */
    public void addStoryReflection(String groupName, String storyId,
                                   String pageId, String pageGroup, String title,
                                   String audioUri, long timestamp, int reflectionIteration) {
        String subParentId = pageGroup.isEmpty() ? TreasureItem.DEFAULT_SUBPARENT_NAME : pageGroup;
        String treasureStringId =
                TreasureItem.getStringId(storyId, subParentId, TreasureItemType.STORY_REFLECTION);

        this.firebaseDbRef
                .child(FIREBASE_ROOT)
                .child(groupName)
                .child(treasureStringId)
                .child(TreasureItem.KEY_CONTENTS)
                .child(pageId)
                .setValue(audioUri);

        this.saveTreasureItemMetadata(groupName, storyId, subParentId, title, timestamp,
                reflectionIteration, TreasureItemType.STORY_REFLECTION);
    }

    /**
     * Save the {@link TreasureItem} meta information into Firebase DB.
     * @param groupName
     * @param parentId
     * @param subParentId
     * @param title
     * @param timestamp
     * @param incarnation
     * @param type
     */
    private void saveTreasureItemMetadata(String groupName, String parentId, String subParentId,
                                          String title, long timestamp,
                                          int incarnation, int type) {
        String treasureStringId =
                TreasureItem.getStringId(parentId, subParentId, TreasureItemType.STORY_REFLECTION);
        DatabaseReference ref =
                this.firebaseDbRef.child(FIREBASE_ROOT).child(groupName).child(treasureStringId);

        if (!title.isEmpty()) {
            // Some treasure content has no title. So we only save them when it's not empty.
            ref.child(TreasureItem.KEY_TITLE).setValue(title);
        }
        ref.child(TreasureItem.KEY_TYPE).setValue(type);
        ref.child(TreasureItem.KEY_PARENT_ID).setValue(parentId);
        ref.child(TreasureItem.KEY_SUBPARENT_ID).setValue(subParentId);
        ref.child(TreasureItem.KEY_INCARNATION).setValue(incarnation);
        ref.child(TreasureItem.KEY_LAST_UPDATE_TIMESTAMP).setValue(timestamp);
    }

    /**
     * Get a list of {@link TreasureItem} from the given {@link DataSnapshot}. Newer items
     * (larger {@link TreasureItem#KEY_LAST_UPDATE_TIMESTAMP)) will appear first.
     * @param dataSnapshot
     * @return
     */
    public static List<TreasureItem> getInstanceList(DataSnapshot dataSnapshot) {
        List<TreasureItem> treasureList = new ArrayList<>();

        for (DataSnapshot childDs : dataSnapshot.getChildren()) {
            treasureList.add(childDs.getValue(TreasureItem.class));
        }
        Collections.reverse(treasureList);
        return treasureList;
    }
}
