package edu.neu.ccs.wellness.storytelling.models;

import android.content.Context;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

import edu.neu.ccs.wellness.storytelling.interfaces.StoryStateInterface;

/**
 * Created by hermansaksono on 10/30/17.
 */

public class StoryState implements StoryStateInterface {

    private static final String STORY_STATE_NAME = "story_%d_state.json";

    @SerializedName("story_id")
    private int storyId;

    @SerializedName("current_page")
    private int currentPage = 0;

    @SerializedName("responded_reflections")
    private Map<Integer, String> reflections;

    public StoryState(int storyId) {
        this.storyId = storyId;
        this.reflections = new HashMap<>();
    }

    @Override
    public void setCurrentPage(int contentId) { this.currentPage = contentId; }

    @Override
    public int getCurrentPage() { return this.currentPage; }

    @Override
    public String getRecordingURL(int contentId) {
        return this.reflections.get(contentId);
    }

    @Override
    public void addReflection(int contentId, String recordingURL) {
        this.reflections.put(contentId, recordingURL);
    }

    @Override
    public void removeReflection(int contentId) {
        this.reflections.remove(contentId);
    }

    @Override
    public boolean isReflectionResponded(int contentId) {
        return this.reflections.containsKey(contentId);
    }

    public void save(Context context) { }
}
