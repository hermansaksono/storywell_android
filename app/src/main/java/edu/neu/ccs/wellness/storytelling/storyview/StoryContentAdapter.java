package edu.neu.ccs.wellness.storytelling.storyview;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import edu.neu.ccs.wellness.storytelling.interfaces.StoryContent;
import edu.neu.ccs.wellness.storytelling.interfaces.StoryContent.ContentType;
import edu.neu.ccs.wellness.storytelling.models.StoryPage;

/**
 * Created by hermansaksono on 6/30/17.
 */

public class StoryContentAdapter {

    public static final String KEY_IMG_URL = "KEY_IMG_URL";
    public static final String KEY_TEXT = "KEY_TEXT";
    public static final String KEY_SUBTEXT = "KEY_SUBTEXT";

    public static Fragment getFragment(StoryContent storyContent) {
        Fragment storyContentFragment = null;

        if (storyContent.getType().equals(ContentType.COVER)) {
            storyContentFragment = createCover(storyContent);
        }
        else if (storyContent.getType().equals(ContentType.PAGE)) {
            storyContentFragment = createPage(storyContent);
        }
        else if (storyContent.getType().equals(ContentType.REFLECTION)) {
            storyContentFragment = createReflection(storyContent);
        }

        return storyContentFragment;
    }

    private static Fragment createCover(StoryContent page) {
        Fragment fragment = new StoryCoverFragment();

        Bundle args = new Bundle();
        args.putString(KEY_IMG_URL, page.getImageURL());
        args.putString(KEY_TEXT, page.getText());
        args.putString(KEY_SUBTEXT, page.getSubtext());
        fragment.setArguments(args);

        return fragment;
    }

    private static Fragment createPage(StoryContent page) {
        Fragment fragment = new StoryPageFragment();

        Bundle args = new Bundle();
        args.putString(KEY_IMG_URL, page.getImageURL());
        args.putString(KEY_TEXT, page.getText());
        args.putString(KEY_SUBTEXT, page.getSubtext());
        fragment.setArguments(args);

        return fragment;
    }

    private static Fragment createReflection(StoryContent page) {
        Fragment fragment = new ReflectionFragment();

        Bundle args = new Bundle();
        args.putString(KEY_IMG_URL, page.getImageURL());
        args.putString(KEY_TEXT, page.getText());
        args.putString(KEY_SUBTEXT, page.getSubtext());
        fragment.setArguments(args);

        return fragment;
    }
}
