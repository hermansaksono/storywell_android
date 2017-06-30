package edu.neu.ccs.wellness.storytelling.models;

import org.json.JSONException;
import org.json.JSONObject;

import edu.neu.ccs.wellness.storytelling.interfaces.StoryContent;
import edu.neu.ccs.wellness.storytelling.interfaces.StoryContent.ContentType;
import edu.neu.ccs.wellness.storytelling.interfaces.StoryInterface;

/**
 * Created by hermansaksono on 6/15/17.
 */

public class StoryContentFactory {
    public static final String IMG_URL = "http://wellness.ccs.neu.edu/story_static/temp/story_01__page__01__ver_01__item_01.png";
    public static final String TITLE = "Book's title";
    public static final String TEXT = "Lorem ipsum is the text";
    public static final String SUBTEXT = "Dolor sit amet is the subtext";

    public static StoryContent create (StoryInterface story, JSONObject jsonContent)
            throws JSONException {
        StoryContent storyContent = null;
        int id = jsonContent.getInt("id");
        String type = jsonContent.getString("type");
        String imgUrl = jsonContent.getString("img_url");
        String text = jsonContent.getString("text");
        String subText = jsonContent.getString("img_url");
        boolean isCurrentPage = jsonContent.getBoolean("is_current_page");
        if (getStoryContentType(type) == ContentType.PAGE) {
            storyContent = new StoryPage(id, story, imgUrl, text, subText, isCurrentPage);
        }
        else if (getStoryContentType(type) == ContentType.REFLECTION) {
            storyContent = new StoryReflection(id, story, imgUrl, text, subText, isCurrentPage);
        }
        return storyContent;
    }

    public static StoryContent createDummy(ContentType type) {
        StoryContent storyContent = null;
        if (type.equals(ContentType.COVER)) {
            storyContent = new StoryPage(1, null, IMG_URL, TITLE, null, true);
        }
        else if (type.equals(ContentType.PAGE)) {
            storyContent = new StoryPage(1, null, IMG_URL, TEXT, SUBTEXT, true);
        }
        else if (type.equals(ContentType.REFLECTION)) {

            storyContent = new StoryReflection(1, null, null, TEXT, SUBTEXT, true);
        }
        return storyContent;
    }

    private static ContentType getStoryContentType(String type) {
        if (type.equals("story")) { return ContentType.PAGE; }
        else if (type.equals("reflection")) { return ContentType.REFLECTION; }
        else { return ContentType.OTHER; }
    }
}
