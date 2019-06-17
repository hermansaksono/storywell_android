package edu.neu.ccs.wellness.story;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import edu.neu.ccs.wellness.people.GroupInterface;
import edu.neu.ccs.wellness.server.RestServer;
import edu.neu.ccs.wellness.server.RestServer.ResponseType;
import edu.neu.ccs.wellness.server.WellnessUser;
import edu.neu.ccs.wellness.story.interfaces.StoryContent;
import edu.neu.ccs.wellness.story.interfaces.StoryInterface;
import edu.neu.ccs.wellness.story.interfaces.StoryType;

public class Story implements StoryInterface {
    public static final String KEY_STORY_ID = "STORY_ID";
    public static final String KEY_STORY_TITLE = "STORY_TITLE";
    public static final String KEY_STORY_COVER = "STORY_COVER_URL";
    public static final String KEY_STORY_DEF = "STORY_DEF_URL";
    public static final String KEY_STORY_IS_CURRENT = "STORY_IS_CURRENT";
    public static final String KEY_STORY_IS_LOCKED = "STORY_IS_LOCKED";
    public static final String KEY_STORY_NEXT_STORY_ID = "STORY_NEXT_STORY_ID";
    public static final String KEY_REFLECTION_LIST = "KEY_REFLECTION_LIST";
    public static final String KEY_RESPONSE_TIMESTAMP = "KEY_RESPONSE_TIMESTAMP";
    public static final String FILENAME_STORYDEF = "story__s%s.json";
    public static final String JSON_CONTENTS = "contents";

    @SerializedName("id")
    private String id;

    @SerializedName("title")
    private String title;

    @SerializedName("cover_url")
    private String coverUrl;

    @SerializedName("def_url")
    private String defUrl;

    @SerializedName("is_current")
    private boolean isCurrent = false;

    @SerializedName("is_locked")
    private boolean isLocked;

    @SerializedName("next_story_id")
    private String nextStoryId;

    private ArrayList<StoryContent> contents = null;
    private String lastRefreshDateTime = null;

    // CONSTRUCTORS
    /***
     * Hidden regular constructor
     * @param id
     * @param title
     * @param coverUrl
     * @param defUrl
     * @param isCurrent
     */
    private Story(String id, String title, String coverUrl, String defUrl, boolean isCurrent,
                  boolean isLocked, String nextStoryId) {
        this.id = id;
        this.title = title;
        this.coverUrl = coverUrl;
        this.defUrl = defUrl;
        this.isCurrent = isCurrent;
        this.lastRefreshDateTime = null;
        this.isLocked = isLocked;
        this.nextStoryId = nextStoryId;
    }

    private Story() {

    }

    /***
     * Create a new Story instance using a JSON String
     * @param json A String that represent a Story object
     * @return A new Story instance
     */
    public static Story newInstance(String json) {
        return new Gson().fromJson(json, Story.class);
    }

    /***
     * A factory method to newInstance a Story object using a Bundle object
     * @param bundle
     * @return
     */
    public static Story create(Bundle bundle) {
        Story story = null;
        if (bundle != null) {
            String id = bundle.getString(Story.KEY_STORY_ID);
            String title = bundle.getString(Story.KEY_STORY_TITLE);
            String cover = bundle.getString(Story.KEY_STORY_COVER);
            String def = bundle.getString(Story.KEY_STORY_DEF);
            Boolean isCurrent = bundle.getBoolean(Story.KEY_STORY_IS_CURRENT);
            Boolean isLocked = bundle.getBoolean(Story.KEY_STORY_IS_LOCKED);
            String nextStoryId = bundle.getString(Story.KEY_STORY_NEXT_STORY_ID);

            story = new Story(id, title, cover, def, isCurrent, isLocked, nextStoryId);
        }
        return story;
    }

    // PUBLIC METHODS
    @Override
    public ResponseType tryLoadStoryDef(Context context, RestServer server, GroupInterface group) {
        if (server.isFileExists(context, this.getDefFilename())) {
            this.fetchStoryDef(context, server, group);
            return RestServer.ResponseType.SUCCESS_202;
        } else if (server.isOnline(context)) {
            return this.fetchStoryDef(context, server, group);
        } else {
            return RestServer.ResponseType.NO_INTERNET;
        }
    }


    /***
     * Download the Story definition and put it to `content` member variable
     * @param context
     * @param server
     * @param group
     * @return
     */
    @Override
    public ResponseType fetchStoryDef(Context context, RestServer server, GroupInterface group) {
        try {
            URL url = new URL(this.getDefUrl());
            String jsonString = server.doGetRequestUsingSaved(context, this.getDefFilename(), url);
            JSONObject jsonObject = new JSONObject(jsonString);

            this.contents = getStoryContentsFromJSONArray(jsonObject.getJSONArray(JSON_CONTENTS));
            return ResponseType.SUCCESS_202;
        } catch (JSONException e) {
            e.printStackTrace();
            return ResponseType.BAD_JSON;
        } catch (IOException e) {
            e.printStackTrace();
            if (e.getMessage().equals(WellnessUser.ERROR_REFRESH_TOKEN_MISSING)) {
                return ResponseType.LOGIN_EXPIRED;
            } else {
                return ResponseType.BAD_REQUEST_400;
            }
        }
    }

    /***
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

    /***
     * Delete the Story definition from internal storage
     * @param context
     * @param server
     */
    @Override
    public boolean deleteStoryDef(Context context, RestServer server) {
        String storyDefFileName = this.getDefFilename();
        if (server.isFileExists(context, storyDefFileName)) {
            context.deleteFile(storyDefFileName);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String getId() { return this.id; }

    @Override
    public String getTitle() {
        return this.title;
    }

    @Override
    public String getCoverUrl() { return this.coverUrl; }

    @Override
    public String getDefUrl() { return this.defUrl; }

    @Override
    public String getDefFilename() { return String.format(FILENAME_STORYDEF, this.id); }

    @Override
    public List<StoryContent> getContents() { return this.contents; }

    @Override
    public StoryContent getContentByIndex(int index) {
        if (index < this.contents.size()) {
            return this.contents.get(index);
        } else {
            return this.contents.get(this.contents.size() - 1);
        }
    }

    @Override
    public StoryType getStoryType() { return StoryType.STORY; }

    @Override
    public boolean isContentSet() {
        return this.contents != null;
    }

    @Override
    public String getRefreshDateTime() {
        return this.lastRefreshDateTime;
    }

    @Override
    public boolean isLocked() {
        return this.isLocked;
    }

    @Override
    public String getNextStoryId() {
        return this.nextStoryId;
    }

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