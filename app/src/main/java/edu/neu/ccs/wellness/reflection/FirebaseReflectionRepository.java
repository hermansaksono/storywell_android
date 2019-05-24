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
    private static final String FIREBASE_REFLECTIONS_ROOT = "group_reflections_history";
    private static final String FIRESTORE_FILENAME_FORMAT = "story%s_content%s %s.3gp";
    private static final String REFLECTION_DATE_FORMAT ="yyyy-MM-dd HH:mm:ss";

    private String firebaseRoot = FIREBASE_REFLECTIONS_ROOT;
    private String firebaseFilenameFormat = FIRESTORE_FILENAME_FORMAT;
    private DatabaseReference firebaseDbRef = FirebaseDatabase.getInstance().getReference();
    private StorageReference firebaseStorageRef = FirebaseStorage.getInstance().getReference();
    private Map<String, String> reflectionUrls = new HashMap<String, String>();
    private int treasureItemType = TreasureItemType.STORY_REFLECTION;
    private int reflectionIteration;

    private boolean isUploadQueueNotEmpty = false;

    public FirebaseReflectionRepository(
            String groupName, String storyId, int reflectionIteration, long reflectionMinEpoch) {
        this.reflectionIteration = reflectionIteration;
        this.getReflectionUrlsFromFirebase(groupName, storyId, reflectionMinEpoch);
    }

    public FirebaseReflectionRepository(
            String groupName, String storyId, int reflectionIteration, long reflectionMinEpoch,
            String firebaseRoot, String firestoreFilenameFormat, int treasureItemType) {
        this.reflectionIteration = reflectionIteration;
        this.firebaseRoot = firebaseRoot;
        this.firebaseFilenameFormat = firestoreFilenameFormat;
        this.getReflectionUrlsFromFirebase(groupName, storyId, reflectionMinEpoch);
        this.treasureItemType = treasureItemType;
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
    public void getReflectionUrlsFromFirebase(String groupName, String storyId, long epoch) {
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
        this.getReflectionUrlsFromFirebase(groupName, storyId, epoch, listener);
    }

    public void getReflectionUrlsFromFirebase(
            String groupName, String storyId, final long epoch, final ValueEventListener listener) {
        this.firebaseDbRef
                .child(this.firebaseRoot)
                .child(groupName)
                .child(storyId)
                .orderByKey()
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        reflectionUrls = processReflectionsUrls(dataSnapshot, epoch);
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

    private static Map<String, String> processReflectionsUrls(
            DataSnapshot dataSnapshot, long epoch) {
        HashMap<String, String> reflectionUrlsHashMap = new HashMap<>();
        if (dataSnapshot.exists()) {
            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                String key = ds.getKey();
                addKeyValuePairToReflectionsMap(key, getRecordingHistory(ds, epoch),
                        reflectionUrlsHashMap);
            }
        }
        return reflectionUrlsHashMap;
    }

    private static List<String> getRecordingHistory(DataSnapshot dataSnapshot, long epoch) {
        List<String> history = new ArrayList<>();
        for (DataSnapshot children : dataSnapshot.getChildren()) {
            if (Long.valueOf(children.getKey()) >= epoch) {
                history.add(children.getValue(String.class));
            }
        }
        return history;
    }

    private static void addKeyValuePairToReflectionsMap(String key, List<String> recordingHistory,
                                                        Map<String, String> reflectionUrlsHashMap) {
        if (recordingHistory.isEmpty()) {

        } else {
            reflectionUrlsHashMap.put(key, getLastReflectionsUrl(recordingHistory));
        }
    }

    private static String getLastReflectionsUrl(List<String> listOfUrl) {
        if (listOfUrl.size() > 0) {
            return listOfUrl.get(listOfUrl.size() - 1);
        } else {
            return null;
        }

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
            String path, final OnSuccessListener<UploadTask.TaskSnapshot> onSuccessListener) {
        final Calendar recordingCal = (Calendar.getInstance(Locale.US));
        String firebaseName = getReflectionFilename(storyId, contentId, recordingCal);
        final File localAudioFile = new File(path);
        final Uri audioUri = Uri.fromFile(localAudioFile);
        this.firebaseStorageRef
                .child(this.firebaseRoot)
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
                        onSuccessListener.onSuccess(taskSnapshot);
                    }
                });
    }

    private String getReflectionFilename(String storyId, String contentId, Calendar cal) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(REFLECTION_DATE_FORMAT, Locale.US);
        String dateString = dateFormatter.format(cal.getTime());
        return String.format(firebaseFilenameFormat, storyId, contentId, dateString);
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
                .child(this.firebaseRoot)
                .child(groupName)
                .child(storyId)
                .child(pageId)
                .child(timestampString)
                .setValue(audioUrl);

        // Save story reflection as a TreasureItem in Firebase DB.
        this.addReflectionUrlAsTreasure(groupName, storyId, pageId, pageGroup,
                title, audioUrl, timestamp, this.reflectionIteration, this.treasureItemType);

        // Put reflection uri to the instance's field
        this.reflectionUrls.put(pageId, audioUrl);
    }

    private static void addReflectionUrlAsTreasure(String groupName, String storyId,
                                            String pageId, String pageGroup, String title,
                                            String audioUrl, long timestamp,
                                            int reflectionIteration, int treasureItemType) {
        FirebaseTreasureRepository treasureRepository = new FirebaseTreasureRepository();
        switch (treasureItemType) {
            case TreasureItemType.STORY_REFLECTION:
                treasureRepository.addStoryReflection(groupName, storyId, pageId, pageGroup,
                        title, audioUrl, timestamp, reflectionIteration);
                break;
            case TreasureItemType.CALMING_PROMPT:
                treasureRepository.addCalmingReflection(groupName, storyId, pageId, pageGroup,
                        title, audioUrl, timestamp, reflectionIteration);
        }
    }


    private void deleteLocalReflectionFile(File file) {
        try {
            file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
