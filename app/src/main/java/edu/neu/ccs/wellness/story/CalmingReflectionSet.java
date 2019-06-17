package edu.neu.ccs.wellness.story;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.PropertyName;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import edu.neu.ccs.wellness.people.GroupInterface;
import edu.neu.ccs.wellness.server.RestServer;
import edu.neu.ccs.wellness.server.RestServer.ResponseType;
import edu.neu.ccs.wellness.setting.SyncableSetting;
import edu.neu.ccs.wellness.story.interfaces.StoryContent;
import edu.neu.ccs.wellness.story.interfaces.StoryInterface;
import edu.neu.ccs.wellness.story.interfaces.StoryType;

public class CalmingReflectionSet implements StoryInterface, SyncableSetting {
    public static final int SET_LENGTH = 2;
    public static final int SET_TOTAL_LENGTH = 3;
    public static final String DEFAULT_ID = "1";
    public static final String DEFAULT_NAME = "Default";

    public static final String KEY_ID = "id";
    public static final String KEY_TITLE = "title";
    public static final String KEY_CONTENTS = "contents";
    public static final String KEY_STORY_COVER = "STORY_COVER_URL";
    public static final String KEY_STORY_DEF = "STORY_DEF_URL";
    public static final String KEY_STORY_IS_CURRENT = "STORY_IS_CURRENT";
    public static final String KEY_STORY_IS_LOCKED = "STORY_IS_LOCKED";
    public static final String KEY_STORY_NEXT_STORY_ID = "STORY_NEXT_STORY_ID";
    public static final String KEY_REFLECTION_LIST = "KEY_REFLECTION_LIST";
    public static final String KEY_RESPONSE_TIMESTAMP = "KEY_RESPONSE_TIMESTAMP";
    public static final String FILENAME_STORYDEF = "story__id_";

    @SerializedName("id") @PropertyName(value=KEY_ID)
    private String id;

    @SerializedName("title") @PropertyName(value=KEY_TITLE)
    private String title;

    @SerializedName("is_current") @PropertyName(value="is_current")
    private boolean isCurrent = false;

    @SerializedName("is_locked") @PropertyName(value="is_locked")
    private boolean isLocked = false;

    @SerializedName("next_story_id")
    private String nextStoryId = null;

    @PropertyName(value=KEY_CONTENTS)
    private List<StoryContent> contents = new ArrayList<>();

    private StoryType type = StoryType.CALMING;

    // CONSTRUCTORS
    /***
     * Hidden regular constructor
     * @param id
     * @param title
     */
    public CalmingReflectionSet(String id, String title) {
        this.id = id;
        this.title = title;
        this.nextStoryId = id;
    }

    public CalmingReflectionSet() {

    }

    /***
     * A factory method to newInstance a Story object using a Bundle object
     * @param bundle
     * @return
     */
    public static CalmingReflectionSet create(Bundle bundle) {
        CalmingReflectionSet story = null;
        if (bundle != null) {
            String id = bundle.getString(CalmingReflectionSet.KEY_ID);
            String title = bundle.getString(CalmingReflectionSet.KEY_TITLE);

            story = new CalmingReflectionSet(id, title);
        }
        return story;
    }

    // PUBLIC METHODS
    /**
     * Not implemented
     */
    @Override
    public ResponseType tryLoadStoryDef(Context context, RestServer server, GroupInterface group) {
        // Not implemented
        return ResponseType.UNINITIALIZED;
    }

    /**
     * Not implemented
     */
    @Override
    public ResponseType fetchStoryDef(Context context, RestServer server, GroupInterface group) {
        // Not implemented
        return ResponseType.UNINITIALIZED;
    }

    @Override
    public boolean deleteStoryDef(Context context, RestServer server) {
        return true;
    }

    @Override
    public String getId() { return this.id; }

    @Override
    public String getTitle() {
        return this.title;
    }

    @Override
    public String getCoverUrl() { return null; }

    @Override
    public String getDefUrl() { return null; }

    @Override @Exclude
    public String getDefFilename() { return null; }

    @Override
    public List<StoryContent> getContents() { return this.contents; }

    public void setContents(List<StoryContent> contents) { this.contents = contents; }

    @Override @Exclude
    public StoryContent getContentByIndex(int index) { return this.contents.get(index); }

    @Override
    public StoryType getStoryType() { return this.type; }

    @Override @Exclude
    public boolean isContentSet() {
        return this.contents != null;
    }

    @Override @Exclude
    public String getRefreshDateTime() {
        return null;
    }

    @Override
    public boolean isLocked() {
        return this.isLocked;
    }

    @Override @Exclude
    public String getNextStoryId() {
        return this.nextStoryId;
    }
}