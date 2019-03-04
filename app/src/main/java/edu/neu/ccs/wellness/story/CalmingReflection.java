package edu.neu.ccs.wellness.story;

import android.content.Context;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;

import edu.neu.ccs.wellness.server.RestServer;
import edu.neu.ccs.wellness.story.interfaces.StoryContent;
import edu.neu.ccs.wellness.story.interfaces.StoryInterface;
import edu.neu.ccs.wellness.story.interfaces.StorytellingException;

/**
 * Created by hermansaksono on 6/14/17.
 */

@IgnoreExtraProperties
public class CalmingReflection extends StoryReflection { //implements StoryContent {

    public static final String KEY_ID = "id";
    public static final String KEY_TEXT = "text";
    public static final String KEY_SUBTEXT = "subtext";
    public static final String KEY_TYPE = "type";
    public static final boolean DEFAULT_IS_REF_START = false;

    private int id = 0;
    private String text = "";
    private String subtext = "";
    private String type = "REFLECTION";
    private String img_url = "";
    private String contentGroupId;
    private String contentGroupName = StoryPage.DEFAULT_CONTENT_GROUP_NAME;
    private int nextContentId;
    private boolean isShowReflectionStart = DEFAULT_IS_REF_START;

    public CalmingReflection(int pageId, StoryInterface story, String imgUrl,
                             String text, String subText, boolean isShowReflectionStart,
                             String contentGroupId, String contentGroupName, int nextContentId,
                             boolean isCurrentPage) {
        super(pageId, story, imgUrl, text, subText, isShowReflectionStart,
                contentGroupId, contentGroupName, nextContentId, isCurrentPage);
    }

    // CONSTRUCTORS
    /*
    public CalmingReflection(int pageId, String text, String subtext, boolean isShowReflectionStart,
                             String contentGroupId, String contentGroupName, int nextContentId) {
        this.id = pageId;
        this.text = text;
        this.subtext = subtext;
        this.contentGroupId = contentGroupId;
        this.contentGroupName = contentGroupName;
        this.nextContentId = nextContentId;
        this.isShowReflectionStart = isShowReflectionStart;
    }

    public CalmingReflection() {

    }

    // PUBLIC METHODS

    @Override
    public int getId() {
        return this.id;
    }

    public String getGroupId() {
        return this.contentGroupId;
    }

    public String getGroupName() {
        return this.contentGroupName;
    }

    @Exclude
    public boolean isGroupNameExists() {
        return !this.contentGroupName.equals(StoryPage.DEFAULT_CONTENT_GROUP_NAME);
    }

    public int getNextId() {
        return this.nextContentId;
    }

    @Exclude
    public boolean isNextExists() { return this.nextContentId >= 0; }

    @Override @Exclude
    public void downloadFiles(Context context, RestServer server)
            throws StorytellingException {
        // Not implemented
    }

    @Override
    public ContentType getType() {
        return ContentType.fromString(this.type);
    }

    @Override @Exclude
    public String getImageURL() { return this.img_url; }

    @Override
    public String getText() {
        return this.text;
    }

    @Override
    public String getSubtext() {
        return this.subtext;
    }

    @Override @Exclude
    public boolean isCurrent() { return true; }

    @Override @Exclude
    public void setIsCurrent(boolean isCurrent) {
        // DO nothing
    }

    @Override @Exclude
    public void respond() { }

    @Override @Exclude
    public boolean isLocked() {
        return false;
    }

    public boolean isShowReflectionStart() { return this.isShowReflectionStart; }
    */
}
