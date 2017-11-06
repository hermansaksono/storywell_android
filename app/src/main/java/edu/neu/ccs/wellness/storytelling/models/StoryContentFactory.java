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
        String subText = jsonContent.getString("subtext");
        boolean isCurrentPage = false;

        if (getStoryContentType(type) == ContentType.COVER) {
            storyContent = new StoryCover(id, story, imgUrl, text, subText, isCurrentPage);
        }
        else if (getStoryContentType(type) == ContentType.PAGE) {
            storyContent = new StoryPage(id, story, imgUrl, text, subText, isCurrentPage);
        }
        else if (getStoryContentType(type) == ContentType.REFLECTION_START) {
            storyContent = new StoryReflectionStart(id, story, imgUrl, text, subText, isCurrentPage);
        }
        else if (getStoryContentType(type) == ContentType.REFLECTION) {
            storyContent = new StoryReflection(id, story, imgUrl, text, subText, isCurrentPage);
        }
        else if (getStoryContentType(type) == ContentType.STATEMENT) {
            storyContent = new StoryStatement(id, story, imgUrl, text, subText, isCurrentPage);
        }
        else if (getStoryContentType(type) == ContentType.CHALLENGE_INFO) {
            storyContent = new StoryChallengeInfo(id, story, imgUrl, text, subText, isCurrentPage);
        }
        else if (getStoryContentType(type) == ContentType.CHALLENGE) {
            storyContent = new StoryChallenge(id, story, imgUrl, text, subText, isCurrentPage);
        }
        else if (getStoryContentType(type) == ContentType.CHALLENGE_SUMMARY) {
            storyContent = new StoryChallengeSummary(id, story, imgUrl, text, subText, isCurrentPage);
        }
        //Log.d("WELL", subText);
        return storyContent;
    }

    private static ContentType getStoryContentType(String type) {
        if (type.equals("COVER")) { return ContentType.COVER; }
        else if (type.equals("PAGE")) { return ContentType.PAGE; }
        else if (type.equals("REFLECTION_START")) { return ContentType.REFLECTION_START; }
        else if (type.equals("REFLECTION")) { return ContentType.REFLECTION; }
        else if (type.equals("STATEMENT")) { return ContentType.STATEMENT; }
        else if (type.equals("CHALLENGE_INFO")) { return ContentType.CHALLENGE_INFO; }
        else if (type.equals("CHALLENGE")) { return ContentType.CHALLENGE; }
        else if (type.equals("CHALLENGE_SUMMARY")) { return ContentType.CHALLENGE_SUMMARY; }
        else { return ContentType.GENERIC; }
    }
}
