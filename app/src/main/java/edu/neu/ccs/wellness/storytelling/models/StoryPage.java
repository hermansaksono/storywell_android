package edu.neu.ccs.wellness.storytelling.models;

import android.content.Context;

import edu.neu.ccs.wellness.storytelling.interfaces.RestServer;
import edu.neu.ccs.wellness.storytelling.interfaces.StoryContent;
import edu.neu.ccs.wellness.storytelling.interfaces.StoryInterface;
import edu.neu.ccs.wellness.storytelling.interfaces.StorytellingException;

/**
 * Created by hermansaksono on 6/13/17.
 */

public class StoryPage implements StoryContent {
    public static final String FILENAME_IMAGE = "story__id_%d__page_%d__image_0.png";

    private int id;
    private StoryInterface story;
    private String imgUrl;
    private String text;
    private String subtext;
    private boolean isCurrent;

    private static final String EXC_CONTENT_UNINITIALIZED = "Content has not been initialized";

    // PRIVATE CONSTRUCTORS
    public StoryPage(int pageId, StoryInterface story,
                     String imgUrl, String text, String subText, boolean isCurrentPage) {
        this.id = pageId;
        this.story = story;
        this.imgUrl = imgUrl;
        this.text = text;
        this.subtext = subText;
        this.isCurrent = isCurrentPage;
    }

    // PUBLIC METHODS
    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public void downloadFiles(Context context, RestServer server)
            throws StorytellingException {
        if (this.imgUrl != null) {
            server.downloadToStorage(context, this.getImageFilename(), this.imgUrl);
        }
        else {
            throw new StorytellingException(EXC_CONTENT_UNINITIALIZED);
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

    // HELPER METHODS
    public String getImageFilename() {
        return String.format(this.FILENAME_IMAGE, this.story.getId(), this.id);
    }
}
