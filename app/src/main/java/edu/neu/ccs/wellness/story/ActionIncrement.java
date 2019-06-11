package edu.neu.ccs.wellness.story;

import android.content.Context;

import edu.neu.ccs.wellness.server.RestServer;
import edu.neu.ccs.wellness.story.interfaces.StoryContent;
import edu.neu.ccs.wellness.story.interfaces.StoryInterface;
import edu.neu.ccs.wellness.story.interfaces.StorytellingException;

/**
 * Created by hermansaksono on 1/28/19.
 */

public class ActionIncrement implements StoryContent {
    private StoryPage page;

    // CONSTRUCTORS
    public ActionIncrement(int pageId, StoryInterface story,
                           String imgUrl, String text, String subText, boolean isCurrentPage) {
        this.page = new StoryPage(
                pageId, story, imgUrl, text, subText, isCurrentPage, false);
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
        return ContentType.ACTION_INCREMENT;
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
}
