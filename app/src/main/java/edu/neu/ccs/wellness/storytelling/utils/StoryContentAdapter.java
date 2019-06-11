package edu.neu.ccs.wellness.storytelling.utils;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;

import edu.neu.ccs.wellness.fitness.interfaces.ChallengeManagerInterface;
import edu.neu.ccs.wellness.fitness.interfaces.ChallengeStatus;
import edu.neu.ccs.wellness.story.StoryChallenge;
import edu.neu.ccs.wellness.story.StoryCover;
import edu.neu.ccs.wellness.story.StoryMemo;
import edu.neu.ccs.wellness.story.StoryReflection;
import edu.neu.ccs.wellness.story.interfaces.StoryContent;
import edu.neu.ccs.wellness.story.interfaces.StoryInterface;
import edu.neu.ccs.wellness.storytelling.Storywell;
import edu.neu.ccs.wellness.storytelling.settings.SynchronizedSetting;
import edu.neu.ccs.wellness.storytelling.storyview.ActionIncrementFragment;
import edu.neu.ccs.wellness.storytelling.storyview.ChallengePickerFragment;
import edu.neu.ccs.wellness.storytelling.storyview.MemoFragment;
import edu.neu.ccs.wellness.storytelling.storyview.ReflectionFragment;
import edu.neu.ccs.wellness.storytelling.storyview.StatementFragment;
import edu.neu.ccs.wellness.storytelling.storyview.StoryCoverFragment;
import edu.neu.ccs.wellness.storytelling.storyview.StoryPageFragment;


public class StoryContentAdapter {

    public static final String KEY_ID = "KEY_ID";
    public static final String KEY_IMG_URL = "KEY_IMG_URL";
    public static final String KEY_TEXT = "KEY_TEXT";
    public static final String KEY_SUBTEXT = "KEY_SUBTEXT";
    public static final String KEY_IS_RESPONSE_EXIST = "KEY_IS_RESPONSE_EXIST";
    public static final String KEY_IS_SHOW_REF_START = "KEY_IS_SHOW_REF_START";
    public static final String KEY_CONTENT_GROUP = "KEY_CONTENT_GROUP";
    public static final String KEY_CONTENT_GROUP_NAME = "KEY_CONTENT_GROUP_NAME";
    public static final String KEY_CONTENT_ALLOW_EDIT = "KEY_CONTENT_ALLOW_EDIT";
    public static final String KEY_IS_LOCKED = "KEY_IS_LOCKED";
    public static final String KEY_IS_ACTIONABLE = "KEY_IS_ACTIONABLE";
    public static final String KEY_CHALLENGE_PICKER_STATE = "KEY_CHALLENGE_PICKER_STATE";
    public static final String KEY_REFLECTION_DATE = "KEY_REFLECTION_DATE";
    public static final String KEY_STORY_ID = "KEY_STORY_ID";
    public static final boolean DEFAULT_CONTENT_ALLOW_EDIT = true;


    // Reverted back the code as it was leading to refactoring for multiple classes and would
    // lead to variation in timed goals
    // public static Fragment getFragment(StoryContent storyContent, boolean isResponseExists) {
    public static Fragment getFragment(StoryContent storyContent, Context context) {
        switch (storyContent.getType()) {
            case COVER:
                return createCover(storyContent, context);
            case PAGE:
                return createPage(storyContent);
            case REFLECTION:
                return createReflection(storyContent);
            case STATEMENT:
                return createStatement(storyContent);
            case CHALLENGE:
                return createChallenge(storyContent, context);
            case MEMO:
                return createMemo(storyContent);
            case ACTION_INCREMENT:
                return createActionIncrement(storyContent);
            default:
                return createPage(storyContent);
        }
    }

    private static Fragment createCover( StoryContent content, Context context) {
        Fragment fragment = new StoryCoverFragment();
        Bundle args = getBundle(content);

        args.putBoolean(KEY_IS_LOCKED, getIsLockedStatus((StoryCover) content, context));
        fragment.setArguments(args);

        return fragment;
    }

    private static Fragment createPage(StoryContent content) {
        Fragment fragment = new StoryPageFragment();
        fragment.setArguments(getBundle(content));
        return fragment;
    }


