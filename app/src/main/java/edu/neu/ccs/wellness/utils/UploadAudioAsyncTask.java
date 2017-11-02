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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Date;

import static edu.neu.ccs.wellness.storytelling.StoryListFragment.storyIdClicked;
import static edu.neu.ccs.wellness.storytelling.StoryViewActivity.mViewPager;
import static edu.neu.ccs.wellness.storytelling.storyview.ReflectionFragment.REFLECTION_AUDIO_LOCAL;
import static edu.neu.ccs.wellness.storytelling.storyview.ReflectionFragment.downloadUrl;
import static edu.neu.ccs.wellness.storytelling.storyview.ReflectionFragment.uploadToFirebase;


public class UploadAudioAsyncTask extends AsyncTask<Void, Void, Void> {
    private DatabaseReference mDBReference = FirebaseDatabase.getInstance().getReference();
    private Context context;

    public UploadAudioAsyncTask(Context contextReceived){
        context = contextReceived;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... voids) {

        //Upload the video to storage
        StorageReference mFirebaseStorageRef = FirebaseStorage.getInstance().getReference();

        // Right Now, the file upload is such that the original file will get replaced every time
        // in online Storage.
        // Even when the user presses next and then comes back and records audio, the file will
        // get replaced.

        //Directory structure is user_id/story_id/reflection_id_{TIMESTAMP_START_RECORDING}/3gp
        mFirebaseStorageRef
                .child("USER_ID")
                .child(String.valueOf((storyIdClicked >= 0) ? storyIdClicked : 0))
                .child(String.valueOf(mViewPager.getCurrentItem()))
                .child(String.valueOf(new Date()))
                .putFile(Uri.fromFile(new File(REFLECTION_AUDIO_LOCAL))).
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
                                    .child("USER_ID")
                                    .child(String.valueOf((storyIdClicked >= 0) ? storyIdClicked : 0))
                                    .child(String.valueOf(mViewPager.getCurrentItem()))
                                    .push().setValue(downloadUrl);

//                            Toast.makeText(getContext(), downloadUrl, Toast.LENGTH_LONG).show();
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                context.deleteFile(String.valueOf(new FileInputStream(new File(REFLECTION_AUDIO_LOCAL))));
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
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
    }
}
