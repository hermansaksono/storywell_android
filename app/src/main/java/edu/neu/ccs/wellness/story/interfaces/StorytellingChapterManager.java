package edu.neu.ccs.wellness.story.interfaces;

import android.content.Context;

/**
 * Created by hermansaksono on 1/26/19.
 */

public interface StorytellingChapterManager {

    boolean isThisChapterUnlocked(String storyChapterId);

    boolean setCurrentChallengeAsUnlocked(Context context);

    boolean removeThisChapterAsUnlocked(String storyChapterId, Context context);

    boolean setThisStoryPageForChallenge(StoryInterface story, String storyPageIdToUnlock,
                                         Context context);
}
