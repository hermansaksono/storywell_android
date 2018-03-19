package edu.neu.ccs.wellness.story;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import edu.neu.ccs.wellness.people.GroupInterface;
import edu.neu.ccs.wellness.story.interfaces.StoryStateInterface;
import edu.neu.ccs.wellness.utils.WellnessIO;

/**
 * Created by hermansaksono on 3/18/18.
 */

public class StoryState implements StoryStateInterface {

    public static final String FIREBASE_STATE_FIELD = "group_story_state";
    private static final String STORY_STATE_NAME = "story__id_%s__state.json";

    private String storyId;
    private int currentPageId = 0;

    public StoryState(String storyId) {
        this.storyId = storyId;
    }

    public static StoryState getSavedInstance(Context context, int storyId) {
        return StoryState.getSavedInstance(context, String.valueOf(storyId));
    }

    public static StoryState getSavedInstance(Context context, String storyId) {
        String filename = getFilename(storyId);
        if (WellnessIO.isFileExists(context, filename)) {
            String jsonString = WellnessIO.readFileFromStorage(context, filename);
            return new Gson().fromJson(jsonString, StoryState.class);
        } else {
            return new StoryState(storyId);
        }
    }

    @Override
    public void setCurrentPage(int contentId) {
        this.currentPageId = contentId;
    }

    @Override
    public int getCurrentPage() {
        return this.currentPageId;
    }

    @Override
    public String getRecordingURL(int contentId) {
        return null;
    }

    @Override
    public void addReflection(int contentId, String recordingURL) {

    }

    @Override
    public void removeReflection(int contentId) {

    }

    @Override
    public boolean isReflectionResponded(int contentId) {
        return false;
    }

    @Override
    public void save(Context context, GroupInterface group) {
        saveOnLocal(context);
        pushToFirebase(group.getName());
    }

    private void saveOnLocal(Context context) {
        String filename = getFilename(this.storyId);
        WellnessIO.writeFileToStorage(context, filename, this.getJson());
    }

    private static String getFilename(String storyId){
        return String.format(STORY_STATE_NAME, storyId);
    }

    private String getJson() {
        return new Gson().toJson(this);
    }

    private void pushToFirebase(String groupName) {
        DatabaseReference firebaseDbRef = FirebaseDatabase.getInstance().getReference();
        firebaseDbRef
                .child(FIREBASE_STATE_FIELD)
                .child(groupName)
                .child(storyId)
                .setValue(this);
    }

    private void pullStatusFromFirebase(String groupName) {
        DatabaseReference firebaseDbRef = FirebaseDatabase.getInstance().getReference();
        firebaseDbRef
                .child(StoryState.FIREBASE_STATE_FIELD)
                .child(groupName)
                .child(storyId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            dataSnapshot.getValue(StoryState.class);
                        } else {
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w("STORYWELL", "loadPost:onCancelled",
                                databaseError.toException());
                    }
                });
    }
}
