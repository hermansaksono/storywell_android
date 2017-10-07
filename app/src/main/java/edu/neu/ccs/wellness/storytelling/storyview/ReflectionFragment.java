package edu.neu.ccs.wellness.storytelling.storyview;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

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
    //Request Audio Permissions as AUDIO RECORDING falls under DANGEROUS PERMISSIONS
    private static final int REQUEST_AUDIO_PERMISSIONS = 100;
    private boolean isPermissionGranted = false;
    private String[] permission = {Manifest.permission.RECORD_AUDIO};
    private Boolean isRecording = false;
    //Audio File Name
    private static String mReflectionsAudioFile;

    //Initialize the MediaPlayback for Reflections Playback
    MediaPlayer mMediaPlayer;


    public ReflectionFragment() {
    }

    // CONSTRUCTORS

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

        //Request Audio Record Permissions
        ActivityCompat.requestPermissions(getActivity(), permission, REQUEST_AUDIO_PERMISSIONS);
        //Write the audioFile in the cache
        try {
            mReflectionsAudioFile = getActivity().getExternalCacheDir().getAbsolutePath();
        } catch (Exception e) {
            Log.e("FILE_MANAGER", e.getMessage());
        }
        mReflectionsAudioFile += "/APPEND_USERNAME.3gp";

        buttonReplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPlayback(!isRecording);
            }
        });

        buttonRespond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRespondButtonPressed(getActivity(), view);
                onRecord(!isRecording);
            }
        });

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnGoToFragmentListener.onGoToFragment(TransitionType.ZOOM_OUT, 1);
                //TODO: If savedAudio is null, do not go to next screen
                uploadAudioToFirebase();
                if (mMediaPlayer != null) {
                    mMediaPlayer.release();
                }
                mMediaRecorder = null;

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
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //Get the requestCode and check our case
        switch (requestCode) {
            case REQUEST_AUDIO_PERMISSIONS:
                //If Permission is Granted, change the boolean value
                isPermissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }

        //If Permission is denied, show a Snackbar
        //Display a button on the Snackbar to request permissions again
        if (!isPermissionGranted) {
            Snackbar permissionRequestSnackbar = Snackbar.make(getView().findViewById(android.R.id.content),
                    "Permission Needed To Record Audio",
                    Snackbar.LENGTH_INDEFINITE);
            permissionRequestSnackbar.setAction("TRY AGAIN", grantPermissionListener);
            permissionRequestSnackbar.show();
        }
    }

    // Implement an onClickListener for Snackbar Button
    // Show the Snackbar if permission is denied
    // and ask for permissions if denied
    View.OnClickListener grantPermissionListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //Show Permissions Dialog
        }
    };

        int count =0;
    private void onRecord(boolean start) {
        if (start && count ==0) {
            Log.e("STARTED_REC","STARTED_REC");
            startRecording();
        } else {
            Log.e("STOPPED","STOPPED_REC");
            stopRecording();
        }
    }

    private void startRecording() {

        mMediaRecorder = new MediaRecorder();

        //Set the Mic as the Audio Source
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        //TODO: DECIDE OUTPUT FORMAT
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mMediaRecorder.setOutputFile(mReflectionsAudioFile);

        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("MEDIA_RECORDER", e.getMessage());
        }

        try {
            mMediaRecorder.start();
            count++;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void stopRecording() {
        mMediaRecorder.stop();
        mMediaRecorder.reset();
    }


    private void uploadAudioToFirebase() {
        //TODO: UPLOAD TO FIREBASE
    }

    /***************************************************************
     * METHODS TO PLAY AUDIO
     ***************************************************************/
    private void onPlayback(boolean start) {
        if (start) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopPlayback() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
        }
    }


}