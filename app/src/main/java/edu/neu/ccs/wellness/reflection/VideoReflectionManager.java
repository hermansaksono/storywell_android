package edu.neu.ccs.wellness.reflection;

import android.content.Context;
import android.media.MediaRecorder;
import android.view.SurfaceHolder;

/**
 * Created by hermansaksono on 8/13/18.
 */

public interface VideoReflectionManager extends SurfaceHolder.Callback {
    void startVideoRecording(
            Context context, String reflectionParentId, MediaRecorder mediaRecorder);

    void stopVideoRecording();
}
