package edu.neu.ccs.wellness.story.interfaces;

import android.content.Context;

import edu.neu.ccs.wellness.server.RestServer;

/**
 * Created by hermansaksono on 6/13/17.

ContentType is an enum, that is either:
 -- COVER              : Show cover page, contains image and book title
 -- PAGE               : Show each storybook's page, contains page image and 
                         the narration text
 -- REFLECTION_START   : Shows the reflection start screen (By default use 
                         string/reflection_start_title and 
                         reflection_start_subtitle)
 -- REFLECTION         : Show the reflection screen. Contains text and subtext
 -- STATEMENT          : Show the closing statement after a reflection(s). 
                         Contains text and subtext.
 -- CHALLENGE_INFO     : Show the challenge start screen (By default use
                         string/challenge_info_title and 
                         string/challenge_info_subtitle)
  -- CHALLENGE         : Show the challenge selection page. Details TBD (By default use
                         string/challenge_title and 
                         string/challenge_subtitle)
  -- CHALLENGE_SUMMARY : Show the summary of the challenge that has been 
                         picked. Details TBD (By default use 
						 string/challenge_summary_info and 
                         string/challenge_summary_subtitle)
  -- GENERIC           : Just show text and subtext
  -- OTHER             : Do not show anything
 */


public interface StoryContent {

    enum ContentType {
        COVER, PAGE, REFLECTION_START, REFLECTION, STATEMENT, 
		CHALLENGE_INFO, CHALLENGE, CHALLENGE_SUMMARY, MEMO, ACTION_INCREMENT,
		GENERIC, OTHER;

        public static ContentType fromString(String type) {
            if (type == null) {
                return GENERIC;
            }
            switch (type) {
                case "COVER":
                    return COVER;
                case "PAGE":
                    return PAGE;
                case "REFLECTION":
                    return REFLECTION;
                case "STATEMENT":
                    return STATEMENT;
                case "CHALLENGE":
                    return CHALLENGE;
                case "MEMO":
                    return MEMO;
                case "ACTION_INCREMENT":
                    return ACTION_INCREMENT;
                default:
                    return PAGE;
            }
        }
    }

    int getId();

    void downloadFiles(Context context, RestServer server)
            throws StorytellingException;

    ContentType getType();

    String getImageURL();

    String getText();

    String getSubtext();

    boolean isCurrent();

    void setIsCurrent(boolean isCurrent);

    void respond();

    boolean isLocked();
}
