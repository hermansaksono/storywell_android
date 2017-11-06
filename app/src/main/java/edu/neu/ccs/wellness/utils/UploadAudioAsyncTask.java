package edu.neu.ccs.wellness.utils;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.Date;

import edu.neu.ccs.wellness.storytelling.Storywell;

import static edu.neu.ccs.wellness.storytelling.StoryListFragment.storyIdClicked;
import static edu.neu.ccs.wellness.storytelling.StoryViewActivity.mViewPager;
import static edu.neu.ccs.wellness.storytelling.storyview.ReflectionFragment.downloadUrl;
import static edu.neu.ccs.wellness.storytelling.storyview.ReflectionFragment.reflectionsAudioLocal;
import static edu.neu.ccs.wellness.storytelling.storyview.ReflectionFragment.uploadToFirebase;
import static edu.neu.ccs.wellness.utils.StreamReflectionsFirebase.reflectionsUrlHashMap;


public class UploadAudioAsyncTask extends AsyncTask<Void, Void, Void> {
    /**
     * For writing to Database
     */
    private DatabaseReference mDBReference = FirebaseDatabase.getInstance().getReference();
    private Context context;
    private int pageId;
    private Storywell storywell;


    public UploadAudioAsyncTask(Context contextReceived, int idFromReflectionsFragment) {
        this.pageId = idFromReflectionsFragment;
        context = contextReceived;
        storywell = new Storywell(context);
    }

    @Override
    protected Void doInBackground(Void... voids) {

        /**
         * For writing to Storage
         * Upload the video to storage
         * */
        StorageReference mFirebaseStorageRef = FirebaseStorage.getInstance().getReference();

        //Directory structure is user_id/story_id/reflection_id_{TIMESTAMP_START_RECORDING}/3gp
        mFirebaseStorageRef
                .child(storywell.getGroup().getName())
                .child(String.valueOf((storyIdClicked >= 0) ? storyIdClicked : 0))
                .child(String.valueOf(pageId))
                .child(String.valueOf(new Date()))
                .putFile(Uri.fromFile(new File(reflectionsAudioLocal))).
                addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //Send this downloadUrl to Reflection Server
                        Uri downloadUri = taskSnapshot.getDownloadUrl();
                        try {
                            assert downloadUri != null;
                            downloadUrl = downloadUri.toString();

                            //Save the Download Url in Database as well
                            mDBReference
                                    .child(storywell.getGroup().getName())
                                    .child(String.valueOf((storyIdClicked >= 0) ? storyIdClicked : 0))
                                    .child(String.valueOf(pageId))
                                    .push().setValue(downloadUrl);

//                            Toast.makeText(getContext(), downloadUrl, Toast.LENGTH_LONG).show();
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        } finally {
                            /*try {
                                context.deleteFile(
                                        String.valueOf(new FileInputStream
                                                (new File(reflectionsAudioLocal.toString())
                                                )
                                        )
                                );
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                            */
//                            Toast.makeText(getContext(), "FILE DELETED", Toast.LENGTH_LONG).show();
                        }
                    }
                });

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        uploadToFirebase = false;
        reflectionsUrlHashMap.put(mViewPager.getCurrentItem() - 1, downloadUrl);
    }
}