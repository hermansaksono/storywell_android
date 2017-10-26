package edu.neu.ccs.wellness.storytelling.models;

import android.content.Context;
import android.os.Bundle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import edu.neu.ccs.wellness.server.RestServer;
import edu.neu.ccs.wellness.storytelling.interfaces.StoryContent;
import edu.neu.ccs.wellness.storytelling.interfaces.StoryInterface;

/**
 * Created by hermansaksono on 6/13/17.
 */

public class Story implements StoryInterface {
    public static final String KEY_STORY_ID = "STORY_ID";
    public static final String KEY_STORY_TITLE = "STORY_TITLE";
    public static final String KEY_STORY_COVER = "STORY_COVER_URL";
    public static final String KEY_STORY_DEF = "STORY_DEF_URL";
    public static final String KEY_STORY_IS_CURRENT = "STORY_IS_CURRENT";
    public static final String FILENAME_STORYDEF = "story__id_";

    private int id;
    private String title;
    private String coverUrl;
    private String defUrl;
    private ArrayList<StoryContent> contents;
    private StoryContent currentContent;
    private String lastRefreshDateTime;
    private boolean isCurrent = false;

    // CONSTRUCTORS
    /***
     * Regular constructor
     * @param id
     * @param title
     * @param coverUrl
     * @param defUrl
     * @param isCurrent
     */
    public Story(int id, String title, String coverUrl, String defUrl, boolean isCurrent) {
        this.id = id;
        this.title = title;
        this.coverUrl = coverUrl;
        this.defUrl = defUrl;
        this.isCurrent = isCurrent;
        this.contents = null;
        this.currentContent = null;
        this.lastRefreshDateTime = null;
    }

    /***
     * A factory method to create a Story object using a JSON object
     * @param jsonStory
     * @return
     * @throws JSONException
     */
    public static Story create(JSONObject jsonStory) throws JSONException {
        return new Story(
                jsonStory.getInt("id"),
                jsonStory.getString("title"),
                jsonStory.getString("cover_url"),
                jsonStory.getString("def_url"),
                jsonStory.getBoolean("is_current"));
    }

    /***
     * A factory method to create a Story object using a Bundle object
     * @param extras
     * @return
     */
    public static Story create(Bundle extras) {
        Story story = null;
        if (extras != null) {
            int id = extras.getInt(Story.KEY_STORY_ID);
            String title = extras.getString(Story.KEY_STORY_TITLE);
            String cover = extras.getString(Story.KEY_STORY_COVER);
            String def = extras.getString(Story.KEY_STORY_DEF);
            Boolean isCurrent = extras.getBoolean(Story.KEY_STORY_IS_CURRENT);

            story = new Story(id, title, cover, def, isCurrent);
        }
        return story;
    }

    // PUBLIC METHODS
    @Override
    public int getId() { return this.id; }

    @Override
    /****
     * Download the Story definition and put it to `content` member variable
     * @param context
     * @param server
     */
    public void loadStoryDef(Context context, RestServer server) {
        try {
            URL url = new URL(this.getDefUrl());
            String jsonString = server.doGetRequestUsingSaved(context, this.getDefFilename(), url);
            JSONObject jsonObject = new JSONObject(jsonString);
            this.contents = getStoryContentsFromJSONArray(jsonObject.getJSONArray("contents"));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /****
     * Download the Story definition to internal storage
     * @param context
     * @param server
     */

    public void downloadStoryDef(Context context, RestServer server) {
        try {
            URL url = new URL(this.defUrl);
            server.doGetRequestThenSave(context, this.getDefFilename(), url);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    @Override
    public boolean isContentSet() {
        return contents != null;
    }

    @Override
    public List<StoryContent> getContents() {
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

    @Override
    public String getCoverUrl() { return this.coverUrl; }

    @Override
    public String getDefUrl() { return this.defUrl; }

    @Override
    public String getDefFilename() { return FILENAME_STORYDEF.concat(String.valueOf(this.id)); }

    // PRIVATE HELPER METHODS
    /****
     * Download StoryContents that was defined in a JSON Array
     * @param contents
     * @return
     * @throws JSONException
     */
    private ArrayList<StoryContent> getStoryContentsFromJSONArray(JSONArray contents)
            throws JSONException {
        ArrayList<StoryContent> storyContents = new ArrayList<StoryContent>();
        for(int i = 0; i < contents.length(); i++) {
            JSONObject jsonPage = contents.getJSONObject(i);
            storyContents.add(StoryContentFactory.create(this, jsonPage));
        }
        return storyContents;
    }
}
