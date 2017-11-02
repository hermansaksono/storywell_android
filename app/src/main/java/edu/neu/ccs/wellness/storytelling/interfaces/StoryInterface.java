package edu.neu.ccs.wellness.storytelling.interfaces;

import android.content.Context;

import java.util.List;

import edu.neu.ccs.wellness.server.RestServer;

public interface StoryInterface {

    int getId();

    void loadStoryDef(Context context, RestServer server);

    String getTitle();

    boolean isContentSet();

    List<StoryContent> getContents();

    StoryContent getContentByIndex(int index);

    int getCurrentPageId();

    boolean isCurrent();

    void setIsCurrent(boolean isCurrent);

    void goToNextPage();

    void goToPrevPage();

    void goToPageById(int pageIndex);

    String getRefreshDateTime();

    String getCoverUrl();

    String getDefUrl();

    String getDefFilename();

    StoryStateInterface getState();

}
