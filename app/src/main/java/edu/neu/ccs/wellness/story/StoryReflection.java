package edu.neu.ccs.wellness.story;

import android.content.Context;

import edu.neu.ccs.wellness.server.RestServer;
import edu.neu.ccs.wellness.story.interfaces.StoryContent;
import edu.neu.ccs.wellness.story.interfaces.StoryInterface;
import edu.neu.ccs.wellness.story.interfaces.StorytellingException;

/**
 * Created by hermansaksono on 6/14/17.
 */

public class StoryReflection implements StoryContent {

    public static final String KEY_SHOW_REF_START = "isShowReflectionStart";
    public static final boolean DEFAULT_IS_REF_START = false;

    private StoryPage page;
    private String contentGroupId;
    private String contentGroupName;
    private int nextContentId;
    private boolean isShowReflectionStart = DEFAULT_IS_REF_START;

    // CONSTRUCTORS

    public StoryReflection(int pageId, StoryInterface story, String imgUrl,
                           String text, String subText, boolean isShowReflectionStart,
                           String contentGroupId, String contentGroupName, int nextContentId,
                           boolean isCurrentPage) {
        this.page = new StoryPage(pageId, story, imgUrl, text, subText, isCurrentPage);
        this.contentGroupId = contentGroupId;
        this.contentGroupName = contentGroupName;
        this.nextContentId = nextContentId;
        this.isShowReflectionStart = isShowReflectionStart;
    }

    // PUBLIC METHODS

    @Override
    public int getId() {
        return this.page.getId();
    }

    public String getGroupId() {
        return this.contentGroupId;
    }

    public String getGroupName() {
        return this.contentGroupName;
    }

    public boolean isGroupNameExists() { return
            !this.contentGroupName.equals(StoryPage.DEFAULT_CONTENT_GROUP_NAME); }

    public int getNextId() {
        return this.nextContentId;
    }

    public boolean isNextExists() { return this.nextContentId >= 0; }

    @Override
    public void downloadFiles(Context context, RestServer server)
            throws StorytellingException {
        this.page.downloadFiles(context, server);
    }

    @Override
    public ContentType getType() {
        return ContentType.REFLECTION;
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
    public void respond() { }

    public boolean isShowReflectionStart() { return this.isShowReflectionStart; }
}
