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
    private static final String FIREBASE_STATE_FIELD = "group_reflections_state";
    private static final String FIREBASE_REFLECTIONS_FIELD = "group_reflections_history";
    private static final String FIREBASE_REFLECTION_PILE = "group_reflections_pile";
    private static final String REFLECTION_NAME = "reflection_story_%s_content_%s %s.3gp";

    private static final DateFormat REFLECTION_DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private DatabaseReference firebaseDbRef = FirebaseDatabase.getInstance().getReference();
    private StorageReference firebaseStorageRef = FirebaseStorage.getInstance().getReference();
    private Map<String, String> reflectionUrls = new HashMap<String, String>();
    private List<ResponsePile> responsePiles = new ArrayList<>();
    private int reflectionIteration = 0;

    private boolean isUploadQueueNotEmpty = false;

    public FirebaseReflectionRepository(String groupName, String storyId, int reflectionIteration) {
        this.reflectionIteration = reflectionIteration;
        this.getReflectionUrlsFromFirebase(groupName, storyId);
        this.refreshReflectionPileFromFirebase(groupName);
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

    /* GETTING REFLECTION PILES METHOD */
    public List<ResponsePile> getReflectionPiles() {
        return this.responsePiles;
    }

    /* UPDATING REFLECTION PILES METHOD */
    public void refreshReflectionPileFromFirebase(String groupName) {
        this.firebaseDbRef
                .child(FIREBASE_REFLECTION_PILE)
                .child(groupName)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        responsePiles = processReflectionPile(dataSnapshot);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        responsePiles.clear();
                    }
                });
    }

    /**
     * Get piles from all iteration of reflections
     * @param pilesOfIterations
     * @return
     */
    private static List<ResponsePile> processReflectionPile(DataSnapshot pilesOfIterations) {
        List<ResponsePile> reflectionPile = new ArrayList<>();
        if (pilesOfIterations.exists()) {
            for (DataSnapshot oneIteration : pilesOfIterations.getChildren()) {
                reflectionPile.addAll(getPilesFromOneIteration(oneIteration));
            }
        }
        return reflectionPile;
    }

    private static List<ResponsePile> getPilesFromOneIteration(DataSnapshot oneIteration) {
        List<ResponsePile> reflectionPileFromOneIncarnation = new ArrayList<>();
        if (oneIteration.exists()) {
            for (DataSnapshot oneStory : oneIteration.getChildren()) {
                int storyId = Integer.getInteger(oneStory.getKey());
                reflectionPileFromOneIncarnation.addAll(getPilesFromOneStory(oneStory, storyId));
            }
        }
        return reflectionPileFromOneIncarnation;
    }

    private static List<ResponsePile> getPilesFromOneStory(DataSnapshot dataSnapshot, int storyId) {
        List<ResponsePile> reflectionPileFromOneStory= new ArrayList<>();
        if (dataSnapshot.exists()) {
            for (DataSnapshot reflectionGroup : dataSnapshot.getChildren()) {
                ResponsePile pile = new ResponsePile(storyId,
                        getReflectionGroupTitle(reflectionGroup),
                        getPilesFromOneGroup(reflectionGroup));
                reflectionPileFromOneStory.add(pile);
            }
        }
        return reflectionPileFromOneStory;
    }

    private static String getReflectionGroupTitle(DataSnapshot dataSnapshot) {
        if (!dataSnapshot.exists()) {
            return ResponsePile.DEFAULT_RESPONSE_GROUP_NAME;
        }

        DataSnapshot groupNameDS = dataSnapshot.child(ResponsePile.KEY_RESPONSE_GROUP_NAME);
        if (groupNameDS.exists()) {
            return (String) groupNameDS.getValue();
        } else {
            return ResponsePile.DEFAULT_RESPONSE_GROUP_NAME;
        }
    }

    private static Map<String, String> getPilesFromOneGroup(DataSnapshot dataSnapshot) {
        Map<String, String> reflectionList = new HashMap<>();
        if (dataSnapshot.exists()) {
            DataSnapshot responseDataSnapshot = dataSnapshot.child(ResponsePile.KEY_RESPONSE_PILE);
            for (DataSnapshot oneReflection : responseDataSnapshot.getChildren()) {
                reflectionList.put(oneReflection.getKey(), (String) oneReflection.getValue());
            }
        }
        return reflectionList;
    }

    /* REFLECTION UPLOADING METHODS */
    public void uploadReflectionFileToFirebase(
            final String groupName, final String storyId,
            final String contentId, final String contentGroup, final String contentGroupName,
            String path) {
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
                        addReflectionUrlToFirebase(
                                groupName, storyId, contentId, contentGroup, contentGroupName,
                                downloadUrl);
                        deleteLocalReflectionFile(localAudioFile);
                        isUploadQueueNotEmpty = false;
                    }
                });
    }

    private void addReflectionUrlToFirebase(String groupName, String storyId,
                                            String pageId, String pageGroup, String pageGroupName,
                                            String audioUrl) {
        /* SAVING REFLECTION LIST */
        this.firebaseDbRef
                .child(FIREBASE_REFLECTIONS_FIELD)
                .child(groupName)
                .child(storyId)
                .child(pageId)
                .push().setValue(audioUrl);

        /* SAVING REFLECTION PILES */
        this.firebaseDbRef
                .child(FIREBASE_REFLECTION_PILE)
                .child(groupName)
                .child(Integer.toString(this.reflectionIteration))
                .child(storyId)
                .child(pageGroup)
                .child(ResponsePile.KEY_RESPONSE_PILE)
                .child(pageId)
                .setValue(audioUrl);

        this.saveContentGroupName(groupName, storyId, pageGroup, pageGroupName);

        /* SAVING REFLECTIONS */
        this.reflectionUrls.put(pageId, audioUrl);
    }

    private void saveContentGroupName(String groupName,
                                      String storyId, String pageGroup, String pageGroupName) {
        if (pageGroupName.equals(ResponsePile.DEFAULT_RESPONSE_GROUP_NAME)) {
            this.firebaseDbRef
                    .child(FIREBASE_REFLECTION_PILE)
                    .child(groupName)
                    .child(Integer.toString(this.reflectionIteration))
                    .child(storyId)
                    .child(pageGroup)
                    .child(ResponsePile.KEY_RESPONSE_GROUP_NAME)
                    .setValue(pageGroupName);
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
