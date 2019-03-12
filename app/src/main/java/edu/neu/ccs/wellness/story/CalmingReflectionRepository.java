package edu.neu.ccs.wellness.story;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import edu.neu.ccs.wellness.story.interfaces.StoryContent;
import edu.neu.ccs.wellness.story.interfaces.StoryContent.ContentType;

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
            String id = String.valueOf(dataSnapshot.child(
                    CalmingReflectionSet.KEY_ID).getValue(Long.class));
            String title = dataSnapshot.child(
                    CalmingReflectionSet.KEY_TITLE).getValue(String.class);
            CalmingReflectionSet calmingReflectionSet = new CalmingReflectionSet(id, title);
            calmingReflectionSet.setContents(getContents(dataSnapshot, calmingReflectionSet));
            return calmingReflectionSet;
        } else {
            return new CalmingReflectionSet(
                    CalmingReflectionSet.DEFAULT_ID, CalmingReflectionSet.DEFAULT_NAME);
        }
    }

    private static List<StoryContent> getContents(
            DataSnapshot dataSnapshot, CalmingReflectionSet calmingReflectionSet) {
        DataSnapshot contentsDS = dataSnapshot.child(CalmingReflectionSet.KEY_CONTENTS);
        if (contentsDS.exists()) {
            return getContentsFromIterable(contentsDS.getChildren(), calmingReflectionSet);
        } else {
            return new ArrayList<>();
        }
    }

    private static List<StoryContent> getContentsFromIterable(
            Iterable<DataSnapshot> dataSnapshotIterable, CalmingReflectionSet calmingReflectionSet) {
        List<StoryContent> contents = new ArrayList<>();
        for (DataSnapshot storyContent : dataSnapshotIterable) {
            contents.add(getStoryContent(storyContent, calmingReflectionSet));
        }
        return contents;
    }

    private static StoryContent getStoryContent(
            DataSnapshot storyContentDS, CalmingReflectionSet calmingReflectionSet) {
        String typeString = storyContentDS.child(CalmingReflection.KEY_TYPE).getValue(String.class);
        ContentType type = ContentType.fromString(typeString);

        switch (type) {
            case REFLECTION:
                return getCalmingReflection(storyContentDS, calmingReflectionSet);
            case STATEMENT:
                return getStatement(storyContentDS, calmingReflectionSet);
            default:
                return null;
        }
    }

    private static StoryContent getStatement(
            DataSnapshot storyContentDS, CalmingReflectionSet calmingReflectionSet) {
        int pageId = storyContentDS.child("id").getValue(Integer.class);
        String imgUrl = storyContentDS.child("img_url").getValue(String.class);
        String text = storyContentDS.child("text").getValue(String.class);
        String subtext = storyContentDS.child("subtext").getValue(String.class);
        boolean isCurrent = false;
        return new StoryStatement(pageId, calmingReflectionSet, imgUrl, text, subtext, isCurrent);
    }

    private static StoryContent getCalmingReflection(
            DataSnapshot storyContentDS, CalmingReflectionSet calmingReflectionSet) {
        int pageId = storyContentDS.child("id").getValue(Integer.class);
        String imgUrl = storyContentDS.child("img_url").getValue(String.class);
        String text = storyContentDS.child("text").getValue(String.class);
        String subtext = storyContentDS.child("subtext").getValue(String.class);
        boolean isShowReflectionStart = getIsShowReflectionStart(storyContentDS);
        String contentGroupId = storyContentDS.child("contentGroupId").getValue(String.class);
        String contentGroupName = storyContentDS.child("contentGroupName").getValue(String.class);
        int nextContentId = storyContentDS.child("nextContentId").getValue(Integer.class);

        return new CalmingReflection(pageId, calmingReflectionSet, imgUrl, text, subtext, isShowReflectionStart,
                contentGroupId, contentGroupName, nextContentId, false);
    }

    private static boolean getIsShowReflectionStart(DataSnapshot storyContentDS) {
        if (storyContentDS.child("isShowReflectionStart").exists()) {
            return storyContentDS.child("isShowReflectionStart").getValue(Boolean.class);
        } else {
            return false;
        }
    }


}
