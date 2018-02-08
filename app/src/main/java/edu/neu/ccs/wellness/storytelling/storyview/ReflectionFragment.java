package edu.neu.ccs.wellness.storytelling.storyview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;


import edu.neu.ccs.wellness.storytelling.MediaPlayerSingleton;
import edu.neu.ccs.wellness.storytelling.R;
import edu.neu.ccs.wellness.storytelling.StoryViewActivity;
import edu.neu.ccs.wellness.story.interfaces.StoryInterface;
import edu.neu.ccs.wellness.story.StoryReflection;
import edu.neu.ccs.wellness.storytelling.Storywell;
import edu.neu.ccs.wellness.storytelling.utils.OnGoToFragmentListener;
import edu.neu.ccs.wellness.storytelling.utils.StoryContentAdapter;

import static edu.neu.ccs.wellness.storytelling.utils.StreamReflectionsFirebase.reflectionsUrlHashMap;

/**
 * Recording and Playback of Audio
 * For reference use Android Docs
 * https://developer.android.com/guide/topics/media/mediarecorder.html
 */
public class ReflectionFragment extends Fragment {


    /***************************************************************************
     * VARIABLE DECLERATION
     ***************************************************************************/

    private static final String KEY_TEXT = "KEY_TEXT";
    private static final int CONTROL_BUTTON_OFFSET = 10;
    private static final Boolean DEFAULT_IS_RESPONSE_STATE = false;

    private View view;
    private OnGoToFragmentListener onGoToFragmentCallback;
    private ReflectionFragmentListener reflectionFragmentListener;

    private int pageId;

    private Button buttonReplay;
    private Button buttonRespond;
    private Button buttonNext;

    /**
     * Ask for Audio Permissions
     */
    public static boolean isPermissionGranted = false;


    // A boolean variable which checks if user has already recorded something
    // and controls uploading file to Firebase
    public static boolean uploadToFirebase;
    public static String downloadUrl;

    /**
     * Audio File Name
     * Made Static as it will be used in uploading to Firebase
     */
    public static String reflectionsAudioLocal;

    //Initialize the MediaRecorder for Reflections Recording
    MediaRecorder mMediaRecorder;
    private Boolean isResponding = false;
    private boolean isResponseExists;

    public View progressBar;
    private float controlButtonVisibleTranslationY;
    private MediaPlayerSingleton mediaPlayerSingleton;
    private boolean isRecording = false;
    int count = 0;


