package edu.neu.ccs.wellness.story.interfaces;

import java.util.List;

/**
 * Created by hermansaksono on 6/13/17.
 */

public interface StorytellingManager {

    boolean isStoryListSet();

    List<StoryInterface> getStoryList();

    String getLastStoryListRefreshDateTime();

    String getCurrentStoryId();

    StoryInterface getStoryById(String storyId) throws StorytellingException;

    StoryInterface getCurrentStory();
}
