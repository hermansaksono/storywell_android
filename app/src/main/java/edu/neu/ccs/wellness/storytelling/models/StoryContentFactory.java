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

    private static ContentType getStoryContentType(String type) {
        if (type.equals("story")) { return ContentType.PAGE; }
        else if (type.equals("reflection")) { return ContentType.REFLECTION; }
        else { return ContentType.OTHER; }
    }
}
