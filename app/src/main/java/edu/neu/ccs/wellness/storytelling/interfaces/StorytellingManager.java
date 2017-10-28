package edu.neu.ccs.wellness.storytelling.interfaces;

import java.util.List;

/**
 * Created by hermansaksono on 6/13/17.
 */

public interface StorytellingManager {

    boolean isStoryListSet();

    List<StoryInterface> getStoryList();

    String getLastStoryListRefreshDateTime();

    int getCurrentStoryId();

    StoryInterface getStoryById(int storyId) throws StorytellingException;

    StoryInterface getCurrentStory();
}
