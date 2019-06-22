package edu.neu.ccs.wellness.story;

import android.content.Context;

import com.crashlytics.android.Crashlytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.neu.ccs.wellness.server.RestServer;
import edu.neu.ccs.wellness.story.interfaces.StoryInterface;
import edu.neu.ccs.wellness.story.interfaces.StorytellingManager;
import edu.neu.ccs.wellness.story.interfaces.StorytellingException;
import edu.neu.ccs.wellness.storytelling.utils.StorySetting;

/**
 * Created by hermansaksono on 6/13/17.
 */

public class StoryManager implements StorytellingManager {
    public static final String STORY_ALL = "group/stories/all";

    public static final String FILENAME_STORY_LIST = "story_list.json";

    private static final String EXC_STORIES_UNINITIALIZED = "Story list has not been initialized";
    private static final String EXC_STORY_EXIST_FALSE = "Story does not exist";

    private RestServer server;
    private ArrayList<StoryInterface> storyList;
    private String lastRefreshDateTime;
    private StoryInterface currentStory;
    private String dateTime;

    // PRIVATE CONSTRUCTORS
    private StoryManager(RestServer server, ArrayList<StoryInterface> storyList) {
        this.server = server;
        this.storyList = storyList;
        this.lastRefreshDateTime = null;
        //this.currentStory = getCurrentStoryFromList(storyList);
    }

    // STATIC FACTORY METHODS
    public static StoryManager create(RestServer server) {
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
    public String getCurrentStoryId() {
        return this.currentStory.getId();
    }

    @Override
    public StoryInterface getStoryById(String storyId) throws StorytellingException {
        for (StoryInterface story:this.storyList) {
            if (story.getId().equals(storyId)) {
                return story;
            }
        }
        throw new StorytellingException(EXC_STORY_EXIST_FALSE);
    }

    @Override
    public StoryInterface getCurrentStory() { return this.currentStory; }

    /**
     * Checks if internet connection is available
     * @param context Android application Context
     * @return true if internet connection is available, otherwise return false
     */
    public boolean canAccessServer (Context context) { return this.server.isOnline(context); }

    /**
     * If the storyListFile doesn't exist in the internal storage, then do an HTTP GET request
     * from the server and save the response to the internal storage.
     * Then, load the storyListFile from internal storage, then convert it to a JSON object.
     * Finally generate a list of uninitialized Story objects from the JSON object and assign it to
     * the instance's storyList.
     * @param context The Android context to assist saving files to internal storage.
     * @param isUseSaved If true, then {@link StoryManager} will try to use the cached story list.
     *                   Otherwise, download a new one.
     */
    public void loadStoryList(Context context, boolean isUseSaved) {
        try {
            String jsonString = this.server
                    .doGetRequestFromAResource(context, FILENAME_STORY_LIST, STORY_ALL, isUseSaved);
            JSONObject jsonObject = new JSONObject(jsonString);
            this.storyList = this.getStoryListFromJSONArray(jsonObject.getJSONArray("stories"));
            this.storyList.add(new StorySetting("SETTING"));
        } catch (JSONException e) {
            Crashlytics.logException(e);
            e.printStackTrace();
        } catch (IOException e) {
            Crashlytics.logException(e);
            e.printStackTrace();
        }
    }

    /**
     * Delete the story definitions.
     * INVARIANT: the storylist has been populated.
     * @param context
     * @return
     */
    public boolean deleteStoryDefinitions(Context context) {
        if (this.storyList == null) {
            return false;
        } else {
            for (StoryInterface story : this.storyList) {
                story.deleteStoryDef(context, this.server);
            }
            return true;
        }
    }

    public boolean isStoryListCacheExists(Context context) {
        return this.server.isFileExists(context, FILENAME_STORY_LIST);
    }

    /***
     * If storyList has been initialized, then iterate on the stories to load each Story
     * definitions.
     * @param context The Android context to assist saving files to internal storage.
     */
    public void downloadStoryDef (Context context, int position) throws StorytellingException {
        if (isStoryListSet()) {
            Story story = (Story) this.storyList.get(position);
            story.downloadStoryDef(context, this.server);
        }
        else {
            throw new StorytellingException(EXC_STORIES_UNINITIALIZED);
        }
    }

    // STATIC METHODS
    /**
     * Delete the story list and definitions.
     * INVARIANT: the storylist has been populated.
     * @param context
     * @return
     */
    public static boolean deleteStoryDefs(Context context, RestServer server) {

        StoryManager storyManager = StoryManager.create(server);
        storyManager.loadStoryList(context, true);
        storyManager.deleteStoryDefinitions(context);

        if (server.isFileExists(context, FILENAME_STORY_LIST)) {
            context.deleteFile(FILENAME_STORY_LIST);
        }

        return true;
    }

    // HELPER FUNCTIONS
    private ArrayList<StoryInterface> getStoryListFromJSONArray(JSONArray jsonList)
            throws JSONException {
        ArrayList<StoryInterface> storyList = new ArrayList<StoryInterface>();
        for(int i = 0; i < jsonList.length(); i++) {
            JSONObject jsonStory = jsonList.getJSONObject(i);
            //storyList.add(Story.newInstance(jsonStory));
            Story story = Story.newInstance(jsonStory.toString());
            //Log.d("WELL Story", story.getTitle());
            storyList.add(story);
        }
        return storyList;
    }
}