    private static Fragment createReflection(StoryContent content) {
        Fragment fragment = new ReflectionFragment();
        StoryReflection storyReflection = (StoryReflection) content;

        Bundle args = getBundle(content);
        args.putBoolean(KEY_IS_SHOW_REF_START, storyReflection.isShowReflectionStart());
        args.putString(KEY_CONTENT_GROUP, storyReflection.getGroupId());
        args.putString(KEY_CONTENT_GROUP_NAME, storyReflection.getGroupName());

        fragment.setArguments(args);
        return fragment;
    }

    private static Fragment createChallenge(StoryContent content, Context context) {
        Fragment fragment = new ChallengePickerFragment();
        StoryChallenge storyChallenge = (StoryChallenge) content;

        Bundle args = getBundle(content);
        args.putBoolean(KEY_IS_SHOW_REF_START, storyChallenge.isLocked());
        args.putInt(KEY_CHALLENGE_PICKER_STATE, getChallengePickerState(storyChallenge, context));

        fragment.setArguments(args);
        return fragment;
    }

    private static Fragment createStatement(StoryContent content) {
        Fragment fragment = new StatementFragment();
        fragment.setArguments(getBundle(content));
        return fragment;
    }

    private static Fragment createMemo(StoryContent content) {
        Fragment fragment = new MemoFragment();
        StoryMemo storyMemo = (StoryMemo) content;

        Bundle args = getBundle(content);
        args.putString(StoryMemo.KEY_STORY_ID_TO_UNLOCK, storyMemo.getStoryIdToUnlock());
        args.putString(StoryMemo.KEY_PAGE_ID_TO_UNLOCK, storyMemo.getPageIdToUnlock());

        fragment.setArguments(args);
        return fragment;
    }

    private static Fragment createActionIncrement(StoryContent content) {
        Fragment fragment = new ActionIncrementFragment();
        fragment.setArguments(getBundle(content));
        return fragment;
    }

    // PRIVATE HELPER METHODS
    private static Bundle getBundle(StoryContent content) {
        Bundle args = new Bundle();
        args.putInt(KEY_ID, content.getId());
        args.putString(KEY_IMG_URL, content.getImageURL());
        args.putString(KEY_TEXT, content.getText());
        args.putString(KEY_SUBTEXT, content.getSubtext());
        args.putBoolean(KEY_IS_LOCKED, content.isLocked());
        return args;
    }

    private static Bundle getBundleForReflection (StoryContent content, boolean isResponseExists) {
        Bundle args = getBundle(content);
        args.putBoolean(KEY_IS_RESPONSE_EXIST, isResponseExists);
        return args;
    }

    private static boolean getIsLockedStatus(StoryCover storyCover, Context context) {
        Storywell storywell = new Storywell(context);
        SynchronizedSetting setting = storywell.getSynchronizedSetting();
        List<String> unlockedStories = setting.getStoryListInfo().getUnlockedStories();
        String storyId = storyCover.getStoryId();
        boolean isOnTheUnlockedList = unlockedStories.contains(storyId);

        if (isOnTheUnlockedList) {
            return false;
        } else {
            return storyCover.isLocked();
        }
    }

    private static int getChallengePickerState(StoryChallenge storyChallenge, Context context) {
        String challengePageId = storyChallenge.getStoryPageId();

        Storywell storywell = new Storywell(context);
        SynchronizedSetting setting = storywell.getSynchronizedSetting();
        String storyPageIdToBeUnlocked = setting.getStoryChallengeInfo().getChapterIdToBeUnlocked();
        List<String> unlockedStoryPages = setting.getStoryListInfo().getUnlockedStoryPages();

        if (unlockedStoryPages.contains(challengePageId)) {
            return ChallengePickerFragment.CHALLENGE_STATUS_COMPLETED;
        }

        if (isRunningChallengeExists(storywell.getChallengeManager())) {
            if (challengePageId.equals(storyPageIdToBeUnlocked)) {
                return ChallengePickerFragment.CHALLENGE_STATUS_RUNNING;
            } else {
                return ChallengePickerFragment.CHALLENGE_STATUS_OTHER_IS_RUNNING;
            }
        } else {
            return ChallengePickerFragment.CHALLENGE_STATUS_UNSTARTED;
        }
    }

    private static boolean isRunningChallengeExists(ChallengeManagerInterface challengeManager) {
        try {
            ChallengeStatus challengeStatus = challengeManager.getStatus();
            switch (challengeStatus) {
                case UNSYNCED_RUN:
                    // pass
                case RUNNING:
                    // pass
                case PASSED:
                    return true;
                default:
                    return false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
