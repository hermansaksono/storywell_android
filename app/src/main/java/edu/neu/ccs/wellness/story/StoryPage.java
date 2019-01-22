package edu.neu.ccs.wellness.story;

import android.content.Context;

import java.io.IOException;
import java.net.URL;

import edu.neu.ccs.wellness.server.RestServer;
import edu.neu.ccs.wellness.story.interfaces.StoryContent;
import edu.neu.ccs.wellness.story.interfaces.StoryInterface;
import edu.neu.ccs.wellness.story.interfaces.StorytellingException;

/**
 * Created by hermansaksono on 6/13/17.
 */

public class StoryPage implements StoryContent {
    public static final String FILENAME_IMAGE = "story__id_%d__page_%d__image_0.png";
    public static final String KEY_CONTENT_GROUP = "contentGroupId";
    public static final String KEY_CONTENT_GROUP_NAME = "contentGroupName";
    public static final String KEY_NEXT_ID = "nextContentId";
    public static final String KEY_IS_LOCKED = "nextContentId";
    public static final String DEFAULT_CONTENT_GROUP = "default";
    public static final String DEFAULT_CONTENT_GROUP_NAME = "";
    public static final int DEFAULT_NEXT_ID = -1;

    private int id;
    private StoryInterface story;
    private String imgUrl;
    private String text;
    private String subtext;
    private boolean isCurrent;
    private boolean isLocked;

    private static final String EXC_CONTENT_UNINITIALIZED = "Content has not been initialized";

    // PRIVATE CONSTRUCTORS
    public StoryPage(int pageId, StoryInterface story,
                     String imgUrl, String text, String subText,
                     boolean isCurrentPage, boolean isLocked) {
        this.id = pageId;
        this.story = story;
        this.imgUrl = imgUrl;
        this.text = text;
        this.subtext = subText;
        this.isCurrent = isCurrentPage;
        this.isLocked = isLocked;
    }

    // PUBLIC METHODS
    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public void downloadFiles(Context context, RestServer server)
            throws StorytellingException {
        try {
            if (this.imgUrl != null) {
                URL url = new URL(this.imgUrl);
                server.doGetRequestThenSave(context, this.getImageFilename(), url);
            }
            else {
                throw new StorytellingException(EXC_CONTENT_UNINITIALIZED);
            }
        } catch (IOException e) {

        }
    }

    @Override
    public ContentType getType() {
        return ContentType.PAGE;
    }

    @Override
    public String getImageURL() { return this.imgUrl; }

    @Override
    public String getText() { return this.text; }

    @Override
    public String getSubtext() { return this.subtext; }

    @Override
    public boolean isCurrent() {
        return this.isCurrent;
    }

    @Override
    public void setIsCurrent(boolean isCurrent) {
        this.isCurrent = isCurrent;
    }

    @Override
    public void respond() {

    }

    @Override
    public boolean isLocked() {
        return this.isLocked;
    }

    // HELPER METHODS
    public String getImageFilename() {
        return String.format(FILENAME_IMAGE, this.story.getId(), this.id);
    }
}
