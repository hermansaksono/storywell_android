package edu.neu.ccs.wellness.storytelling.utils;

import android.content.Context;

import java.util.List;

import edu.neu.ccs.wellness.people.GroupInterface;
import edu.neu.ccs.wellness.server.RestServer;
import edu.neu.ccs.wellness.story.interfaces.StoryContent;
import edu.neu.ccs.wellness.story.interfaces.StoryInterface;
import edu.neu.ccs.wellness.story.interfaces.StoryType;

/**
 * Created by hermansaksono on 11/6/17.
 */

public class StorySetting implements StoryInterface {

    private String id;

    public StorySetting (String storyId) { this.id = storyId; }

    @Override
    public String getId() { return this.id; }

    @Override
    public RestServer.ResponseType tryLoadStoryDef(Context context, RestServer server, GroupInterface group) {
        return RestServer.ResponseType.NO_INTERNET;
    };

    @Override
    public RestServer.ResponseType fetchStoryDef(Context context, RestServer server, GroupInterface group) {
        return RestServer.ResponseType.NO_INTERNET;
    }

    @Override
    public boolean deleteStoryDef(Context context, RestServer server) {
        return true; // Don't do anything
    }

    @Override
    public String getTitle() { return "About Storywell"; }

    @Override
    public boolean isContentSet() { return false; }

    @Override
    public List<StoryContent> getContents() { return null; }

    @Override
    public StoryContent getContentByIndex(int index) { return null; }

    @Override
    public String getRefreshDateTime() { return null; }

    @Override
    public boolean isLocked() {
        return false;
    }

    @Override
    public String getNextStoryId() {
        return null;
    }

    @Override
    public String getCoverUrl() { return "art_book_cover_info"; }

    @Override
    public String getDefUrl() { return null; }

    @Override
    public String getDefFilename() { return null; }

    @Override
    public StoryType getStoryType() { return StoryType.APP; }
}
