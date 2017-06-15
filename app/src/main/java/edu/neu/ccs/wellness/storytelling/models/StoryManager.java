package edu.neu.ccs.wellness.storytelling.models;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import edu.neu.ccs.wellness.storytelling.interfaces.RestServerInterface;
import edu.neu.ccs.wellness.storytelling.interfaces.StoryInterface;
import edu.neu.ccs.wellness.storytelling.interfaces.StorytellingManagerInterface;
import edu.neu.ccs.wellness.storytelling.interfaces.UserAuthInterface;

/**
 * Created by hermansaksono on 6/13/17.
 */

public class StoryManager implements StorytellingManagerInterface {
    public static final String STORY_ALL = "group/stories/all";
    public static final String PREFS_NAME = "WELLNESS_STORYTELLING";

    public static final String FILENAME_STORY_LIST = "story_list";

    private RestServerInterface server;
    private UserAuthInterface user;
    private ArrayList<StoryInterface> storyList;
    private String lastRefreshDateTime;
    private StoryInterface currentStory;
    private String dateTime;

    // PRIVATE CONSTRUCTORS
    private StoryManager(RestServerInterface server, ArrayList<StoryInterface> storyList) {
        this.server = server;
        this.storyList = storyList;
        this.lastRefreshDateTime = null;
        //this.currentStory = getCurrentStoryFromList(storyList);
    }

    // STATIC FACTORY METHODS
    public static StoryManager create(RestServerInterface server) {
        // TODO If Json file is in the local storage, then load, otherwise set null
        return new StoryManager(server, null);
    }

    public static StoryManager createContentItem(RestServerInterface server) {
        // TODO If Json file is in the local storage, then load, otherwise set null
        return new StoryManager(server, null);
    }

    // PUBLIC METHODS
    @Override
    public boolean isStoryListSet() {
        return (this.storyList != null);
    }

    @Override
    public ArrayList<StoryInterface> getStoryList() {
        return this.storyList;
    }

    @Override
    public String getLastStoryListRefreshDateTime() {
        return this.lastRefreshDateTime;
    }

    @Override
    public int getCurrentStoryId() {
        return this.currentStory.getId();
    }

    @Override
    public StoryInterface getStoryById(int storyId) {
        for (StoryInterface story:this.storyList) {
            if (story.getId() == storyId) {
                return story;
            }
        }
        return null;
    }

    @Override
    public StoryInterface getCurrentStory() { return this.currentStory; }

    @Override
    public UserAuthInterface getAuthUser() {
        return this.user;
    }

    public void loadStoryList(Context context) {
        try {
            String jsonString = this.server.loadGetRequest(context, FILENAME_STORY_LIST, STORY_ALL);
            JSONObject jsonObject = new JSONObject(jsonString);
            this.storyList = getStoryListFromJSONArray(jsonObject.getJSONArray("stories"));

            Log.d("WELL SM", jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // HELPER FUNCTIONS
    private static ArrayList<StoryInterface> getStoryListFromJSONArray(JSONArray jsonList)
            throws JSONException {
        ArrayList<StoryInterface> storyList = new ArrayList<StoryInterface>();
        for(int i = 0; i < jsonList.length(); i++) {
            JSONObject jsonStory = jsonList.getJSONObject(i);
            storyList.add(Story.create(jsonStory));

            Log.d("WELL SM Iter", jsonStory.toString());
        }
        return storyList;
    }

    private static StoryInterface getCurrentStoryFromList (ArrayList<StoryInterface> storyList) {
        for (StoryInterface story:storyList) {
            if (story.isCurrent()) {
                return story;
            }
        }
        return null;
    }
}