package edu.neu.ccs.wellness.storytelling.storyview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

import edu.neu.ccs.wellness.storytelling.R;
import edu.neu.ccs.wellness.storytelling.StoryViewActivity;
import edu.neu.ccs.wellness.storytelling.models.StoryReflection;
import edu.neu.ccs.wellness.utils.OnGoToFragmentListener;
import edu.neu.ccs.wellness.utils.OnGoToFragmentListener.TransitionType;

import static edu.neu.ccs.wellness.StreamReflectionsFirebase.reflectionsUrlHashMap;
import static edu.neu.ccs.wellness.storytelling.StoryListFragment.storyIdClicked;
import static edu.neu.ccs.wellness.storytelling.StoryViewActivity.mViewPager;

/**
 * Recording and Playback of Audio
 * For reference use Android Docs
 * https://developer.android.com/guide/topics/media/mediarecorder.html
 */
public class ReflectionFragment extends Fragment {
    private static final String KEY_TEXT = "KEY_TEXT";
    private static final int CONTROL_BUTTON_OFFSET = 10;
    private DatabaseReference mDBReference;

    private View view;
    private Button buttonReplay;
    private Button buttonRespond;
    private Button buttonNext;
    private View progressBar;
    private float controlButtonVisibleTranslationY;

    private OnGoToFragmentListener mOnGoToFragmentListener;
    private Boolean isResponding = false;
    //Initialize the MediaRecorder for Reflections Recording
    MediaRecorder mMediaRecorder;
    public static boolean isPermissionGranted = false;
    private Boolean isRecording = false;
    private Boolean isPlayingNow = false;
    //Audio File Name
    private String REFLECTION_AUDIO_LOCAL;
    // A boolean variable which checks if user has already recorded something
    // and controls uploading file to Firebase
    public static boolean shouldRecord;
    //Initialize the MediaPlayback for Reflections Playback
    MediaPlayer mMediaPlayer;
    private String downloadUrl;
    //A boolean to control movement of user based on if he/she has recorded a reflection or not
    public static boolean isRecordingInitiated = false;
    //A string for the path of file downloaded from Firebase
    String REFLECTION_AUDIO_FIREBASE = "";


    public ReflectionFragment() {
    }


