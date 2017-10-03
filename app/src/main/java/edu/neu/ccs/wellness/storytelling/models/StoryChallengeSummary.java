package edu.neu.ccs.wellness.storytelling.models;

import android.content.Context;

import edu.neu.ccs.wellness.storytelling.interfaces.RestServer;
import edu.neu.ccs.wellness.storytelling.interfaces.StoryContent;
import edu.neu.ccs.wellness.storytelling.interfaces.StoryInterface;
import edu.neu.ccs.wellness.storytelling.interfaces.StorytellingException;

/**
 * Created by hermansaksono on 6/14/17.
 */

public class StoryChallengeSummary implements StoryContent {
    private StoryPage page;

    // CONSTRUCTORS

    public StoryChallengeSummary (int pageId, StoryInterface story,
                           String imgUrl, String text, String subText, boolean isCurrentPage) {
        this.page = new StoryPage(pageId, story, imgUrl, text, subText, isCurrentPage);
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
        return ContentType.CHALLENGE_SUMMARY;
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
}
