package edu.neu.ccs.wellness.storytelling.storyview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;

import edu.neu.ccs.wellness.storytelling.R;
import edu.neu.ccs.wellness.storytelling.StoryViewActivity;
import edu.neu.ccs.wellness.storytelling.models.StoryReflection;
import edu.neu.ccs.wellness.utils.OnGoToFragmentListener;
import edu.neu.ccs.wellness.utils.OnGoToFragmentListener.TransitionType;

/**
 * Recording and Playback of Audio
 * For reference use Android Docs
 * https://developer.android.com/guide/topics/media/mediarecorder.html
 */
public class ReflectionFragment extends Fragment {
    private static final String KEY_TEXT = "KEY_TEXT";
    private static final int CONTROL_BUTTON_OFFSET = 10;

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
    public static String mReflectionsAudioFile;
    // A boolean variable which checks if user has already recorded something
    // and controls uploading file to Firebase
    public static boolean shouldRecord;
    //Initialize the MediaPlayback for Reflections Playback
    MediaPlayer mMediaPlayer;
    private String downloadUrl;

    //A boolean to control movement of user based on if he/she has recorded a reflection or not
    public static boolean isRecordingInitiated = false;


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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_reflection_view, container, false);
        this.buttonRespond = (Button) view.findViewById(R.id.buttonRespond);
        this.buttonNext = (Button) view.findViewById(R.id.buttonNext);
        this.buttonReplay = (Button) view.findViewById(R.id.buttonReplay);
        this.progressBar = view.findViewById(R.id.reflectionProgressBar);
        this.controlButtonVisibleTranslationY = buttonNext.getTranslationY();

        String text = getArguments().getString(StoryContentAdapter.KEY_TEXT);
        String subtext = getArguments().getString(StoryContentAdapter.KEY_SUBTEXT);
        setContentText(view, text, subtext);

        //Write the audioFile in the cache
        try {
            // Write to Internal storage
            // Removed Permission for External Storage from Manifest
            mReflectionsAudioFile = getActivity().getCacheDir().getAbsolutePath();
        } catch (Exception e) {
            Log.d("FILE_MANAGER", e.getMessage());
        }
        mReflectionsAudioFile += "/APPEND_USERNAME.3gp";

        buttonReplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPlayback(isPlayingNow);
            }
        });

        buttonRespond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shouldRecord = true;
                isRecordingInitiated = true;
                onRespondButtonPressed(getActivity(), view);
                if (isPlayingNow) {
                    onPlayback(isPlayingNow);
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
                if (mMediaPlayer != null) {
                    releaseMediaPlayer();
                }
                if (mMediaRecorder != null) {
                    releaseMediaRecorder();
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
        mMediaRecorder.setOutputFile(mReflectionsAudioFile);

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
    private void onPlayback(boolean isPlayingCurrently) {
        if (!isPlayingCurrently) {
            isPlayingNow = true;
            startPlayback();
        } else {
            stopPlayback();
        }
    }

    private void startPlayback() {
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(mReflectionsAudioFile);
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
        // TODO: WRITE THE RULES OF FIREBASE STORAGE API AFTER THAT
        // TALK ABOUT THE FORMAT AND NAMING CONVENTION REQUIRED FOR STORING FILES IN STORAGE
        new UploadAudioAsyncTask().execute();
    }


    private class UploadAudioAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            //Upload the video to storage
            StorageReference mFirebaseStorageRef = FirebaseStorage.getInstance().getReference();

            // Right Now, the file upload is such that the original file will get replaced every time
            // in online Storage.
            // Even when the user presses next and then comes back and records audio, the file will
            // get replaced.
            mFirebaseStorageRef
                    .child("REFLECTION_ID_GOES_HERE")
                    .child("REFLECTION_USERNAME")
                    .putFile(Uri.fromFile(new File(mReflectionsAudioFile))).
                    addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //Send this downloadUrl to Reflection Server
                            Uri downloadUri = taskSnapshot.getDownloadUrl();
                            try {
                                assert downloadUri != null;
                                downloadUrl = downloadUri.toString();
                                Toast.makeText(getContext(), downloadUrl, Toast.LENGTH_LONG).show();
                            } catch (NullPointerException e) {
                                e.printStackTrace();
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
        mMediaPlayer.stop();
        mMediaPlayer.reset();
        mMediaPlayer.release();
    }

    /**
     * Release Media Recorder
     */
    private void releaseMediaRecorder() {
        mMediaRecorder.stop();
        mMediaRecorder.reset();
        mMediaRecorder.release();
    }

}