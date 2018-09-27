package edu.neu.ccs.wellness.reflection;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;

/**
 * Created by hermansaksono on 8/13/18.
 */

public interface PlaybackManager {
    void startPlayback(String audioPath, MediaPlayer mediaPlayer,
                            final MediaPlayer.OnCompletionListener completionListener);

    void stopPlayback();
}
