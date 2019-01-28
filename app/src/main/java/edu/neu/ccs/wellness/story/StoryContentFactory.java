package edu.neu.ccs.wellness.story;

import org.json.JSONException;
import org.json.JSONObject;

import edu.neu.ccs.wellness.story.interfaces.StoryContent;
import edu.neu.ccs.wellness.story.interfaces.StoryContent.ContentType;
import edu.neu.ccs.wellness.story.interfaces.StoryInterface;

/**
 * Created by hermansaksono on 6/15/17.
 */

public class StoryContentFactory {

    public static StoryContent create (StoryInterface story, JSONObject jsonContent)
            throws JSONException {
        int id = jsonContent.getInt("id");
        String type = jsonContent.getString("type");
        String imgUrl = jsonContent.getString("img_url");
        String text = jsonContent.getString("text");
        String subText = jsonContent.getString("subtext");
        boolean isCurrentPage = false;

        switch (ContentType.fromString(type)) {
            case COVER:
                return new StoryCover(id, story, imgUrl, text, subText, false);
            case PAGE:
                return new StoryPage(id, story, imgUrl, text, subText, false, false);
            case REFLECTION:
                String contentGroupId = jsonContent.optString(StoryPage.KEY_CONTENT_GROUP,
                        StoryPage.DEFAULT_CONTENT_GROUP);
                String contentGroupName = jsonContent.optString(StoryPage.KEY_CONTENT_GROUP_NAME,
                        StoryPage.DEFAULT_CONTENT_GROUP_NAME);
                int nextContentId = jsonContent.optInt(StoryPage.KEY_NEXT_ID,
                        StoryPage.DEFAULT_NEXT_ID);
                return new StoryReflection(id, story, imgUrl, text, subText,
                        getIsShowReflStart(jsonContent), contentGroupId, contentGroupName,
                        nextContentId, false);
            case STATEMENT:
                return new StoryStatement(id, story, imgUrl, text, subText, false);
            case CHALLENGE:
                return new StoryChallenge(id, story, imgUrl, text, subText, isCurrentPage);
            case MEMO:
                // TODO do something
            default:
                return new StoryPage(
                        id, story, imgUrl, text, subText, false, false);
        }
    }
    /*
    private static ContentType getStoryContentType(String type) {
        if (type == null) {
            return ContentType.GENERIC;
        }
        switch (type) {
            case "COVER":
                return ContentType.COVER;
            case "PAGE":
                return ContentType.PAGE;
            case "REFLECTION":
                return ContentType.REFLECTION;
            case "STATEMENT":
                return ContentType.STATEMENT;
            case "CHALLENGE":
                return ContentType.CHALLENGE;
            case "MEMO":
                return ContentType.MEMO;
            default:
                return ContentType.PAGE;
        }
    }
    */
    private static boolean getIsShowReflStart(JSONObject jsonObj) {
        return jsonObj.optBoolean(StoryReflection.KEY_SHOW_REF_START, StoryReflection.DEFAULT_IS_REF_START);
    }
}
