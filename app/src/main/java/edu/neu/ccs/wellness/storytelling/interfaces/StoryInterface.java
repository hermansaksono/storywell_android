package edu.neu.ccs.wellness.storytelling.interfaces;

import android.content.Context;

import java.util.List;

/**
 * Created by hermansaksono on 6/13/17.
 */

public interface StoryInterface {

    public int getId();

    public void loadStoryDef(Context context, RestServer server);

    public void loadStoryContents(Context context, RestServer server)
            throws StorytellingException;

    public String getTitle();

    public boolean isContentSet();

    public List<StoryContent> getContents();

    public int getCurrentPageId();

    public boolean isCurrent();

    public void setIsCurrent(boolean isCurrent);

    public void goToNextPage();

    public void goToPrevPage();

    public void goToPageById(int pageIndex);

    public String getRefreshDateTime();

    public String getCoverUrl();

}