    /**
     * Demo Constructor
     *
     * @param text
     * @return
     */
    public static ReflectionFragment create(String text) {
        ReflectionFragment fragment = new ReflectionFragment();
        Bundle args = new Bundle();
        args.putString(KEY_TEXT, text);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Constructor
     *
     * @param page
     * @return
     */
    public static ReflectionFragment create(StoryReflection page) {
        ReflectionFragment fragment = new ReflectionFragment();
        Bundle args = new Bundle();
        args.putString(KEY_TEXT, page.getText());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDBReference = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_reflection_view, container, false);
        this.buttonRespond = (Button) view.findViewById(R.id.buttonRespond);
        this.buttonNext = (Button) view.findViewById(R.id.buttonNext);
        this.buttonReplay = (Button) view.findViewById(R.id.buttonReplay);
        this.progressBar = view.findViewById(R.id.reflectionProgressBar);
        this.controlButtonVisibleTranslationY = buttonNext.getTranslationY();
        setFirebaseAsPlaybackSource();

        String text = getArguments().getString(StoryContentAdapter.KEY_TEXT);
        String subtext = getArguments().getString(StoryContentAdapter.KEY_SUBTEXT);
        setContentText(view, text, subtext);

        //Write the audioFile in the cache
        try {
            // Write to Internal storage
            // Removed Permission for External Storage from Manifest
            REFLECTION_AUDIO_LOCAL = getActivity().getCacheDir().getAbsolutePath();
        } catch (Exception e) {
            Log.d("FILE_MANAGER", e.getMessage());
        }
        REFLECTION_AUDIO_LOCAL += "/APPEND_USERNAME.3gp";

        //Play the recently recorded Audio
        buttonReplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String audioForPlayback = "";

                //If we have both files
                if (REFLECTION_AUDIO_FIREBASE.length() > 0) {
                    //If there is local file, play that
                    //USE CASE: Even though there is a firebase file and everything is available for Streaming
                    //If the user records a new Reflection - that should be played rather than the old Firebase Audio
                    audioForPlayback = (new File(REFLECTION_AUDIO_LOCAL).length() > 0)
                            ? REFLECTION_AUDIO_LOCAL
                            : REFLECTION_AUDIO_FIREBASE;
                } else {
                    audioForPlayback = (REFLECTION_AUDIO_FIREBASE.length() > 0)
                            ? REFLECTION_AUDIO_FIREBASE
                            //The Only case where both REFLECTION_AUDIO_LOCAL and REFLECTION_AUDIO_FIREBASE are null
                            //Is on the first run.
                            //In such a case, this buttonReplay won't be visible
                            : "";
                }
                onPlayback(isPlayingNow, audioForPlayback);
            }
        });

        /**
         *   Button to record Audio
         */
        buttonRespond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Make it true if user records something new again
                //This controls Uploading of file to Firebase
                shouldRecord = true;
                isRecordingInitiated = true;
                onRespondButtonPressed(getActivity(), view);

                //Stop the Audio
                if (isPlayingNow) {
                    onPlayback(isPlayingNow, REFLECTION_AUDIO_LOCAL);
                }
                onRecord(!isRecording);
            }
        });

        /**
         * Go to Next Fragment
         * */
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnGoToFragmentListener.onGoToFragment(TransitionType.ZOOM_OUT, 1);
                if (shouldRecord) {
                    uploadAudioToFirebase();
                }
            }
        });


        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mOnGoToFragmentListener = (OnGoToFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(((Activity) context).getLocalClassName()
                    + " must implement OnReflectionBeginListener");
        }
    }

    /***
     * Set View to show the Story's content
     * @param view The View in which the content will be displayed
     * @param text The Reflection's text
     * @param subtext The Reflection's extra text
     */
    private void setContentText(View view, String text, String subtext) {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(),
                StoryViewActivity.STORY_TEXT_FACE);
        TextView itv = (TextView) view.findViewById(R.id.reflectionInstruction);
        TextView tv = (TextView) view.findViewById(R.id.reflectionText);
        TextView stv = (TextView) view.findViewById(R.id.reflectionSubtext);

        itv.setTypeface(tf);

        tv.setTypeface(tf);
        tv.setText(text);

        stv.setTypeface(tf);
        stv.setText(subtext);
    }

    private void onRespondButtonPressed(Context context, View view) {
        if (isResponding) {
            stopResponding();
            fadeControlButtonsTo(view, 1);
        } else {
            startResponding();
            fadeControlButtonsTo(view, 0);
        }
    }

    private void startResponding() {
        isResponding = true;
        fadeProgressBarTo(1, R.integer.anim_short);
        changeReflectionButtonTextTo(getString(R.string.reflection_button_stop));
    }

    private void stopResponding() {
        isResponding = false;
        fadeProgressBarTo(0, R.integer.anim_fast);
        changeReflectionButtonTextTo(getString(R.string.reflection_button_answer_again));
    }

    private void changeReflectionButtonTextTo(String text) {
        buttonRespond.setText(text);
    }

    private void fadeProgressBarTo(float alpha, int resId) {
        progressBar.animate()
                .alpha(alpha)
                .setDuration(getResources().getInteger(resId))
                .setListener(null);
    }

    private void fadeControlButtonsTo(View view, float toAlpha) {
        buttonNext.animate()
                .alpha(toAlpha)
                .translationY(getControlButtonOffset(toAlpha))
                .setDuration(getResources().getInteger(R.integer.anim_fast))
                .setListener(new FadeSwitchListener(toAlpha));
        buttonReplay.animate()
                .alpha(toAlpha)
                .translationY(getControlButtonOffset(toAlpha))
                .setDuration(getResources().getInteger(R.integer.anim_fast))
                .setListener(new FadeSwitchListener(toAlpha));
    }

    private float getControlButtonOffset(float toAlpha) {
        return controlButtonVisibleTranslationY + (CONTROL_BUTTON_OFFSET * (1 - toAlpha));
    }

    private class FadeSwitchListener extends AnimatorListenerAdapter {
        private float toAlpha;

        public FadeSwitchListener(float toAlpha) {
            this.toAlpha = toAlpha;
        }

        @Override
        public void onAnimationStart(Animator animation) {
            if (toAlpha > 0) {
                buttonNext.setVisibility(View.VISIBLE);
                buttonReplay.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            if (toAlpha <= 0) {
                buttonNext.setVisibility(View.GONE);
                buttonReplay.setVisibility(View.GONE);
            }
        }
    }

    /*****************************************************************
     * METHODS TO RECORD AUDIO
     *****************************************************************/

    private void onRecord(boolean start) {
        if (start) {
            Log.i("STARTED_REC", "STARTED_REC");
            isRecording = true;
            startRecording();
        } else {
            Log.i("STOPPED", "STOPPED_REC");
            stopRecording();
        }
    }


    /**
     * Start Recording and handle multiple recordings
     * Manage different states
     */
    private void startRecording() {

        mMediaRecorder = new MediaRecorder();
        //Set the Mic as the Audio Source
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mMediaRecorder.setOutputFile(REFLECTION_AUDIO_LOCAL);

        try {
            mMediaRecorder.prepare();
            mMediaRecorder.start();
        } catch (IOException e) {
            isRecording = false;
            if (mMediaRecorder != null) {
                mMediaRecorder.stop();
                mMediaRecorder.reset();
            }
            Log.e("MEDIA_REC_PRE_ERROR", e.getMessage());
        }
    }

    /**
     * Stop the recording and handle multiple recording
     * Release Media Recorder when not needed
     */
    private void stopRecording() {
        if (mMediaRecorder != null) {
            Log.i("stopRecording", "mMediaRec is NOT NULL");
            mMediaRecorder.stop();
            mMediaRecorder.release();
            isRecording = false;
        } else {
            Log.i("stopRecording", "mMediaRec is NULL");
        }
    }


    /***************************************************************
     * METHODS TO PLAY AUDIO
     ***************************************************************/
    private void onPlayback(boolean isPlayingCurrently, String pathForPlayback) {
        if (!isPlayingCurrently) {
            isPlayingNow = true;
            startPlayback(pathForPlayback);
        } else {
            stopPlayback();
        }
    }

    private void startPlayback(String fileForPlayback) {
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(fileForPlayback);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
            buttonReplay.setText(getResources().getText(R.string.reflection_button_replay_stop));
        } catch (Exception e) {
            e.printStackTrace();
            isPlayingNow = false;
        }

        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                stopPlayback();
            }
        });
    }

    private void stopPlayback() {
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            buttonReplay.setText(getResources().getText(R.string.reflection_button_replay));
            mMediaPlayer.release();
            isPlayingNow = false;
        }
    }

    /***************************************************************************
     * UPLOAD TO DATABASE
     ***************************************************************************/


    private void uploadAudioToFirebase() {
        new UploadAudioAsyncTask().execute();
    }


    private class UploadAudioAsyncTask extends AsyncTask<Void, Void, Void> {

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

                                Toast.makeText(getContext(), downloadUrl, Toast.LENGTH_LONG).show();
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            } finally {
                                try {
                                    getContext().deleteFile(String.valueOf(new FileInputStream(new File(REFLECTION_AUDIO_LOCAL))));
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }
                                Toast.makeText(getContext(), "FILE DELETED", Toast.LENGTH_LONG).show();
                            }
                        }
                    });

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            shouldRecord = false;
        }
    }

    /**
     * Request Audio Focus for proper Playbacks during Notifications in background
     */
    private void requestAudioFocus() {
        AudioManager.OnAudioFocusChangeListener mAudioManager = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int i) {
                switch (i) {
                    case AudioManager.AUDIOFOCUS_GAIN:
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS:
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        break;
                    case AudioManager.AUDIOFOCUS_REQUEST_FAILED:
                        break;
                }
            }
        };
    }

    /**
     * Release Media Player
     */
    private void releaseMediaPlayer() {
        try {
            mMediaPlayer.reset();
            mMediaPlayer.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Release Media Recorder
     */
    private void releaseMediaRecorder() {
        try {
            mMediaRecorder.reset();
            mMediaRecorder.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * GET THE LINKS TO STREAM FROM FIREBASE
     */
    private void setFirebaseAsPlaybackSource() {
        //TODO: Make it Dynamic by using mViewPager.getCurrentItem() and remove the 6
        if (reflectionsUrlHashMap.get(mViewPager.getCurrentItem()+1) != null) {
            Log.e("FIREBASE", reflectionsUrlHashMap.get(mViewPager.getCurrentItem()+1));

            //Thus there is an Audio that can be streamed
            //Set the Audio Url as Stream for the playback button
            REFLECTION_AUDIO_FIREBASE = reflectionsUrlHashMap.get(6);

            //Change state of buttons
            buttonNext.setVisibility(View.VISIBLE);
            stopResponding();
//            changeReflectionButtonTextTo(getResources().getString(R.string.reflection_button_answer_again));

            //Change State of navigation Booleans
            //And allow moving forward
            isRecordingInitiated = true;

        }
    }
}