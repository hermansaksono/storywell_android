package edu.neu.ccs.wellness.story;

import android.content.Context;

import edu.neu.ccs.wellness.server.RestServer;
import edu.neu.ccs.wellness.story.interfaces.StoryContent;
import edu.neu.ccs.wellness.story.interfaces.StoryInterface;
import edu.neu.ccs.wellness.story.interfaces.StorytellingException;

/**
 * Created by hermansaksono on 1/28/19.
 */

public class StoryMemo implements StoryContent {
    public static final String KEY_PAGE_ID_TO_UNLOCK = "storyPageIdToUnlock";
    public static final String KEY_STORY_ID_TO_UNLOCK = "KEY_STORY_ID_TO_UNLOCK";
    public static final String DEFAULT_PAGE_ID_TO_UNLOCK = "";
    public static final String FORMAT_PAGE_ID_TO_UNLOCK = "s%s_c0";

    private String storyIdToUnlock;
    private String pageIdToUnlock;
    private StoryPage page;

    // CONSTRUCTORS
    public StoryMemo(int pageId, StoryInterface story,
                      String imgUrl, String text, String subText,
                      boolean isCurrentPage, String storyIdToUnlock, String pageIdToUnlock) {
        this.page = new StoryPage(
                pageId, story, imgUrl, text, subText, isCurrentPage, false);
        this.storyIdToUnlock = storyIdToUnlock;
        this.pageIdToUnlock = pageIdToUnlock;
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
        return ContentType.MEMO;
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

    public String getPageIdToUnlock() {
        return this.pageIdToUnlock;
    }

    public String getStoryIdToUnlock() {
        return this.storyIdToUnlock;
    }
}
