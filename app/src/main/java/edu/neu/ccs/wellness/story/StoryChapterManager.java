package edu.neu.ccs.wellness.story;

import android.content.Context;

import java.util.List;

import edu.neu.ccs.wellness.story.interfaces.StorytellingChapterManager;
import edu.neu.ccs.wellness.storytelling.settings.SynchronizedSetting;
import edu.neu.ccs.wellness.storytelling.settings.SynchronizedSettingRepository;

/**
 * Created by hermansaksono on 1/26/19.
 */

public class StoryChapterManager implements StorytellingChapterManager {

    private final List<String> unlockedChapters;

    public StoryChapterManager(Context context) {
        SynchronizedSetting setting = SynchronizedSettingRepository.getInstance(context);
        this.unlockedChapters = setting.getCompletedChallenges();
    }

    @Override
    public boolean isThisChapterUnlocked(String storyChapterId) {
        return this.unlockedChapters.contains(storyChapterId);
    }

    @Override
    public void setCurrentChallengeAsUnlocked(Context context) {
        SynchronizedSetting setting = SynchronizedSettingRepository.getInstance(context);
        String storyChapterId = setting.getCurrentChallengeId();

        if (storyChapterId != null) {
            this.unlockedChapters.add(storyChapterId);
            setting.setCompletedChallenges(this.unlockedChapters);
            setting.setCurrentChallengeId(null);
            SynchronizedSettingRepository.saveInstance(setting, context);
        }

    }

    @Override
    public boolean removeThisChapterAsUnlocked(String storyChapterId, Context context) {
        SynchronizedSetting setting = SynchronizedSettingRepository.getInstance(context);

        if (this.unlockedChapters.contains(storyChapterId)) {
            this.unlockedChapters.remove(storyChapterId);
            setting.setCompletedChallenges(this.unlockedChapters);
            SynchronizedSettingRepository.saveInstance(setting, context);
            return true;
        } else {
            return false;
        }
    }
}
