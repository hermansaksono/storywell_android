package edu.neu.ccs.wellness.storytelling.interfaces;

import android.content.Context;

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

    void save(Context context);
}
