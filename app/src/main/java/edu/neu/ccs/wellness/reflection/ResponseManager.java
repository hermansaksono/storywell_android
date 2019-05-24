package edu.neu.ccs.wellness.reflection;

import com.google.firebase.database.ValueEventListener;

/**
 * Created by hermansaksono on 3/2/19.
 */

public abstract class ResponseManager
        implements AudioReflectionManager, VideoReflectionManager, PlaybackManager {

    public abstract void getReflectionUrlsFromFirebase(
            long reflectionMinEpoch, ValueEventListener listener);

    public abstract boolean isReflectionResponded(String contentId);

    public abstract boolean getIsPlayingStatus();

    public abstract boolean getIsRecordingStatus();

    public abstract String getRecordingURL(String contentId);

    public abstract boolean isUploadQueued();

    public abstract void uploadReflectionAudioToFirebase();
}
