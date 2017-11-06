package edu.neu.ccs.wellness.storytelling.models;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import edu.neu.ccs.wellness.server.RestServer;
import edu.neu.ccs.wellness.storytelling.interfaces.StoryStateInterface;
import edu.neu.ccs.wellness.utils.WellnessIO;

/**
 * Created by hermansaksono on 10/30/17.
 */

public class StoryState implements StoryStateInterface {

    private static final String STORY_STATE_NAME = "story__id_%d__state.json";
    private static final String RES_POST_STATE = "story/state";

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


    public static StoryStateInterface newInstanceFromSaved(Context context, int storyId) {
        String filename = getFilename(storyId);
        if (WellnessIO.isFileExists(context, filename)) {
            String json = WellnessIO.readFileFromStorage(context, filename);
            return new Gson().fromJson(json, StoryState.class);
        } else {
            return new StoryState(storyId);
        }
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


    @Override
    public void save(Context context) {
        String filename = getFilename(storyId);
        WellnessIO.writeFileToStorage(context, filename, this.getJson());
        Log.d("WELL StoryState saved", filename);
    }

    @Override
    public RestServer.ResponseType sync(RestServer server) {
        RestServer.ResponseType response;
        try {
            server.doPostRequestFromAResource(this.getJson(), RES_POST_STATE);
            response = RestServer.ResponseType.SUCCESS_202;
        } catch (IOException e) {
            response = RestServer.ResponseType.NOT_FOUND_404;
        }
        return response;
    }

    private String getJson() {
        return new Gson().toJson(this);
    }

    private static String getFilename(int storyId){
        return String.format(STORY_STATE_NAME, storyId);
    }
}
