package edu.neu.ccs.wellness.reflection;

import android.content.Context;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.view.SurfaceHolder;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

/**
 * Created by hermansaksono on 3/5/18.
 */

public class ReflectionManager extends ResponseManager {
    private static final String REFLECTION_FILENAME_FORMAT = "/reflection_story_%s_content_%s.3gp";

    private boolean isPlaying = false;
    private boolean isRecording = false;
    private final String groupName;
    private final String storyId;
    private String currentContentId;
    private String currentContentGroupId;
    private String currentContentGroupName;
    private String currentRecordingAudioFile;
    private boolean isUploadQueueNotEmpty = false;
    private MediaPlayer mediaPlayer;
    private MediaRecorder mediaRecorder;
    private FirebaseReflectionRepository reflectionRepository;
    private String reflectionFileNameFormat = REFLECTION_FILENAME_FORMAT;
    private String cachePath;


    /* CONSTRUCTOR */
    public ReflectionManager(String groupName, String storyId,
                             int reflectionIteration, long reflectionMinEpoch, Context context) {
        this.groupName = groupName;
        this.storyId = storyId;
        this.reflectionRepository = new FirebaseReflectionRepository(
                groupName, storyId, reflectionIteration, reflectionMinEpoch);
        this.cachePath = context.getCacheDir().getAbsolutePath();
    }

    public ReflectionManager(String groupName, String storyId,
                             int reflectionIteration, long reflectionMinEpoch,
                             String filenameFormat, Context context) {
        this.groupName = groupName;
        this.storyId = storyId;
        this.reflectionRepository = new FirebaseCalmingResponseRepository(
                groupName, storyId, reflectionIteration, reflectionMinEpoch);
        this.cachePath = context.getCacheDir().getAbsolutePath();
        this.reflectionFileNameFormat = filenameFormat;
    }

    /* GENERAL METHODS */
    @Override
    public boolean getIsPlayingStatus() {
        return this.isPlaying;
    }

    public boolean getIsRecordingStatus() {
        return this.isRecording;
    }

    private void setIsPlayingState(boolean status) {
        this.isPlaying = status;
    }

    private void setIsRecordingState(boolean status) {
        this.isRecording = status;
    }

    @Override
    public boolean isReflectionResponded(String contentId) {
        return this.reflectionRepository.isReflectionResponded(contentId);
    }

    @Override
    public String getRecordingURL(String contentId) {
        return this.reflectionRepository.getRecordingURL(contentId);
    }

