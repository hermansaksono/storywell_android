package edu.neu.ccs.wellness.reflection;

import android.media.MediaRecorder;

/**
 * Created by hermansaksono on 8/13/18.
 */

public interface AudioReflectionManager {
    void startRecording(
            String reflectionParentId,
            String reflectionGroupId,
            String reflectionGroupName,
            MediaRecorder mediaRecorder);

    void stopRecording();
}
