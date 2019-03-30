package edu.neu.ccs.wellness.story;

import android.content.Context;

import java.util.Locale;

import edu.neu.ccs.wellness.server.RestServer;
import edu.neu.ccs.wellness.story.interfaces.StoryContent;
import edu.neu.ccs.wellness.story.interfaces.StoryInterface;
import edu.neu.ccs.wellness.story.interfaces.StorytellingException;

/**
 * Created by hermansaksono on 6/14/17.
 */

public class StoryCover implements StoryContent {
    private StoryPage page;
    private String storyId;

    // CONSTRUCTORS

    public StoryCover(int pageId, StoryInterface story,
                      String imgUrl, String text, String subText,
                      boolean isCurrentPage, boolean isLocked) {
        this.storyId = story.getId();
        this.page = new StoryPage(pageId, story, imgUrl, text, subText, isCurrentPage, isLocked);
    }

    // PUBLIC METHODS

    @Override
    public int getId() {
        return this.page.getId();
    }

    @Override
    public void downloadFiles(Context context, RestServer server)
            throws StorytellingException {
        this.page.downloadFiles(context, server);
    }

    @Override
    public ContentType getType() {
        return ContentType.COVER;
    }

    @Override
    public String getImageURL() { return this.page.getImageURL(); }

    @Override
    public String getText() {
        return this.page.getText();
    }

    @Override
    public String getSubtext() {
        return this.page.getSubtext();
    }

    @Override
    public boolean isCurrent() {
        return this.page.isCurrent();
    }

    @Override
    public void setIsCurrent(boolean isCurrent) {
        this.page.setIsCurrent(isCurrent);
    }

    @Override
    public void respond() {

    }

    @Override
    public boolean isLocked() {
        return this.page.isLocked();
    }

    public String getStoryPageId() {
        return String.format(Locale.US, StoryPage.FORMAT_STORY_PAGE_ID, this.storyId, this.getId());
    }

    public String getStoryId() {
        return this.storyId;
    }
}
