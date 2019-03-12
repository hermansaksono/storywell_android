package edu.neu.ccs.wellness.story.interfaces;

import android.content.Context;

import java.util.List;

import edu.neu.ccs.wellness.people.GroupInterface;
import edu.neu.ccs.wellness.server.RestServer;
import edu.neu.ccs.wellness.server.RestServer.ResponseType;

public interface StoryInterface {

    String getId();

    ResponseType tryLoadStoryDef(Context context, RestServer server, GroupInterface group);

    void fetchStoryDef(Context context, RestServer server, GroupInterface group);

    String getTitle();

    String getCoverUrl();

    String getDefUrl();

    String getDefFilename();

    StoryType getStoryType();

    List<StoryContent> getContents();

    StoryContent getContentByIndex(int index);

    StoryStateInterface getState();

    void saveState(Context context, GroupInterface group);

    boolean isContentSet();

    String getRefreshDateTime();

    boolean isLocked();

    String getNextStoryId();

}
