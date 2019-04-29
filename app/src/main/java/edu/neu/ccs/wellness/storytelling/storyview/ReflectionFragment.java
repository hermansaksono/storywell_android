package edu.neu.ccs.wellness.storytelling.storyview;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ViewFlipper;

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
    private ViewFlipper viewFlipper;
    private ViewFlipper reflectionControlViewFlipper;
    private View reflectionView;
    private OnGoToFragmentListener onGoToFragmentCallback;
    private ReflectionFragmentListener reflectionFragmentListener;

    private int pageId;
    private String contentGroupId;
    private String contentGroupName;
    private boolean isShowReflectionStart = false;

    private ImageButton buttonReplay;
    private TextView textViewReplay;
    private ImageButton buttonRespond;
    private TextView textViewRespond;
    private Button buttonBack;
    private Button buttonNext;
    private View buttonStartReflection;

    private Drawable playDrawable;
    private Drawable stopDrawable;



    /**
     * Ask for Audio Permissions
     */
    private static final int REQUEST_AUDIO_PERMISSIONS = 100;
    private String[] permission = {android.Manifest.permission.RECORD_AUDIO};

    /**
     * Audio File Name
     * Made Static as it will be used in uploading to Firebase
     */

    //Initialize the MediaRecorder for Reflections Recording
    private Boolean isResponding = false;
    private boolean isResponseExists;

    public View recordingProgressBar;
    public View playbackProgressBar;
    private float controlButtonVisibleTranslationY;
    private boolean isRecording;

    private Boolean isPlayingRecording = false;
    private boolean isAllowEdit;

    private String dateString = null;


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
        void doStartRecording(int contentId, String contentGroupId, String contentGroupName);
        void doStopRecording();
        //void doPlayOrStopRecording(int contentId);
        void doStartPlay(int contentId, OnCompletionListener completionListener);
        void doStopPlay();
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
        this.contentGroupId = getArguments().getString(StoryContentAdapter.KEY_CONTENT_GROUP);
        this.contentGroupName = getArguments().getString(StoryContentAdapter.KEY_CONTENT_GROUP_NAME);
        this.isAllowEdit = getArguments().getBoolean(StoryContentAdapter.KEY_CONTENT_ALLOW_EDIT,
                StoryContentAdapter.DEFAULT_CONTENT_ALLOW_EDIT);
        this.isShowReflectionStart = isShowReflectionStart(getArguments());
        this.view = getView(inflater, container, this.isShowReflectionStart);
        this.viewFlipper = getViewFlipper(this.view, this.isShowReflectionStart);

        this.playDrawable = getResources().getDrawable(R.drawable.ic_round_play_arrow_big);
        this.stopDrawable = getResources().getDrawable(R.drawable.ic_round_stop_big);

        this.reflectionControlViewFlipper = getReflectionControl(this.view);
        this.buttonStartReflection = view.findViewById(R.id.buttonReflectionStart);
        this.buttonRespond = view.findViewById(R.id.buttonRespond);
        this.buttonBack = view.findViewById(R.id.buttonBack);
        this.buttonNext = view.findViewById(R.id.buttonNext);
        this.buttonReplay = view.findViewById(R.id.buttonPlay);
        this.textViewRespond = view.findViewById(R.id.textRespond);
        this.textViewReplay = view.findViewById(R.id.textPlay);
        this.recordingProgressBar = view.findViewById(R.id.reflectionProgressBar);
        this.playbackProgressBar = view.findViewById(R.id.playbackProgressBar);
        this.controlButtonVisibleTranslationY = buttonNext.getTranslationY();

        if (getArguments().containsKey(StoryContentAdapter.KEY_REFLECTION_DATE)) {
            TextView dateTextView = view.findViewById(R.id.reflectionDate);
            dateTextView.setVisibility(View.VISIBLE);
            dateTextView.setText(getArguments().getString(StoryContentAdapter.KEY_REFLECTION_DATE));
            view.findViewById(R.id.reflectionInstruction).setVisibility(View.GONE);
        }

        /**Get the text to display from bundle and show it as view*/
        String text = getArguments().getString(StoryContentAdapter.KEY_TEXT);
        String subtext = getArguments().getString(StoryContentAdapter.KEY_SUBTEXT);
        setContentText(view, text, subtext);

        /* Animation for reflection start button */
        buttonStartReflection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewFlipper.showNext();
            }
        });

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
        
        /*
        buttonRespond.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                onRespondButtonPressed(getActivity(), view);
                return true;
            }
        });
        */

        buttonNext.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View v) {
                onGoToFragmentCallback.onGoToFragment(OnGoToFragmentListener.TransitionType.ZOOM_OUT, 1);
            }
        });

        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonBackPressed(getContext());
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
                    + " must implement OnGoToFragmentListener");
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


        changeButtonsVisibility(this.isResponseExists, this.view);
        changeReflectionStartVisibility(this.isResponseExists, this.viewFlipper);
        changeReflectionEditButtonVisibility(this.isAllowEdit, this.buttonBack);
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


        TextView refStartText = view.findViewById(R.id.refl_start_text);
        TextView refStartSubtext = view.findViewById(R.id.refl_start_subtext);

        refStartText.setTypeface(tf);
        refStartSubtext.setTypeface(tf);

    }

    private void changeReflectionStartVisibility(boolean isResponseExists,
                                                        ViewFlipper viewFlipper) {
        if (isResponseExists && isShowReflectionStart) {
            viewFlipper.showNext();
        }
    }

    private static void changeReflectionEditButtonVisibility(boolean isAllowEdit, View view) {
        if (!isAllowEdit) {
            view.setVisibility(View.INVISIBLE);
        }
    }


    /***************************************************************
     * METHODS TO ANIMATE BUTTONS
     ***************************************************************/

    public void onReplayButtonPressed() {
        if (isPlayingRecording == false) {
            this.startPlayingResponse();
        } else {
            this.stopPlayingResponse();
        }
    }

    private void startPlayingResponse() {
        OnCompletionListener onCompletionListener = new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                stopPlayingResponse();
            }
        };

        if (isPlayingRecording == false) {
            this.fadePlaybackProgressBarTo(1, R.integer.anim_short);
            this.reflectionFragmentListener.doStartPlay(pageId, onCompletionListener);
            //this.buttonReplay.setText(R.string.reflection_button_replay_stop);
            this.textViewReplay.setText(R.string.reflection_label_playing);
            this.buttonReplay.setImageDrawable(stopDrawable);
            this.isPlayingRecording = true;
        }
    }

    private void stopPlayingResponse() {
        if (isPlayingRecording == true && this.getActivity() != null ) {
            this.fadePlaybackProgressBarTo(0, R.integer.anim_short);
            this.reflectionFragmentListener.doStopPlay();
            //this.buttonReplay.setText(R.string.reflection_button_replay);
            this.textViewReplay.setText(R.string.reflection_label_play);
            this.buttonReplay.setImageDrawable(playDrawable);
            this.isPlayingRecording = false;
        }
    }

    public void onRespondButtonPressed(Context context, View view) {
        if (isRecordingAllowed() == false) {
            requestPermissions(permission, REQUEST_AUDIO_PERMISSIONS);
            return;
        }
        if (isResponding) {
            this.stopResponding();
        } else {
            this.startResponding();
        }
    }

    private void startResponding() {
        this.isResponding = true;
        this.fadeRecordingProgressBarTo(1, R.integer.anim_short);
        //this.fadeControlButtonsTo(view, 0);
        //this.changeReflectionButtonTextTo(getString(R.string.reflection_button_stop));
        this.textViewRespond.setText(getString(R.string.reflection_label_record));

        this.reflectionFragmentListener.doStartRecording(this.pageId,
                this.contentGroupId, this.contentGroupName);
    }

    private void stopResponding() {
        this.reflectionFragmentListener.doStopRecording();

        this.isResponding = false;
        this.fadeRecordingProgressBarTo(0, R.integer.anim_fast);
        //this.changeReflectionButtonTextTo(getString(R.string.reflection_button_answer));
        //this.fadeControlButtonsTo(view, 1);
        this.textViewRespond.setText(getString(R.string.reflection_label_answer));
        this.doGoToPlaybackControl();
    }

    private void doGoToPlaybackControl() {
        this.reflectionControlViewFlipper.setInAnimation(getContext(), R.anim.view_move_left_next);
        this.reflectionControlViewFlipper.setOutAnimation(getContext(), R.anim.view_move_left_current);
        this.reflectionControlViewFlipper.showNext();
    }

    private void onButtonBackPressed(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle(R.string.reflection_delete_confirmation_title);
        builder.setMessage(R.string.reflection_delete_confirmation_desc);
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                doGoToRecordingControl();
            }
        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void doGoToRecordingControl() {
        this.reflectionFragmentListener.doStopPlay();
        this.reflectionControlViewFlipper.setInAnimation(getContext(), R.anim.view_move_right_prev);
        this.reflectionControlViewFlipper.setOutAnimation(getContext(), R.anim.view_move_right_current);
        this.reflectionControlViewFlipper.showPrevious();
    }

    private void changeReflectionButtonTextTo(String text) {

        //buttonRespond.setText(text);
    }

    private void fadeRecordingProgressBarTo(float alpha, int animLengthResId) {
        recordingProgressBar.animate()
                .alpha(alpha)
                .setDuration(getResources().getInteger(animLengthResId))
                .setListener(null);
    }

    private void fadePlaybackProgressBarTo(float alpha, int animLengthResId) {
        playbackProgressBar.animate()
                .alpha(alpha)
                .setDuration(getResources().getInteger(animLengthResId))
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
     * If Recordings are available in either state or either in Firebase
     * Then make the buttons visible
     ***************************************************************************/

    private void changeButtonsVisibility(boolean isResponseExists, View view) {
        if (isResponseExists) {
            //fadeControlButtonsTo(view, 1);
            reflectionControlViewFlipper.showNext();
        }
    }

    private static View getView(LayoutInflater inflater, ViewGroup container,
                                boolean isShowReflectionStart) {
        return inflater.inflate(R.layout.fragment_reflection_root_view, container, false);
    }

    private static ViewFlipper getViewFlipper(View view, boolean isShowReflectionStart) {
        if (isShowReflectionStart) {
            ViewFlipper viewFlipper = view.findViewById(R.id.view_flipper);
            viewFlipper.setInAnimation(view.getContext(), R.anim.reflection_fade_in);
            viewFlipper.setOutAnimation(view.getContext(), R.anim.reflection_fade_out);
            return viewFlipper;
        } else {
            ViewFlipper viewFlipper = view.findViewById(R.id.view_flipper);
            viewFlipper.showNext();
            return viewFlipper;
        }
    }

    private static ViewFlipper getReflectionControl(View view) {
        ViewFlipper viewFlipper = view.findViewById(R.id.view_flipper_reflection_control);
        viewFlipper.setInAnimation(view.getContext(), R.anim.reflection_fade_in);
        viewFlipper.setOutAnimation(view.getContext(), R.anim.reflection_fade_out);
        return viewFlipper;
    }

    private static boolean isShowReflectionStart(Bundle arguments) {
        return arguments.getBoolean(StoryContentAdapter.KEY_IS_SHOW_REF_START,
                StoryReflection.DEFAULT_IS_REF_START);
    }

    private boolean isRecordingAllowed() {
        int permissionRecordAudio = ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.RECORD_AUDIO);
        return permissionRecordAudio == PackageManager.PERMISSION_GRANTED;
    }


}//End of Fragment