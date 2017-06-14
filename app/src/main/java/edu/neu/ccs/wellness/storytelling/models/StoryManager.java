package edu.neu.ccs.wellness.storytelling.models;

import java.util.ArrayList;

import edu.neu.ccs.wellness.storytelling.interfaces.RestServerInterface;
import edu.neu.ccs.wellness.storytelling.interfaces.StoryInterface;
import edu.neu.ccs.wellness.storytelling.interfaces.StorytellingManagerInterface;
import edu.neu.ccs.wellness.storytelling.interfaces.UserAuthInterface;

/**
 * Created by hermansaksono on 6/13/17.
 */

public class StoryManager implements StorytellingManagerInterface {
    private RestServerInterface server;
    private UserAuthInterface user;
    private ArrayList<StoryInterface> storyList;
    private String lastRefreshDateTime;
    private StoryInterface currentStory;

    // PRIVATE CONSTRUCTORS
    private StoryManager(RestServerInterface server) {
        this.server = server;
        this.storyList = null;
        this.lastRefreshDateTime = null;
        this.currentStory = null;
    }

    private StoryManager(RestServerInterface server, ArrayList<StoryInterface> storyList) {
        this.server = server;
        this.storyList = storyList;
        this.lastRefreshDateTime = null;
        this.currentStory = getCurrentStoryFromList(storyList);
    }

    // STATIC FACTORY METHODS

    public static StoryManager create(RestServerInterface server) {
        // TODO If Json file is in the local storage, then load, otherwise set null
        return new StoryManager(server);
    }

    @Override
    public boolean isStoryListSet() {
        return (this.storyList == null);
    }

    @Override
    public ArrayList<StoryInterface> getStoryList() {
        return this.storyList;
    }

    @Override
    public void refreshStoryList() {
        // TODO make a connection and download the
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
    public StoryInterface getCurrentStory(int storyId) {
        return null;
    }

    @Override
    public UserAuthInterface getAuthUser() {
        return this.user;
    }

    // HELPER FUNCTIONS
    private static StoryInterface getCurrentStoryFromList (ArrayList<StoryInterface> storyList) {
        for (StoryInterface story:storyList) {
            if (story.isCurrent()) {
                return story;
            }
        }
        return null;
    }
}