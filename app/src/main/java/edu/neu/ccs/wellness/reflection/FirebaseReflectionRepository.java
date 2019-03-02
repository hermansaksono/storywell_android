package edu.neu.ccs.wellness.reflection;

import android.net.Uri;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by hermansaksono on 8/13/18.
 */

class FirebaseReflectionRepository {
    private static final String FIREBASE_REFLECTIONS_FIELD = "group_reflections_history";
    private static final String REFLECTION_NAME = "reflection_story_%s_content_%s %s.3gp";

    private static final DateFormat REFLECTION_DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private DatabaseReference firebaseDbRef = FirebaseDatabase.getInstance().getReference();
    private StorageReference firebaseStorageRef = FirebaseStorage.getInstance().getReference();
    private Map<String, String> reflectionUrls = new HashMap<String, String>();
    private int reflectionIteration = 0;

    private boolean isUploadQueueNotEmpty = false;

    public FirebaseReflectionRepository(String groupName, String storyId, int reflectionIteration) {
        this.reflectionIteration = reflectionIteration;
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

    /* UPDATING REFLECTION URLS METHOD */
    public void getReflectionUrlsFromFirebase(String groupName, String storyId) {
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // DO NOTHING
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // DO NOTHING
            }
        };
        this.getReflectionUrlsFromFirebase(groupName, storyId, listener);
        /*
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
        */
    }

    public void getReflectionUrlsFromFirebase(
            String groupName, String storyId, final ValueEventListener listener) {
        this.firebaseDbRef
                .child(FIREBASE_REFLECTIONS_FIELD)
                .child(groupName)
                .child(storyId)
                .orderByKey()
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        reflectionUrls = processReflectionsUrls(dataSnapshot);
                        listener.onDataChange(dataSnapshot);
                        // TODO StoryView should be paused until this data is loaded
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        reflectionUrls.clear();
                        listener.onCancelled(databaseError);
                    }
                });
    }

    private static Map<String, String> processReflectionsUrls(DataSnapshot dataSnapshot) {
        HashMap<String, String> reflectionUrlsHashMap = new HashMap<String, String>();
        if (dataSnapshot.exists()) {
            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                /*
                List<Object> listOfUrls = new ArrayList<>((Collection<?>)
                        ((HashMap<Long, String>) ds.getValue()).values());
                String key = ds.getKey();
                String value = getLastReflectionsUrl(listOfUrls);
                */
                String key = ds.getKey();
                String value = getLastReflectionsUrl(getRecordingHistory(ds));
                reflectionUrlsHashMap.put(key, value);
            }
        }
        return reflectionUrlsHashMap;
    }

    private static List<String> getRecordingHistory(DataSnapshot dataSnapshot) {
        List<String> history = new ArrayList<>();
        for (DataSnapshot children : dataSnapshot.getChildren()) {
            history.add(children.getValue(String.class));
        }
        return history;
    }

    private static String getLastReflectionsUrl(List<String> listOfUrl) {
        return listOfUrl.get(listOfUrl.size() - 1);
    }

    /* REFLECTION UPLOADING METHODS */
    /**
     * Upload the given path to a recording file to Firebase Storage. Then put the uri (of the
     * recording in Firebase storage) into Firebase DB.
     * @param groupName
     * @param storyId
     * @param contentId
     * @param contentGroup
     * @param contentGroupName
     * @param path
     */
    public void uploadReflectionFileToFirebase(
            final String groupName, final String storyId,
            final String contentId, final String contentGroup, final String contentGroupName,
            String path) {
        final Calendar recordingCal = (Calendar.getInstance(Locale.US));
        String firebaseName = getReflectionFilename(storyId, contentId, recordingCal);
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
                        deleteLocalReflectionFile(localAudioFile);
                        isUploadQueueNotEmpty = false;
                        addReflectionUrlToFirebase(
                                taskSnapshot, groupName, storyId, contentId,
                                contentGroup, contentGroupName, recordingCal.getTimeInMillis());
                    }
                });
    }

    private static String getReflectionFilename(String storyId, String contentId, Calendar cal) {
        String dateString = REFLECTION_DATE_FORMAT.format(cal.getTime());
        return String.format(REFLECTION_NAME, storyId, contentId, dateString);
    }

    /**
     * Add
     * @param taskSnapshot
     * @param groupName
     * @param storyId
     * @param contentId
     * @param contentGroup
     * @param contentGroupName
     */
    private void addReflectionUrlToFirebase(UploadTask.TaskSnapshot taskSnapshot,
                                            final String groupName, final String storyId,
                                            final String contentId, final String contentGroup,
                                            final String contentGroupName,
                                            final long timestamp) {
        Task<Uri> result = taskSnapshot.getMetadata().getReference().getDownloadUrl();
        result.addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                String downloadUrl = uri.toString();
                addReflectionUrlToFirebase(
                        groupName, storyId, contentId, contentGroup, contentGroupName,
                        downloadUrl, timestamp);
            }
        });
    }

    private void addReflectionUrlToFirebase(String groupName, String storyId,
                                            String pageId, String pageGroup, String title,
                                            String audioUrl, long timestamp) {
        String timestampString = String.valueOf(timestamp);

        // Put the reflection URI to Firebase DB.
        this.firebaseDbRef
                .child(FIREBASE_REFLECTIONS_FIELD)
                .child(groupName)
                .child(storyId)
                .child(pageId)
                .child(timestampString)
                .setValue(audioUrl);

        // Save story reflection as a TreasureItem in Firebase DB.
        new FirebaseTreasureRepository()
                .addStoryReflection(groupName, storyId, pageId, pageGroup,
                        title, audioUrl, timestamp, this.reflectionIteration);

        // Put reflection uri to the instace's field
        this.reflectionUrls.put(pageId, audioUrl);
    }

    private void deleteLocalReflectionFile(File file) {
        try {
            file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
