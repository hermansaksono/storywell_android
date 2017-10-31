package edu.neu.ccs.wellness.storytelling.storyview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import edu.neu.ccs.wellness.storytelling.R;
import edu.neu.ccs.wellness.storytelling.StoryViewActivity;
import edu.neu.ccs.wellness.storytelling.models.StoryReflection;

import static edu.neu.ccs.wellness.storytelling.StoryViewActivity.controlButtonVisibleTranslationY;
import static edu.neu.ccs.wellness.storytelling.StoryViewActivity.progressBar;

/**
 * Recording and Playback of Audio
 * For reference use Android Docs
 * https://developer.android.com/guide/topics/media/mediarecorder.html
 */
public class ReflectionFragment extends Fragment {

    private static final String KEY_TEXT = "KEY_TEXT";
    private static final int CONTROL_BUTTON_OFFSET = 10;

    private View view;
    private OnPlayButtonListener playButtonCallback;

    private int pageId;
    private Boolean isResponding = false;

    private Button buttonReplay;
    private Button buttonRespond;
    private Button buttonNext;

    public static boolean isPermissionGranted = false;
    public static boolean isRecording = false;
    public static boolean isPlayingNow = false;
    //Audio File Name
    public static String REFLECTION_AUDIO_LOCAL;
    // A boolean variable which checks if user has already recorded something
    // and controls uploading file to Firebase
    public static boolean shouldRecord;

    public static String downloadUrl;
    //A boolean to control movement of user based on if he/she has recorded a reflection or not
    public static boolean isRecordingInitiated = false;
    //A string for the path of file downloaded from Firebase
    String REFLECTION_AUDIO_FIREBASE = "";

    public static boolean playButtonPressed = false;


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
        this.pageId = getArguments().getInt(StoryContentAdapter.KEY_ID);
        this.view = inflater.inflate(R.layout.fragment_reflection_view, container, false);
        buttonRespond = (Button) view.findViewById(R.id.buttonRespond);
        buttonNext = (Button) view.findViewById(R.id.buttonNext);
        buttonReplay = (Button) view.findViewById(R.id.buttonReplay);
        progressBar = view.findViewById(R.id.reflectionProgressBar);
        controlButtonVisibleTranslationY = buttonNext.getTranslationY();
//        setFirebaseAsPlaybackSource();

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

        buttonReplay.setVisibility(View.VISIBLE);
        buttonReplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playButtonCallback.onPlayButtonPressed(pageId);
            }
        });

        buttonRespond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRespondButtonPressed(getActivity(), view);
            }
        });



        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            //mOnGoToFragmentListener = (OnGoToFragmentListener) context;
            playButtonCallback = (OnPlayButtonListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(((Activity) context).getLocalClassName()
                    + " must implement OnPlayButtonListener");
        }
    }

    public interface OnPlayButtonListener {
        public void onPlayButtonPressed(int contentId);
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













    /**
     * GET THE LINKS TO STREAM FROM FIREBASE
     */
    /*
    private void setFirebaseAsPlaybackSource() {

        int firstFragment = 6;
        int secondFragment = 7;

        //NO AUDIO IS RECORDED FOR 1st FRAGMENT
        //That means nothing is recorded
        if (reflectionsUrlHashMap.get(firstFragment) == null) {
            //DO NOTHING
            return;
        }

        //Audio is recorded for both fragments
        if (reflectionsUrlHashMap.get(firstFragment) != null
                && reflectionsUrlHashMap.get(secondFragment) != null) {


//            isResponding = true;
//            onRespondButtonPressed(getContext(), view);
            REFLECTION_AUDIO_FIREBASE = reflectionsUrlHashMap.get(secondFragment);
        }

        //Allow navigation between all fragments
        isRecordingInitiated = true;
        visitedSevenOnce = true;
        phase2 = true;

        //Change state of all buttons to be visible
        buttonRespond.setText(getResources().getText(R.string.reflection_button_answer_again));
        buttonRespond.setVisibility(View.VISIBLE);

        buttonNext.setText(getResources().getText(R.string.reflection_button_next));
        buttonNext.setVisibility(View.VISIBLE);

        buttonReplay.setText(getResources().getText(R.string.reflection_button_replay));
        buttonReplay.setVisibility(View.VISIBLE);

    }//End of setFirebaseAsPlaybackSource
    */

    /***************************************************************
     * METHODS TO ANIMATE BUTTON
     ***************************************************************/



}//End of Fragment