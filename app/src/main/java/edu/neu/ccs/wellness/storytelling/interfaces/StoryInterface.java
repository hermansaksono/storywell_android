package edu.neu.ccs.wellness.storytelling.interfaces;

import android.content.Context;

import java.util.List;

import edu.neu.ccs.wellness.storytelling.models.StoryPage;

/**
 * Created by hermansaksono on 6/13/17.
 */

public interface StoryInterface {

    public int getId();

    public void loadStoryDef(Context context, RestServerInterface server);

    public void loadStoryContents(Context context, RestServerInterface server)
            throws StorytellingException;

    public boolean isContentSet();

    public List<StoryContentInterface> getContents();

    public int getCurrentPageId();

    public boolean isCurrent();

    public void setIsCurrent(boolean isCurrent);

    public void goToNextPage();

    public void goToPrevPage();

    public void goToPageById(int pageIndex);

    public String getRefreshDateTime();

}
