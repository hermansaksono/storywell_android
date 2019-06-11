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

        switch (ContentType.fromString(type)) {
            case COVER:
                boolean isLocked = jsonContent.optBoolean(
                        StoryPage.KEY_IS_LOCKED, StoryPage.DEFAULT_IS_LOCKED);
                return new StoryCover(
                        id, story, imgUrl, text, subText, false, isLocked);
            case PAGE:
                return new StoryPage(
                        id, story, imgUrl, text, subText, false, false);
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
                return new StoryChallenge(id, story, imgUrl, text, subText, false);
            case MEMO:
                String storyPageId = getNextStoryPageId(story.getNextStoryId());
                return new StoryMemo(
                        id, story, imgUrl, text, subText, false,
                        story.getNextStoryId(), storyPageId);
            case ACTION_INCREMENT:
                return new ActionIncrement(id, story, imgUrl, text, subText, false);
            default:
                return new StoryPage(
                        id, story, imgUrl, text, subText, false, false);
        }
    }

    private static boolean getIsShowReflStart(JSONObject jsonObj) {
        return jsonObj.optBoolean(
                StoryReflection.KEY_SHOW_REF_START, StoryReflection.DEFAULT_IS_REF_START);
    }

    private static String getNextStoryPageId(String storyId) {
        if (storyId == null) {
            return StoryMemo.DEFAULT_PAGE_ID_TO_UNLOCK;
        } else {
            return String.format(StoryMemo.FORMAT_PAGE_ID_TO_UNLOCK, storyId);
        }
    }
}
