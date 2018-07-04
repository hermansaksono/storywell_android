package edu.neu.ccs.wellness.reflection;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.neu.ccs.wellness.story.interfaces.StoryStateInterface;

/**
 * Created by hermansaksono on 3/5/18.
 */

public class ReflectionManager {
    public static final String FIREBASE_REFLECTIONS_FIELD = "group_reflections_history";
    private static final String REFLECTION_LOCAL_FORMAT = "/reflection_story_%s_content_%s.3gp";
    private static final String REFLECTION_FIREBASE_FORMAT = "reflection_story_%s_content_%s %s.3gp";
    private static final DateFormat REFLECTION_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private boolean isPlaying = false;
    private boolean isRecording = false;
    private final String groupName;
    private final String storyId;
    private String currentContentId;
    private String currentRecordingAudioFile;
    private boolean isUploadQueueNotEmpty = false;
    private MediaPlayer mediaPlayer;
    private MediaRecorder mediaRecorder;
    private DatabaseReference firebaseDbRef = FirebaseDatabase.getInstance().getReference();
    private StorageReference firebaseStorageRef = FirebaseStorage.getInstance().getReference();
    private Map<String, String> reflectionUrls = new HashMap<String, String>();


    /* CONSTRUCTOR */
    public ReflectionManager(String groupName, String storyId) {
        this.groupName = groupName;
        this.storyId = storyId;
        this.getReflectionUrlsFromFirebase();
    }

    /* AUDIO RECORDING PUBLIC METHODS */
    public boolean getIsPlayingStatus() {
        return this.isPlaying;
    }

    public boolean getIsRecordingStatus() {
        return this.isRecording;
    }

    public void startPlayback(String audioPath, MediaPlayer mediaPlayer) {
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
            }
        });
    }

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

    public void startRecording(Context context, String contentId, MediaRecorder mediaRecorder) {
        if (this.isPlaying == true) {
            this.stopPlayback();
        }
        if (this.isRecording == false) {
            this.setIsRecordingState(true);
            this.currentContentId = contentId;
            this.currentRecordingAudioFile = getOutputFilePath(context, contentId);
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
                }
            }
        }
    }

    public void stopRecording() {
        if (this.mediaRecorder != null && this.isRecording == true) {
            this.reflectionUrls.put(currentContentId, currentRecordingAudioFile);
            this.mediaRecorder.stop();
            this.mediaRecorder.release();
            this.mediaRecorder = null;
            this.setIsRecordingState(false);
        }
    }

    /* PRIVATE METHODS */
    private void setIsPlayingState(boolean status){
        this.isPlaying = status;
    }

    private void setIsRecordingState(boolean status){
        this.isRecording = status;
    }

    private String getOutputFilePath(Context context, String contentId) {
        String cachePath = context.getCacheDir().getAbsolutePath();
        String filename = String.format(REFLECTION_LOCAL_FORMAT, this.storyId, contentId);
        StringBuilder sb = new StringBuilder();
        sb.append(cachePath);
        sb.append(filename);
        return sb.toString();
    }

    /* REFLECTION STATE METHODS */
    public boolean isReflectionResponded(String contentId) {
        return this.reflectionUrls.containsKey(contentId);
    }

    public String getRecordingURL(String contentId) {
        return this.reflectionUrls.get(contentId);
    }

    /* FIREBASE STORAGE PUBLIC METHODS*/
    public boolean isUploadQueued() {
        return this.isUploadQueueNotEmpty;
    }

    public void getReflectionUrlsFromFirebase() {
        this.firebaseDbRef
                .child(FIREBASE_REFLECTIONS_FIELD)
                .child(groupName)
                .child(storyId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        reflectionUrls = processReflectionsUrls(dataSnapshot);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        reflectionUrls.clear();
                    }
                });
    }

    public void uploadReflectionAudioToFirebase(final StoryStateInterface state) {
        String dateString = REFLECTION_DATE_FORMAT.format(new Date());
        String firebaseName = String.format(REFLECTION_FIREBASE_FORMAT, storyId, currentContentId, dateString);
        final File localAudioFile = new File(currentRecordingAudioFile);
        final Uri audioUri = Uri.fromFile(localAudioFile);
        this.firebaseStorageRef
                .child(FIREBASE_REFLECTIONS_FIELD)
                .child(groupName)
                .child(storyId)
                .child(currentContentId)
                .child(firebaseName)
                .putFile(audioUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        String downloadUrl = taskSnapshot.getMetadata().getReference().getDownloadUrl().toString();
                        addReflectionUrlToFirebase(state, currentContentId, downloadUrl);
                        deleteLocalReflectionFile(localAudioFile);
                        isUploadQueueNotEmpty = false;
                    }
                });
    }

    public void addReflectionUrlToFirebase(final StoryStateInterface state, String pageId, String audioUrl) {
        state.addReflection(Integer.valueOf(pageId), audioUrl);
        this.firebaseDbRef
                .child(FIREBASE_REFLECTIONS_FIELD)
                .child(groupName)
                .child(storyId)
                .child(pageId)
                .push().setValue(audioUrl);
        this.reflectionUrls.put(pageId, audioUrl);
    }

    public void deleteLocalReflectionFile(File file) {
        try {
            file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Map<String, String> processReflectionsUrls(DataSnapshot dataSnapshot) {
        HashMap<String, String> reflectionUrlsHashMap = new HashMap<String, String>();
        if (dataSnapshot.exists()) {
            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                List<Object> listOfUrls = new ArrayList<>((Collection<?>)
                        ((HashMap<Object, Object>) ds.getValue()).values());
                String key = ds.getKey();
                String value = getLastReflectionsUrl(listOfUrls);
                reflectionUrlsHashMap.put(key, value);
            }
        }
        return reflectionUrlsHashMap;
    }

    private static String getLastReflectionsUrl(List<Object> listOfUrl) {
        return (String) listOfUrl.get(listOfUrl.size() - 1);
    }
}
