package edu.neu.ccs.wellness.storytelling.models;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import edu.neu.ccs.wellness.storytelling.interfaces.RestServerInterface;
import edu.neu.ccs.wellness.storytelling.interfaces.StoryContentInterface;
import edu.neu.ccs.wellness.storytelling.interfaces.StoryInterface;

/**
 * Created by hermansaksono on 6/13/17.
 */

public class Story implements StoryInterface {
    public static final String FILENAME_STORYDEF = "story__id_%d";
    public static final String FILENAME_IMAGE = "story__id_%d__page_%d__image_0.png";

    private int id;
    private String title;
    private String coverUrl;
    private String defUrl;
    private ArrayList<StoryContentInterface> contents;
    private StoryContentInterface currentContent;
    private String lastRefreshDateTime;
    private boolean isCurrent = false;

    private Story(int id, String title, String coverUrl, String defUrl, boolean isCurrent) {
        this.id = id;
        this.title = title;
        this.coverUrl = coverUrl;
        this.defUrl = defUrl;
        this.isCurrent = isCurrent;
        this.contents = null;
        this.currentContent = null;
        this.lastRefreshDateTime = null;
    }

    public static Story create(JSONObject jsonStory) throws JSONException {
        return new Story(
                jsonStory.getInt("id"),
                jsonStory.getString("title"),
                jsonStory.getString("cover_url"),
                jsonStory.getString("def_url"),
                jsonStory.getBoolean("is_current"));
    }

    @Override
    public int getId() { return this.id; }

    @Override
    public boolean isContentSet() {
        return contents != null;
    }

    @Override
    public List<StoryContentInterface> getContents() {
        return this.contents;
    }

    @Override
    public int getCurrentPageId() {
        return this.currentContent.getId();
    }

    @Override
    public boolean isCurrent() {
        return this.isCurrent;
    }

    @Override
    public void setIsCurrent(boolean isCurrent) {
        this.isCurrent = isCurrent;
    }

    @Override
    public void goToNextPage() { this.goToPageById(this.currentContent.getId() + 1); }

    @Override
    public void goToPrevPage() { this.goToPageById(this.currentContent.getId() - 1); }

    @Override
    public void goToPageById(int pageIndex) {
        this.currentContent = this.contents.get(pageIndex);
    }

    @Override
    public String getRefreshDateTime() {
        return this.lastRefreshDateTime;
    }


    /***
     * Download Story contents
     */
    public void loadStoryDef(Context context, RestServerInterface server) {
        try {
            String jsonString = server.loadGetRequest(context, this.getDefFilename(), this.defUrl);
            JSONObject jsonObject = new JSONObject(jsonString);
            this.contents = getStoryContentsFromJSONArray(jsonObject.getJSONArray("contents"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<StoryContentInterface> getStoryContentsFromJSONArray(JSONArray contents)
            throws JSONException {
        ArrayList<StoryContentInterface> storyContents = new ArrayList<StoryContentInterface>();
        for(int i = 0; i < contents.length(); i++) {
            JSONObject jsonPage = contents.getJSONObject(i);
            storyContents.add(StoryContentFactory.create(this, jsonPage));
            Log.d("WELL STORY", jsonPage.toString());
        }
        return storyContents;
    }

    // HELPER METHODS
    private String getDefFilename() {
        return FILENAME_STORYDEF.format(String.valueOf(this.id));
    }

    private static StoryContentInterface getCurrentPage(ArrayList<StoryContentInterface> contents) {
        for (StoryContentInterface content: contents) {
            if (content.isCurrent()) { return content; }
        }
        return null;
    }
}
