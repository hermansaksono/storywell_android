package edu.neu.ccs.wellness.storytelling.interfaces;

import android.content.Context;

import java.util.List;

import edu.neu.ccs.wellness.server.RestServer;

/**
 * Created by hermansaksono on 6/13/17.
 */

public interface StoryInterface {

    public int getId();

    public void loadStoryDef(Context context, RestServer server);

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

    public String getDefUrl();

    public String getDefFilename();

}
