package edu.neu.ccs.wellness.storytelling.utils;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import edu.neu.ccs.wellness.story.StoryChallenge;
import edu.neu.ccs.wellness.story.StoryMemo;
import edu.neu.ccs.wellness.story.StoryReflection;
import edu.neu.ccs.wellness.story.interfaces.StoryContent;
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
    public static final boolean DEFAULT_CONTENT_ALLOW_EDIT = true;


    // Reverted back the code as it was leading to refactoring for multiple classes and would
    // lead to variation in timed goals
    // public static Fragment getFragment(StoryContent storyContent, boolean isResponseExists) {
    public static Fragment getFragment(StoryContent storyContent) {
        switch (storyContent.getType()) {
            case COVER:
                return createCover(storyContent);
            case PAGE:
                return createPage(storyContent);
            case REFLECTION:
                return createReflection(storyContent);
            case STATEMENT:
                return createStatement(storyContent);
            case CHALLENGE:
                return createChallenge(storyContent);
            case MEMO:
                return createMemo(storyContent);
            default:
                return createCover(storyContent);
        }
    }

    private static Fragment createCover(StoryContent content) {
        Fragment fragment = new StoryCoverFragment();
        fragment.setArguments(getBundle(content));
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


    private static Fragment createChallenge(StoryContent content) {
        Fragment fragment = new ChallengePickerFragment();
        StoryChallenge storyChallenge = (StoryChallenge) content;

        Bundle args = getBundle(content);
        args.putBoolean(KEY_IS_SHOW_REF_START, storyChallenge.isLocked());

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
        args.putString(StoryMemo.KEY_PAGE_ID_TO_UNLOCK, storyMemo.getPageIdToUnlock());

        fragment.setArguments(args);
        return fragment;
    }

    // PRIVATE HELPER METHODS
    private static Bundle getBundle(StoryContent content) {
        Bundle args = new Bundle();
        args.putInt(KEY_ID, content.getId());
        args.putString(KEY_IMG_URL, content.getImageURL());
        args.putString(KEY_TEXT, content.getText());
        args.putString(KEY_SUBTEXT, content.getSubtext());
        return args;
    }

    private static Bundle getBundleForReflection (StoryContent content, boolean isResponseExists) {
        Bundle args = getBundle(content);
        args.putBoolean(KEY_IS_RESPONSE_EXIST, isResponseExists);
        return args;
    }
}
