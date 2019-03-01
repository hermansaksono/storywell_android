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
public class CalmingReflection implements StoryContent {

    public static final String KEY_SHOW_REF_START = "isShowReflectionStart";
    public static final boolean DEFAULT_IS_REF_START = false;

    private int id;
    private ContentType type = ContentType.REFLECTION;
    private String text;
    private String subtext;

    @PropertyName(value="content_group_id")
    private String contentGroupId;

    @PropertyName(value="content_group_name")
    private String contentGroupName = StoryPage.DEFAULT_CONTENT_GROUP_NAME;

    @PropertyName(value="content_group_name")
    private int nextContentId;

    @PropertyName(value="is_show_reflection_start")
    private boolean isShowReflectionStart = DEFAULT_IS_REF_START;

    // CONSTRUCTORS

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

    @Override
    public void downloadFiles(Context context, RestServer server)
            throws StorytellingException {
        // Not implemented
    }

    @Override
    public ContentType getType() {
        return this.type;
    }

    @Override @Exclude
    public String getImageURL() { return null; }

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

    @Override  @Exclude
    public void respond() { }

    @Override  @Exclude
    public boolean isLocked() {
        return false;
    }

    public boolean isShowReflectionStart() { return this.isShowReflectionStart; }
}
