package edu.neu.ccs.wellness.utils;

import android.content.Context;

import java.util.List;

import edu.neu.ccs.wellness.server.RestServer;
import edu.neu.ccs.wellness.storytelling.interfaces.StoryContent;
import edu.neu.ccs.wellness.storytelling.interfaces.StoryInterface;
import edu.neu.ccs.wellness.storytelling.interfaces.StoryType;

/**
 * Created by hermansaksono on 11/6/17.
 */

public class StorySetting implements StoryInterface {

    private int id;

    public StorySetting (int storyId) { this.id = storyId; }

    @Override
    public int getId() { return this.id; }

    @Override
    public void loadStoryDef(Context context, RestServer server) { }

    @Override
    public String getTitle() { return "About Storywell"; }

    @Override
    public boolean isContentSet() { return false; }

    @Override
    public List<StoryContent> getContents() { return null; }

    @Override
    public int getCurrentPageId() { return 0; }

    @Override
    public boolean isCurrent() { return false; }

    @Override
    public void setIsCurrent(boolean isCurrent) { }

    @Override
    public void goToNextPage() { }

    @Override
    public void goToPrevPage() { }

    @Override
    public void goToPageById(int pageIndex) { }

    @Override
    public String getRefreshDateTime() { return null; }

    @Override
    public String getCoverUrl() { return "art_cover_baloons"; }

    @Override
    public String getDefUrl() { return null; }

    @Override
    public String getDefFilename() { return null; }

    @Override
    public StoryType getStoryType() { return StoryType.APP; }
}
