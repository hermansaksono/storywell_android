package edu.neu.ccs.wellness.storytelling.interfaces;

import android.content.Context;

/**
 * Created by hermansaksono on 6/13/17.
 */

public interface StoryContentInterface {

    public enum ContentType {
        STORY, REFLECTION, OTHER;
    }

    public int getId();

    public void downloadFiles(Context context, RestServerInterface server)
            throws StorytellingException;

    public ContentType getType();

    public void getImage();

    public String getText();

    public String getSubtext();

    public boolean isCurrent();

    public void setIsCurrent(boolean isCurrent);

    public void respond();
}
