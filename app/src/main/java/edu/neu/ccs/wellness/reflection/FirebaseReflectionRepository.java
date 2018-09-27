package edu.neu.ccs.wellness.reflection;

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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hermansaksono on 8/13/18.
 */

class FirebaseReflectionRepository {
    public static final String FIREBASE_REFLECTIONS_FIELD = "group_reflections_history";
    private static final String REFLECTION_NAME = "reflection_story_%s_content_%s %s.3gp";
    private static final DateFormat REFLECTION_DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private DatabaseReference firebaseDbRef = FirebaseDatabase.getInstance().getReference();
    private StorageReference firebaseStorageRef = FirebaseStorage.getInstance().getReference();
    private Map<String, String> reflectionUrls = new HashMap<String, String>();

    private boolean isUploadQueueNotEmpty = false;

    public FirebaseReflectionRepository(String groupName, String storyId) {
        this.getReflectionUrlsFromFirebase(groupName, storyId);
    }

    public boolean isReflectionResponded(String contentId) {
        return this.reflectionUrls.containsKey(contentId);
    }

    public String getRecordingURL(String contentId) {
        return this.reflectionUrls.get(contentId);
    }

    public void putRecordingURL(String contentId, String recordingUri) {
        this.reflectionUrls.put(contentId, recordingUri);
    }

    public boolean isUploadQueued() {
        return this.isUploadQueueNotEmpty;
    }

    public void getReflectionUrlsFromFirebase(String groupName, String storyId) {
        this.firebaseDbRef
                .child(FIREBASE_REFLECTIONS_FIELD)
                .child(groupName)
                .child(storyId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        reflectionUrls = processReflectionsUrls(dataSnapshot);
                        // TODO StoryView should be paused until this data is loaded
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        reflectionUrls.clear();
                    }
                });
    }

    /* REFLECTION UPLOADING METHODS */
    public void uploadReflectionFileToFirebase(
            final String groupName, final String storyId, final String contentId, String path) {
        String dateString = REFLECTION_DATE_FORMAT.format(new Date());
        String firebaseName = String.format(REFLECTION_NAME, storyId, contentId, dateString);
        final File localAudioFile = new File(path);
        final Uri audioUri = Uri.fromFile(localAudioFile);
        this.firebaseStorageRef
                .child(FIREBASE_REFLECTIONS_FIELD)
                .child(groupName)
                .child(storyId)
                .child(contentId)
                .child(firebaseName)
                .putFile(audioUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        String downloadUrl = taskSnapshot.getDownloadUrl().toString();
                        addReflectionUrlToFirebase(groupName, storyId, contentId, downloadUrl);
                        deleteLocalReflectionFile(localAudioFile);
                        isUploadQueueNotEmpty = false;
                    }
                });
    }

    private void addReflectionUrlToFirebase(
            String groupName, String storyId, String pageId, String audioUrl) {
        this.firebaseDbRef
                .child(FIREBASE_REFLECTIONS_FIELD)
                .child(groupName)
                .child(storyId)
                .child(pageId)
                .push().setValue(audioUrl);
        this.reflectionUrls.put(pageId, audioUrl);
    }

    private void deleteLocalReflectionFile(File file) {
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
