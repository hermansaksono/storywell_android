package edu.neu.ccs.wellness.story.interfaces;

import android.content.Context;

import edu.neu.ccs.wellness.people.GroupInterface;
import edu.neu.ccs.wellness.server.RestServer;
import edu.neu.ccs.wellness.server.RestServer.ResponseType;

/**
 * Created by hermansaksono on 10/30/17.
 */

public interface StoryStateInterface {

    void setCurrentPage(int contentId);

    int getCurrentPage();

    String getRecordingURL(int contentId);

    void addReflection(int contentId, String recordingURL);

    void removeReflection(int contentId);

    boolean isReflectionResponded(int contentId);

    void save(Context context, GroupInterface group);

    //ResponseType sync(RestServer server);
}
