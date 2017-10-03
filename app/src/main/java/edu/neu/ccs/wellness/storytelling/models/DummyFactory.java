package edu.neu.ccs.wellness.storytelling.models;

import edu.neu.ccs.wellness.storytelling.interfaces.StoryContent;

/**
 * Created by hermansaksono on 6/30/17.
 */

public class DummyFactory {
    public static final String IMG_URL = "http://wellness.ccs.neu.edu/story_static/temp/story_01__page__01__ver_01__item_01.png";
    public static final String TITLE = "Book's title";
    public static final String TEXT = "Lorem ipsum is the text";
    public static final String SUBTEXT = "Dolor sit amet is the subtext";

    public static StoryContent createDummy(StoryContent.ContentType type) {
        StoryContent storyContent = null;
        if (type.equals(StoryContent.ContentType.COVER)) {
            storyContent = new StoryCover(1, null, IMG_URL, TITLE, null, true);
        }
        else if (type.equals(StoryContent.ContentType.PAGE)) {
            storyContent = new StoryPage(1, null, IMG_URL, TEXT, SUBTEXT, true);
        }
        else if (type.equals(StoryContent.ContentType.REFLECTION)) {

            storyContent = new StoryReflection(1, null, null, TEXT, SUBTEXT, true);
        }
        return storyContent;
    }
}