    /* AUDIO PLAYBACK METHODS */
    @Override
    public void startPlayback(String audioPath, MediaPlayer mediaPlayer,
                              final OnCompletionListener completionListener) {
        this.setIsPlayingState(true);
        this.mediaPlayer = mediaPlayer;
        try {
            this.mediaPlayer.setDataSource(audioPath);
            this.mediaPlayer.prepare();
            this.mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
            this.setIsPlayingState(false);
        }

        this.mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                stopPlayback();
                completionListener.onCompletion(mediaPlayer);
            }
        });
    }

    @Override
    public void stopPlayback() {
        if (this.mediaPlayer != null) {
            if (this.mediaPlayer.isPlaying()) {
                this.mediaPlayer.stop();
            }
            this.mediaPlayer.release();
            this.mediaPlayer = null;
            this.setIsPlayingState(false);
        }
    }

    /* AUDIO RECORDING METHODS */
    @Override
    public void startRecording(String contentId,
                               String reflectionGroupId, String reflectionGroupName,
                               MediaRecorder mediaRecorder) {
        if (this.isPlaying == true) {
            this.stopPlayback();
        }
        if (this.isRecording == false) {
            this.setIsRecordingState(true);
            this.currentContentId = contentId;
            this.currentContentGroupId = reflectionGroupId;
            this.currentContentGroupName = reflectionGroupName;
            this.currentRecordingAudioFile = getOutputFilePath(cachePath, storyId, contentId);
            this.isUploadQueueNotEmpty = true;
            this.mediaRecorder = mediaRecorder;
            this.mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            this.mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            this.mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            this.mediaRecorder.setOutputFile(this.currentRecordingAudioFile);
            try {
                this.mediaRecorder.prepare();
                this.mediaRecorder.start();
            } catch (IOException e) {
                this.setIsRecordingState(false);
                if (this.mediaRecorder != null) {
                    this.mediaRecorder.stop();
                    this.mediaRecorder.reset();
                    this.mediaRecorder.release(); // TODO may cause bugs
                }
            }
        }
    }

    private String getOutputFilePath(String path, String storyId, String contentId) {
        String filename = String.format(this.reflectionFileNameFormat, storyId, contentId);
        StringBuilder sb = new StringBuilder();
        sb.append(path);
        sb.append(filename);
        return sb.toString();
    }

    @Override
    public void stopRecording() {
        if (this.mediaRecorder != null && this.isRecording == true) {
            this.reflectionRepository.putRecordingURL(currentContentId, currentRecordingAudioFile);
            this.mediaRecorder.stop();
            this.mediaRecorder.release();
            this.mediaRecorder = null;
            this.setIsRecordingState(false);
        }
    }

    /* FIREBASE STORAGE PUBLIC METHODS*/
    @Override
    public boolean isUploadQueued() {
        return this.isUploadQueueNotEmpty;
    }

    @Override
    public void getReflectionUrlsFromFirebase(
            long reflectionMinEpoch, ValueEventListener listener) {
        this.reflectionRepository.getReflectionUrlsFromFirebase(
                groupName, storyId, reflectionMinEpoch, listener);
    }

    @Override
    public void uploadReflectionAudioToFirebase() {
        this.reflectionRepository.uploadReflectionFileToFirebase(
                groupName, storyId,
                currentContentId, currentContentGroupId, currentContentGroupName,
                currentRecordingAudioFile, new OnSuccessListener<UploadTask.TaskSnapshot>(){
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        isUploadQueueNotEmpty = false;
                    }
                });
    }

    /* VIDEO RECORDING */
    private static final CamcorderProfile VID_CP_HIGH =
            CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
    private static final int VID_MAX_DURATION_MILLIS = 50 * 1000;
    private static final int VID_MAX_FILE_SIZE_BYTES = 5000 * 000;
    private static final String VID_REFLECTION_PATH_FORMAT = "/vid_refl_story_%s_content_%s.mp4";

    /**
     *
     * Ref: https://stackoverflow.com/questions/1817742/how-can-i-record-a-video-in-my-android-app
     * @param contentId
     */
    @Override
    public void startVideoRecording(String contentId, MediaRecorder mediaRecorder) {
        if (this.isPlaying) {
            this.stopPlayback();
        }
        if (this.isRecording) {
            this.setIsRecordingState(true);
            this.currentContentId = contentId;
            this.currentRecordingAudioFile = getVideoOutputPath(cachePath, storyId, contentId);
            this.isUploadQueueNotEmpty = true;

            this.mediaRecorder = mediaRecorder;
            this.mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            this.mediaRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
            this.mediaRecorder.setMaxDuration(VID_MAX_DURATION_MILLIS);
            this.mediaRecorder.setProfile(VID_CP_HIGH);
            this.mediaRecorder.setMaxFileSize(VID_MAX_FILE_SIZE_BYTES);

            // this.mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            this.mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            this.mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
            this.mediaRecorder.setOutputFile(this.currentRecordingAudioFile);
            try {
                this.mediaRecorder.prepare();
                this.mediaRecorder.start();
            } catch (IOException e) {
                this.setIsRecordingState(false);
                if (this.mediaRecorder != null) {
                    this.mediaRecorder.stop();
                    this.mediaRecorder.reset();
                }
            }
        }
    }

    @Override
    public void stopVideoRecording() {
        if (this.mediaRecorder != null && this.isRecording) {
            this.reflectionRepository.putRecordingURL(currentContentId, currentRecordingAudioFile);
            this.mediaRecorder.stop();
            this.mediaRecorder.release();
            this.mediaRecorder = null;
            this.setIsRecordingState(false);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        this.prepareRecorder(surfaceHolder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        this.stopVideoRecording();
    }

    private void prepareRecorder(SurfaceHolder surfaceHolder) {
        this.mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());
    }

    /* HELPER METHODS */
    private static String getVideoOutputPath(String path, String storyId, String contentId) {
        String filename = String.format(VID_REFLECTION_PATH_FORMAT, storyId, contentId);
        StringBuilder sb = new StringBuilder();
        sb.append(path);
        sb.append(filename);
        return sb.toString();
    }
}
