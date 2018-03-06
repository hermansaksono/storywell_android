package edu.neu.ccs.wellness.storytelling.storyview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


import edu.neu.ccs.wellness.storytelling.R;
import edu.neu.ccs.wellness.storytelling.StoryViewActivity;
import edu.neu.ccs.wellness.story.StoryReflection;
import edu.neu.ccs.wellness.storytelling.utils.OnGoToFragmentListener;
import edu.neu.ccs.wellness.storytelling.utils.StoryContentAdapter;

/**
 * Recording and Playback of Audio
 * For reference use Android Docs
 * https://developer.android.com/guide/topics/media/mediarecorder.html
 */
public class ReflectionFragment extends Fragment {


    /***************************************************************************
     * VARIABLE DECLARATION
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

    /**
     * Audio File Name
     * Made Static as it will be used in uploading to Firebase
     */

    //Initialize the MediaRecorder for Reflections Recording
    private Boolean isResponding = false;
    private boolean isResponseExists;

    public View progressBar;
    private float controlButtonVisibleTranslationY;
    private boolean isRecording = false;


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
        void doStartRecording(int contentId);
        void doStopRecording();
        void doPlayOrStopRecording(int contentId);
    }

    /**
     * Initialization should be done here
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.pageId = getArguments().getInt(StoryContentAdapter.KEY_ID);
        this.view = inflater.inflate(R.layout.fragment_reflection_view, container, false);
        this.buttonRespond = view.findViewById(R.id.buttonRespond);
        this.buttonNext = view.findViewById(R.id.buttonNext);
        this.buttonReplay = view.findViewById(R.id.buttonReplay);
        this.progressBar = view.findViewById(R.id.reflectionProgressBar);
        this.controlButtonVisibleTranslationY = buttonNext.getTranslationY();

        /**Get the text to display from bundle and show it as view*/
        String text = getArguments().getString(StoryContentAdapter.KEY_TEXT);
        String subtext = getArguments().getString(StoryContentAdapter.KEY_SUBTEXT);
        setContentText(view, text, subtext);


        /**
         * Play the recently recorded Audio
         * */
        buttonReplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onReplayButtonPressed();
            }
        });

        buttonRespond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRespondButtonPressed(getActivity(), view);
            }
        });

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
            this.isResponseExists = savedInstanceState.getBoolean(
                    StoryContentAdapter.KEY_IS_RESPONSE_EXIST, DEFAULT_IS_RESPONSE_STATE);
        } else {
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
        //SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        //TODO: Remove this from here (Giving inconsistent results)
        //Do it in StoryViewActivity
//        this.story.getState().addReflection(pref.getInt("PAGE ID", pageId), pref.getString("REFLECTION URL", " "));
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
        TextView itv = view.findViewById(R.id.reflectionInstruction);
        TextView tv = view.findViewById(R.id.reflectionText);
        TextView stv = view.findViewById(R.id.reflectionSubtext);

        itv.setTypeface(tf);

        tv.setTypeface(tf);
        tv.setText(text);

        stv.setTypeface(tf);
        stv.setText(subtext);
    }


    /***************************************************************
     * METHODS TO ANIMATE BUTTON
     ***************************************************************/

    public void onReplayButtonPressed() {
        this.reflectionFragmentListener.doPlayOrStopRecording(pageId);
    }

    public void onRespondButtonPressed(Context context, View view) {
        if (isResponding) {
            this.stopResponding();
        } else {
            this.startResponding();
        }
    }

    private void startResponding() {
        this.isResponding = true;
        this.fadeProgressBarTo(1, R.integer.anim_short);
        this.fadeControlButtonsTo(view, 0);
        this.changeReflectionButtonTextTo(getString(R.string.reflection_button_stop));

        this.reflectionFragmentListener.doStartRecording(this.pageId);
    }

    private void stopResponding() {
        this.reflectionFragmentListener.doStopRecording();

        this.isResponding = false;
        this.fadeProgressBarTo(0, R.integer.anim_fast);
        this.fadeControlButtonsTo(view, 1);
        this.changeReflectionButtonTextTo(getString(R.string.reflection_button_answer_again));
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