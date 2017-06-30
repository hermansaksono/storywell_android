package edu.neu.ccs.wellness.storytelling.interfaces;

import android.content.Context;

/**
 * Created by hermansaksono on 6/13/17.
 */

public interface StoryContent {

    public enum ContentType {
        COVER, PAGE, REFLECTION, STATEMENT, CHALLENGE, OTHER;
    }

    public int getId();

    public void downloadFiles(Context context, RestServer server)
            throws StorytellingException;

    public ContentType getType();

    public void getImage();

    public String getText();

    public String getSubtext();

    public boolean isCurrent();

    public void setIsCurrent(boolean isCurrent);

    public void respond();
}
