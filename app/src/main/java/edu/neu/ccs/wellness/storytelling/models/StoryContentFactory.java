package edu.neu.ccs.wellness.storytelling.models;

import org.json.JSONException;
import org.json.JSONObject;

import edu.neu.ccs.wellness.storytelling.interfaces.StoryContentInterface;
import edu.neu.ccs.wellness.storytelling.interfaces.StoryContentInterface.ContentType;
import edu.neu.ccs.wellness.storytelling.interfaces.StoryInterface;

/**
 * Created by hermansaksono on 6/15/17.
 */

public class StoryContentFactory {

    public static StoryContentInterface create (StoryInterface story, JSONObject jsonContent)
            throws JSONException {
        StoryContentInterface storyContent = null;
        int id = jsonContent.getInt("id");
        String type = jsonContent.getString("type");
        String imgUrl = jsonContent.getString("img_url");
        String text = jsonContent.getString("text");
        String subText = jsonContent.getString("img_url");
        boolean isCurrentPage = jsonContent.getBoolean("is_current_page");
        if (getStoryContentType(type) == ContentType.STORY) {
            storyContent = new StoryPage(id, story, imgUrl, text, subText, isCurrentPage);
        }
        else if (getStoryContentType(type) == ContentType.REFLECTION) {
            storyContent = new StoryReflection(id, story, imgUrl, text, subText, isCurrentPage);
        }
        return storyContent;
    }

    private static ContentType getStoryContentType(String type) {
        if (type.equals("story")) { return ContentType.STORY; }
        else if (type.equals("reflection")) { return ContentType.REFLECTION; }
        else { return ContentType.OTHER; }
    }
}
