package edu.neu.ccs.wellness.storytelling.models;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import edu.neu.ccs.wellness.storytelling.interfaces.RestServer;
import edu.neu.ccs.wellness.storytelling.interfaces.StoryContent;
import edu.neu.ccs.wellness.storytelling.interfaces.StoryInterface;
import edu.neu.ccs.wellness.storytelling.interfaces.StorytellingException;
import edu.neu.ccs.wellness.storytelling.interfaces.StorytellingManager;

/**
 * Created by hermansaksono on 6/13/17.
 */

public class Story implements StoryInterface {
    public static final String FILENAME_STORYDEF = "story__id_%d";

    private static final String EXC_STORY_UNINITIALIZED = "Story has not been initialized";

    private int id;
    private String title;
    private String coverUrl;
    private String defUrl;
    private ArrayList<StoryContent> contents;
    private StoryContent currentContent;
    private String lastRefreshDateTime;
    private boolean isCurrent = false;
    private StorytellingManager storyManager;

    public Story(int id, String title, String coverUrl, String defUrl, boolean isCurrent) {
        this.id = id;
        this.title = title;
        this.coverUrl = coverUrl;
        this.defUrl = defUrl;
        this.isCurrent = isCurrent;
        this.contents = null;
        this.currentContent = null;
        this.lastRefreshDateTime = null;
        //this.storyManager = storyManager;
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
    public void loadStoryDef(Context context, RestServer server) {
        try {
            String jsonString = server.loadGetRequest(context, this.getDefFilename(), this.defUrl);
            JSONObject jsonObject = new JSONObject(jsonString);
            this.contents = getStoryContentsFromJSONArray(jsonObject.getJSONArray("contents"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /***
     * If the contents have been initialized, then download the contents of each StoryContent.
     * @param context
     */
    @Override
    public void loadStoryContents(Context context, RestServer server)
            throws StorytellingException {
        if (this.isContentSet()) {
            for (StoryContent content: this.contents) {
                content.downloadFiles(context, server);
            }
        }
        else {
            throw new StorytellingException(EXC_STORY_UNINITIALIZED);
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
    public String getCoverUrl() {
        return this.coverUrl;
    }

    /***
     * Download Story contents
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

    // HELPER METHODS
    private String getDefFilename() {
        return FILENAME_STORYDEF.format(String.valueOf(this.id));
    }

    private static StoryContent getCurrentPage(ArrayList<StoryContent> contents) {
        for (StoryContent content: contents) {
            if (content.isCurrent()) { return content; }
        }
        return null;
    }
}
