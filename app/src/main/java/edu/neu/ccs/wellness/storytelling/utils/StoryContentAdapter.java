package edu.neu.ccs.wellness.storytelling.utils;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import edu.neu.ccs.wellness.story.interfaces.StoryContent;
import edu.neu.ccs.wellness.story.interfaces.StoryContent.ContentType;
import edu.neu.ccs.wellness.storytelling.storyview.ChallengeInfoFragment;
import edu.neu.ccs.wellness.storytelling.storyview.ChallengePickerFragment;
import edu.neu.ccs.wellness.storytelling.storyview.ChallengeSummaryFragment;
import edu.neu.ccs.wellness.storytelling.storyview.ReflectionFragment;
import edu.neu.ccs.wellness.storytelling.storyview.ReflectionStartFragment;
import edu.neu.ccs.wellness.storytelling.storyview.StatementFragment;
import edu.neu.ccs.wellness.storytelling.storyview.StoryCoverFragment;
import edu.neu.ccs.wellness.storytelling.storyview.StoryPageFragment;


public class StoryContentAdapter {

    public static final String KEY_ID = "KEY_ID";
    public static final String KEY_IMG_URL = "KEY_IMG_URL";
    public static final String KEY_TEXT = "KEY_TEXT";
    public static final String KEY_SUBTEXT = "KEY_SUBTEXT";
    public static final String KEY_IS_RESPONSE_EXIST = "KEY_IS_RESPONSE_EXIST";


    //Reverted back the code as it was leading to refactoring for multiple classes and would lead to variation in timed goals
    public static Fragment getFragment(StoryContent storyContent, boolean isResponseExists) {
        Fragment storyContentFragment = null;
        if (storyContent.getType().equals(ContentType.COVER)) {
            storyContentFragment = createCover(storyContent);
        } else if (storyContent.getType().equals(ContentType.PAGE)) {
            storyContentFragment = createPage(storyContent);
        } else if (storyContent.getType().equals(ContentType.REFLECTION_START)) {
            storyContentFragment = createReflectionStart(storyContent);
        } else if (storyContent.getType().equals(ContentType.REFLECTION)) {
            storyContentFragment = createReflection(storyContent, isResponseExists);
        } else if (storyContent.getType().equals(ContentType.STATEMENT)) {
            storyContentFragment = createStatement(storyContent);
        } else if (storyContent.getType().equals(ContentType.CHALLENGE_INFO)) {
            storyContentFragment = createChallengeInfo(storyContent);
        } else if (storyContent.getType().equals(ContentType.CHALLENGE)) {
            storyContentFragment = createChallenge(storyContent);
        } else if (storyContent.getType().equals(ContentType.CHALLENGE_SUMMARY)) {
            storyContentFragment = createChallengeSummary(storyContent);
        }
        return storyContentFragment;
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

    private static Fragment createReflectionStart(StoryContent content) {
        Fragment fragment = new ReflectionStartFragment();
        fragment.setArguments(getBundle(content));
        return fragment;
    }


    private static Fragment createReflection(StoryContent content, boolean isResponseExists) {
        Fragment fragment = new ReflectionFragment();
        fragment.setArguments(getBundleForReflection(content, isResponseExists));
        return fragment;
    }


    private static Fragment createChallenge(StoryContent content) {
        Fragment fragment = new ChallengePickerFragment();
        fragment.setArguments(getBundle(content));
        return fragment;
    }

    private static Fragment createStatement(StoryContent content) {
        Fragment fragment = new StatementFragment();
        fragment.setArguments(getBundle(content));
        return fragment;
    }

    private static Fragment createChallengeInfo(StoryContent content) {
        Fragment fragment = new ChallengeInfoFragment();
        fragment.setArguments(getBundle(content));
        return fragment;
    }

    private static Fragment createChallengeSummary(StoryContent content) {
        Fragment fragment = new ChallengeSummaryFragment();
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
        return args;
    }

    private static Bundle getBundleForReflection (StoryContent content, boolean isResponseExists) {
        Bundle args = getBundle(content);
        args.putBoolean(KEY_IS_RESPONSE_EXIST, isResponseExists);
        return args;
    }
}
