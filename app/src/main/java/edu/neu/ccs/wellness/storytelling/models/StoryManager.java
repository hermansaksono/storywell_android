package edu.neu.ccs.wellness.storytelling.models;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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

    public static final String DIR_STORIES = "stories/";
    public static final String DIR_CONTENTS = DIR_STORIES.concat("contents/");
    public static final String FILENAME_STORYDEF_LIST = DIR_STORIES.concat("storylist");

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
        this.currentStory = getCurrentStoryFromList(storyList);
    }

    // STATIC FACTORY METHODS
    public static StoryManager create(RestServerInterface server) {
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
        return this.currentStory.getStoryId();
    }

    @Override
    public StoryInterface getStoryById(int storyId) {
        for (StoryInterface story:this.storyList) {
            if (story.getStoryId() == storyId) {
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
        // TODO If no JSON File is in the internal storage
        if (isStoryListDataExists() == false) {
            downloadStoryListFromServer(context, this.server);
        }
        this.loadStoryListFromStorage(context);
    }

    // HELPER FUNCTIONS
    private void loadStoryListFromStorage (Context context) {
        try {
            String jsonString = readStoryListFromStorage(context);
            JSONObject jsonObject = new JSONObject(jsonString);
            this.dateTime = jsonObject.getString("datetime");
            this.storyList = getStoryListFromJSONArray(jsonObject.getJSONArray("stories"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static ArrayList<StoryInterface> getStoryListFromJSONArray(JSONArray jsonList)
            throws JSONException {
        ArrayList<StoryInterface> storyList = new ArrayList<StoryInterface>();
        for(int i = 0; i < jsonList.length(); i++) {
            JSONObject jsonStory = jsonList.getJSONObject(i);
            storyList.add(Story.create(jsonStory));
        }
        return storyList;
    }

    private static boolean isStoryListDataExists () {
        File file = new File(FILENAME_STORYDEF_LIST);
        return file.exists();
    }

    private static void saveStoryListData (Context context, String jsonString) {
        try {
            FileOutputStream fos = context.openFileOutput(FILENAME_STORYDEF_LIST,
                    Context.MODE_PRIVATE);
            fos.write(jsonString.getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void downloadStoryListFromServer(Context context, RestServerInterface server) {
        String jsonString = server.makeGetRequest(StoryManager.STORY_ALL);
        saveStoryListData(context, jsonString);
    }

    private static String readStoryListFromStorage (Context context) {
        StringBuffer sb = new StringBuffer("");
        try {
            FileInputStream fileInputStream = context.openFileInput(FILENAME_STORYDEF_LIST);
            InputStreamReader isReader = new InputStreamReader(fileInputStream);
            BufferedReader buffReader = new BufferedReader(isReader);
            String readString = buffReader.readLine ( ) ;
            while (readString != null) {
                sb.append(readString);
                readString = buffReader.readLine ( ) ;
            }
            isReader.close ( ) ;
        } catch ( IOException ioe ) {
            ioe.printStackTrace ( ) ;
        }
        return sb.toString();
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