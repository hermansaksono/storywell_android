package edu.neu.ccs.wellness.storytelling.interfaces;

import java.util.List;

/**
 * Created by hermansaksono on 6/13/17.
 */

public interface StorytellingManagerInterface {

    public StorytellingManagerInterface create(UserAuthInterface user);

    public boolean isStoryListSet();

    public List<StoryInterface> getStoryList();

    public void refreshStoryList();

    public String getLastStoryListRefreshDateTime();

    public int getCurrentStoryId();

    public StoryInterface getStoryById(int storyId);

    public UserAuthInterface getAuthUser();
}