    public ReflectionFragment() {
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

    public interface ReflectionFragmentListener {
        boolean isReflectionExists(int contentId);
        String getReflectionUrl(int contentId);
        void onRecordButtonPressed(int contentId, String url);
        void onPlayButtonPressed(int contentId);
    }

    /**
     * Initialization should be done here
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mediaPlayerSingleton = MediaPlayerSingleton.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.pageId = getArguments().getInt(StoryContentAdapter.KEY_ID);
        this.view = inflater.inflate(R.layout.fragment_reflection_view, container, false);
        this.buttonRespond = (Button) view.findViewById(R.id.buttonRespond);
        this.buttonNext = (Button) view.findViewById(R.id.buttonNext);
        this.buttonReplay = (Button) view.findViewById(R.id.buttonReplay);
        this.progressBar = view.findViewById(R.id.reflectionProgressBar);
        controlButtonVisibleTranslationY = buttonNext.getTranslationY();

        /**Get the text to display from bundle and show it as view*/
        String text = getArguments().getString(StoryContentAdapter.KEY_TEXT);
        String subtext = getArguments().getString(StoryContentAdapter.KEY_SUBTEXT);

        setContentText(view, text, subtext);

        //Write the audioFile in the cache
        try

        {
            /** Write to Internal storage
             * Removed Permission for External Storage from Manifest
             * */
            reflectionsAudioLocal = getActivity().getCacheDir().getAbsolutePath();
        } catch (Exception e) {
            Log.e("FILE_MANAGER", e.getMessage());
        }

        Storywell storywell = new Storywell(getActivity());
        reflectionsAudioLocal += "/" + storywell.getGroup().getName();

        /**
         * Play the recently recorded Audio
         * */
        buttonReplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /**Get a String*/
                String audioForPlayback;

                /**If we have both files: Local and Firebase File*/
                if (reflectionsUrlHashMap.get(pageId) != null) {
                    /**If there is local file, play that
                     * USE CASE: Even though there is a firebase file
                     * and everything is available for Streaming
                     * If the user records a new Reflection -
                     * that should be played rather than the old Firebase Audio*/
                    audioForPlayback = (reflectionFragmentListener.isReflectionExists(pageId))
                            ? reflectionFragmentListener.getReflectionUrl(pageId)
                            : reflectionsUrlHashMap.get(pageId);

                    /**If we don't have Firebase Audio*/
                } else {
                    /**This means either user has recorded and not uploaded OR maybe not recorded at all*/
                    if (reflectionFragmentListener.isReflectionExists(pageId)) {
                        /**No Firebase Recording. User has recorded a local audio*/
                        audioForPlayback = reflectionFragmentListener.getReflectionUrl(pageId);
                    } else {
                        /**
                         * No recording is ever recorded.
                         * But in that case, button won't be visible
                         * */
                        audioForPlayback = reflectionsAudioLocal;
                    }
                }

                /**
                 * Update the local state with the recording
                 * Because this will only be called */
                // TODO commented the line below because its unclear what it does
                //reflectionFragmentListener.onRecordButtonPressed(pageId, audioForPlayback);
                /**Send the Audio for playback*/
                mediaPlayerSingleton.onPlayback(mediaPlayerSingleton.getPlayingState(), audioForPlayback);
            }
        });

        /**
         *   Button to record Audio
         */
        buttonRespond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /**
                 * Make it true if user records something new again.
                 * uploadToFirebase controls Uploading of file to Firebase
                 * uploadToFirebase is set to true when respond button is pressed => User recorded something new
                 * uploadToFirebase is set to false in AsyncTask onPostExecute
                 * */
                uploadToFirebase = true;


                /**
                 * Change state of buttons ==> Animations and visibility
                 * */
                onRespondButtonPressed(getActivity(), view);

                /**Just a naming Convention*/
                if (count < 1) {
                    reflectionsAudioLocal += "_" + pageId + ".3gp";
                    count++;
                }

                /**
                 * Stop the Audio in case it is already playing
                 * and someone presses the record Audio button
                 * */
                if (mediaPlayerSingleton.getPlayingState()) {
                    mediaPlayerSingleton.onPlayback(mediaPlayerSingleton.getPlayingState(),
                            reflectionsAudioLocal);
                }

                /**
                 * Finally Record
                 * */
                onRecord(!isRecording);
            }
        });

        /**
         * Go to Next Fragment
         * */
        buttonNext.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View v) {
                onGoToFragmentCallback.onGoToFragment(OnGoToFragmentListener.TransitionType.ZOOM_OUT, 1);
            }
        });

        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            onGoToFragmentCallback = (OnGoToFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(((Activity) context).getLocalClassName()
                    + " must implement OnRecordButtonListener");
        }

        try {
            reflectionFragmentListener = (ReflectionFragmentListener) context;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ClassCastException(((Activity) context).getLocalClassName()
                    + " must implement ReflectionFragmentListener");
        }

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            this.isResponseExists = savedInstanceState.getBoolean(StoryContentAdapter.KEY_IS_RESPONSE_EXIST, DEFAULT_IS_RESPONSE_STATE);
        } else {
            //this.isResponseExists = getArguments().getBoolean(StoryContentAdapter.KEY_IS_RESPONSE_EXIST);
            this.isResponseExists = reflectionFragmentListener.isReflectionExists(pageId);
        }

        /**Change visibility of buttons if recordings are already present*/
        changeButtonsVisibility(pageId);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean(StoryContentAdapter.KEY_IS_RESPONSE_EXIST, isResponseExists);
    }


    @Override
    public void onPause() {
        super.onPause();
        /**If Recording if not stopped and someone minimizes the app, stop the recording*/
        if (isRecording) {
            buttonRespond.performClick();
        }

        SharedPreferences saveStateStoryPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor saveStateStory = saveStateStoryPref.edit();
        //TODO: Remove these states from here (Giving inconsistent results)
        //Do it in StoryViewActivity
//        saveStateStory.putInt("PAGE ID", pageId);
//        saveStateStory.putString("REFLECTION URL", getStoryCallback.getStoryState().getState().getRecordingURL(pageId));
        saveStateStory.apply();
    }


    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        //TODO: Remove this from here (Giving inconsistent results)
        //Do it in StoryViewActivity
//        this.story.getState().addReflection(pref.getInt("PAGE ID", pageId), pref.getString("REFLECTION URL", " "));
    }

    /*****************************************************************
     * METHODS TO RECORD AUDIO
     *****************************************************************/

    public void onRecord(boolean start) {
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
        mMediaRecorder.setOutputFile(reflectionsAudioLocal);
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
            try {
                mMediaRecorder.stop();
                mMediaRecorder.release();
                /**
                 * Change the state of story after we have the first audio
                 * */
                reflectionFragmentListener.onRecordButtonPressed(pageId, reflectionsAudioLocal.toString());
            } catch (Exception e) {
                Log.e("STOP_PRESSED_MANY TIMES", e.getMessage());
            }
            isRecording = false;
            isResponseExists = true;
        } else {
            Log.i("stopRecording", "mMediaRec is NULL");
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


    /***************************************************************
     * METHODS TO ANIMATE BUTTON
     ***************************************************************/

    public void onRespondButtonPressed(Context context, View view) {
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

    public class FadeSwitchListener extends AnimatorListenerAdapter {
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

    /***************************************************************************
     *If Recordings are available in either state or either in Firebase
     * Then make the buttons visible
     ***************************************************************************/

    private void changeButtonsVisibility(int currentPageId) {
        if (isResponseExists) {
            fadeControlButtonsTo(view, 1);
        }
        /*
        if ((reflectionsUrlHashMap.get(5) != null && pageId == 5)
                || ((reflectionsUrlHashMap.get(6) != null && pageId == 6))
                ) {
            //Change visibility of buttons
            isResponding = true;
            onRespondButtonPressed(getActivity(), view);
        }


        if(story.getState() != null){
            if(story.getState().getRecordingURL(currentPageId) != null){
                //Change visibility of buttons
                isResponding = true;
                onRespondButtonPressed(getActivity(), view);
            }
        }
        */
    }


}//End of Fragment