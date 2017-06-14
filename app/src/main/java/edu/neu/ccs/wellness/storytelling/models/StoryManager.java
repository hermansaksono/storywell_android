package edu.neu.ccs.wellness.storytelling.models;

import java.util.ArrayList;

import edu.neu.ccs.wellness.storytelling.interfaces.StoryInterface;
import edu.neu.ccs.wellness.storytelling.interfaces.StorytellingManagerInterface;
import edu.neu.ccs.wellness.storytelling.interfaces.UserAuthInterface;

/**
 * Created by hermansaksono on 6/13/17.
 */

public class StoryManager implements StorytellingManagerInterface {

    public StorytellingManagerInterface create(UserAuthInterface user) {
        return null;
    }

    public boolean isStoryListSet() {
        return false;
    }

    public ArrayList<StoryInterface> getStoryList() {
        return null;
    }

    public void refreshStoryList() {

    }

    public String getLastStoryListRefreshDateTime() {
        return "1970-01-01";
    }

    public int getCurrentStoryId() {
        return 0;
    }

    public StoryInterface getStoryById(int storyId) {
        return null;
    }

    public UserAuthInterface getAuthUser() {
        return null;
    }
}