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
        SynchronizedSetting setting = SynchronizedSettingRepository.getLocalInstance(context);
        this.unlockedChapters = setting.getStoryListInfo().getUnlockedStoryPages();
    }

    @Override
    public boolean isThisChapterUnlocked(String storyChapterId) {
        return this.unlockedChapters.contains(storyChapterId);
    }

    @Override
    public boolean setCurrentChallengeAsUnlocked(Context context) {
        SynchronizedSetting setting = SynchronizedSettingRepository.getLocalInstance(context);
        String storyChapterId = setting.getChallengeInfo().getCurrentChallengeId();

        if (storyChapterId != null) {
            this.unlockedChapters.add(storyChapterId);
            setting.getStoryListInfo().setUnlockedStoryPages(this.unlockedChapters);
            setting.getChallengeInfo().setCurrentChallengeId(null);
            SynchronizedSettingRepository.saveLocalAndRemoteInstance(setting, context);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean removeThisChapterAsUnlocked(String storyChapterId, Context context) {
        SynchronizedSetting setting = SynchronizedSettingRepository.getLocalInstance(context);

        if (this.unlockedChapters.contains(storyChapterId)) {
            this.unlockedChapters.remove(storyChapterId);
            setting.getStoryListInfo().setUnlockedStoryPages(this.unlockedChapters);
            SynchronizedSettingRepository.saveLocalAndRemoteInstance(setting, context);
            return true;
        } else {
            return false;
        }
    }
}
