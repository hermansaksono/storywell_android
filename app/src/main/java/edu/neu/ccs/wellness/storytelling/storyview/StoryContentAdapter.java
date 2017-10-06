package edu.neu.ccs.wellness.storytelling.storyview;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import edu.neu.ccs.wellness.storytelling.interfaces.StoryContent;
import edu.neu.ccs.wellness.storytelling.interfaces.StoryContent.ContentType;


public class StoryContentAdapter {

    public static final String KEY_IMG_URL = "KEY_IMG_URL";
    public static final String KEY_TEXT = "KEY_TEXT";
    public static final String KEY_SUBTEXT = "KEY_SUBTEXT";

    public static Fragment getFragment(StoryContent storyContent) {
        Fragment storyContentFragment = null;

        switch (storyContent.getType()) {
            case PAGE:
                storyContentFragment = createPage(storyContent);
                break;

            case COVER:
                storyContentFragment = createCover(storyContent);
//                StoryCoverFragment.newInstance(getBundle(storyContent));
//                break;

            case REFLECTION_START:
                storyContentFragment = createReflectionStart(storyContent);
                break;

            case REFLECTION:
                storyContentFragment = createReflection(storyContent);
                break;

            case STATEMENT:
                storyContentFragment = createStatement(storyContent);
                break;
            case CHALLENGE_INFO:
                storyContentFragment = createChallengeInfo(storyContent);
                break;
            case CHALLENGE:
                storyContentFragment = createChallenge(storyContent);
                break;
            case CHALLENGE_SUMMARY:
                storyContentFragment = createChallengeSummary(storyContent);
                break;
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

    private static Fragment createReflection(StoryContent content) {
        Fragment fragment = new ReflectionFragment();
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

    private static Fragment createChallenge(StoryContent content) {
        Fragment fragment = new ChallengePickerFragment();
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
        args.putString(KEY_IMG_URL, content.getImageURL());
        args.putString(KEY_TEXT, content.getText());
        args.putString(KEY_SUBTEXT, content.getSubtext());
        return args;
    }
}
