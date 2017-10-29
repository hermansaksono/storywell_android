package edu.neu.ccs.wellness.storytelling;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.neu.ccs.wellness.server.RestServer;
import edu.neu.ccs.wellness.server.WellnessRestServer;
import edu.neu.ccs.wellness.server.WellnessUser;
import edu.neu.ccs.wellness.storytelling.interfaces.StoryContent;
import edu.neu.ccs.wellness.storytelling.interfaces.StoryInterface;
import edu.neu.ccs.wellness.storytelling.models.Story;
import edu.neu.ccs.wellness.storytelling.storyview.StoryContentAdapter;
import edu.neu.ccs.wellness.utils.CardStackPageTransformer;
import edu.neu.ccs.wellness.utils.OnGoToFragmentListener;
import edu.neu.ccs.wellness.utils.UploadAudioAsyncTask;

import static edu.neu.ccs.wellness.StreamReflectionsFirebase.reflectionsUrlHashMap;
import static edu.neu.ccs.wellness.storytelling.storyview.ReflectionFragment.REFLECTION_AUDIO_LOCAL;
import static edu.neu.ccs.wellness.storytelling.storyview.ReflectionFragment.buttonNext;
import static edu.neu.ccs.wellness.storytelling.storyview.ReflectionFragment.buttonReplay;
import static edu.neu.ccs.wellness.storytelling.storyview.ReflectionFragment.buttonRespond;
import static edu.neu.ccs.wellness.storytelling.storyview.ReflectionFragment.isPlayingNow;
import static edu.neu.ccs.wellness.storytelling.storyview.ReflectionFragment.isRecording;
import static edu.neu.ccs.wellness.storytelling.storyview.ReflectionFragment.isRecordingInitiated;
import static edu.neu.ccs.wellness.storytelling.storyview.ReflectionFragment.playButtonPressed;
import static edu.neu.ccs.wellness.storytelling.storyview.ReflectionFragment.shouldRecord;

public class StoryViewActivity extends AppCompatActivity implements OnGoToFragmentListener {
    // CONSTANTS
    public static final String STORY_TEXT_FACE = "fonts/pangolin_regular.ttf";
    public static final float PAGE_MIN_SCALE = 0.75f;
    private WellnessUser user;
    private WellnessRestServer server;
    private StoryInterface story;
    public static boolean visitedSevenOnce = false;
    public static boolean phase2 = false;

    private Boolean isResponding = false;
    public static float controlButtonVisibleTranslationY;
    public static final int CONTROL_BUTTON_OFFSET = 10;
    public static View progressBar;
    //Initialize the MediaRecorder for Reflections Recording
    MediaRecorder mMediaRecorder;
    public static OnGoToFragmentListener mOnGoToFragmentListener;


    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private StoryContentPagerAdapter mSectionsPagerAdapter;
    private CardStackPageTransformer cardStackTransformer;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    @SuppressLint("StaticFieldLeak")
    public static ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_content_view);

        WellnessRestServer.configureDefaultImageLoader(getApplicationContext());
        this.loadStory();
    }


    @Override
    public void onStart() {
        super.onStart();
        showNavigationInstruction();
    }

    @Override
    public void onGoToFragment(TransitionType transitionType, int direction) {
        mViewPager.setCurrentItem(mViewPager.getCurrentItem() + direction);
    }

    // PRIVATE METHODS

    /***
     * Initializes the User, Server, and Story object. Then initiate an
     * AsyncTask object that loads the Story's definition from the internal
     * storage. If no saved definition in the internal storage, then make an
     * HTTP call to download the definition.
     */
    private void loadStory() {
        this.user = new WellnessUser(Storywell.DEFAULT_USER, Storywell.DEFAULT_PASS);
        this.server = new WellnessRestServer(Storywell.SERVER_URL, 0, Storywell.API_PATH, user);
        this.story = Story.create(getIntent().getExtras());

        new AsyncLoadStoryDef().execute();
    }

    /**
     * Initialize the pages in the storybook. A page is a fragment that mirrors
     * the list of StoryContent objects in the Story objects.
     * INVARIANT: The Story's stories variable has been initialized
     */
    private void InitStoryContentFragments() {
        mSectionsPagerAdapter = new StoryContentPagerAdapter(getSupportFragmentManager());

        // Set up the transitions
        cardStackTransformer = new CardStackPageTransformer(PAGE_MIN_SCALE);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setPageTransformer(true, cardStackTransformer);

        /**
         * Detect a right swipe for reflections page
         * */
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {

                // If position is Reflections Page and Audio is null
                // Don't Change the page
                switch (position) {

                    case 5:

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
                                onRespondButtonPressed(StoryViewActivity.this, findViewById(android.R.id.content));

                                //Stop the Audio
                                if (isPlayingNow) {
                                    MediaPlayerSingleton.getInstance().onPlayback(isPlayingNow, REFLECTION_AUDIO_LOCAL);
                                }
                                onRecord(!isRecording);
                            }
                        });

                        buttonReplay.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                if (isPlayingNow) {
                                    //Change text on Button if it is currently playing to "STOP"
                                    buttonReplay.setText(getResources().getText(R.string.reflection_button_replay_stop));
                                } else {
                                    //Change text on Button if it is currently not playing to "REPLAY"
                                    buttonReplay.setText(getResources().getText(R.string.reflection_button_replay));
                                }

                                try {
                                    MediaPlayerSingleton mediaPlayerSingleton = MediaPlayerSingleton.getInstance();
                                    FileInputStream fis = new FileInputStream(REFLECTION_AUDIO_LOCAL);
                                    if (String.valueOf(fis.read()).length() > 0) {
                                        mediaPlayerSingleton.onPlayback(isPlayingNow, REFLECTION_AUDIO_LOCAL);
                                        buttonReplay.setAlpha(1);
                                        buttonReplay.setVisibility(View.VISIBLE);
                                        buttonNext.setAlpha(1);
                                        buttonNext.setVisibility(View.VISIBLE);
                                    } else {
                                        mediaPlayerSingleton.onPlayback(isPlayingNow, reflectionsUrlHashMap.get(6));
                                    }
                                } catch (Exception playbackFromScrollChange) {
                                    Log.e("playbackFromScroll_5", playbackFromScrollChange.getMessage());
                                } finally {
                                    playButtonPressed = false;
                                }
                            }//End of onCLick
                        });

                        break;

                    case 6:
                        buttonReplay.setAlpha(0);
                        buttonReplay.setVisibility(View.INVISIBLE);
                        buttonNext.setAlpha(0);
                        buttonNext.setVisibility(View.INVISIBLE);
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
                                onRespondButtonPressed(StoryViewActivity.this, findViewById(android.R.id.content));

                                //Stop the Audio
                                if (isPlayingNow) {
                                    MediaPlayerSingleton.getInstance().onPlayback(isPlayingNow, REFLECTION_AUDIO_LOCAL);
                                }
                                onRecord(!isRecording);
                            }
                        });

                        buttonReplay.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                if (isPlayingNow) {
                                    //Change text on Button if it is currently playing to "STOP"
                                    buttonReplay.setText(getResources().getText(R.string.reflection_button_replay_stop));
                                } else {
                                    //Change text on Button if it is currently not playing to "REPLAY"
                                    buttonReplay.setText(getResources().getText(R.string.reflection_button_replay));
                                }

                                try {
                                    FileInputStream fis = new FileInputStream(REFLECTION_AUDIO_LOCAL);
                                    MediaPlayerSingleton mediaPlayerSingleton = MediaPlayerSingleton.getInstance();
                                    if (String.valueOf(fis.read()).length() > 0) {
                                        mediaPlayerSingleton.onPlayback(isPlayingNow, REFLECTION_AUDIO_LOCAL);
                                    } else {
                                        mediaPlayerSingleton.onPlayback(isPlayingNow, reflectionsUrlHashMap.get(7));
                                    }
                                } catch (Exception playbackFromScrollChange) {
                                    Log.e("playbackFromScroll_6", playbackFromScrollChange.getMessage());
                                } finally {
                                    playButtonPressed = false;
                                }
                            }
                        });

                        if (reflectionsUrlHashMap.get(7) != null) {

                        }


                        //If person tries to reach 6 and has not recorded audio
                        if (isRecordingInitiated == false) {

                            //If the person has not recorded even one reflection for one of the 2 reflection pages
                            //This will be false and person can't move forward, and will be pushed back to 5th
                            if (visitedSevenOnce == false) {
                                mViewPager.setCurrentItem(5);
                                Toast.makeText(getBaseContext(), "Please Record Audio first", Toast.LENGTH_SHORT).show();
                            }
                            // If the person has recorded for 1st reflection and has not reflected
                            // for 2nd one, this will be true
                            else if (visitedSevenOnce == true) {
                                //Thus the person will stay on 6th i.e. 1st reflection
                                mViewPager.setCurrentItem(6);
                            }

                            //If the person records the 1st reflection,
                            //isRecordingInitiated will get true the first time
                        } else if (isRecordingInitiated == true) {
                            mViewPager.setCurrentItem(6);

                            //isRecordingInitiated is set false for 2nd reflection
                            isRecordingInitiated = false;
                            //visitedSevenOnce is set true for 2nd reflection
                            visitedSevenOnce = true;
                        }
                        break;

                    case 7:
//                        Toast.makeText(getBaseContext(), "7th FRAGMENT", Toast.LENGTH_SHORT).show();
                        //This is set to true because when 7th throws user back to 6th,
                        // he/she does not go back to 5th as he/she has already recorded 1st reflection
                        // and reached 7th.
                        //If the person has not recorded 1st reflection, he/she won't be able to reach here
                        if (!phase2 && !isRecordingInitiated) {
                            mViewPager.setCurrentItem(6);
                            Toast.makeText(getBaseContext(), "Please Record Audio first", Toast.LENGTH_SHORT).show();
                        } else {
                            mViewPager.setCurrentItem(7);
                            phase2 = true;
                        }
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    /**
     * Show the navigation instruction on the screen
     */
    private void showNavigationInstruction() {
        //TODO: Replace with a SnackBar with ability to Swipe and end it
        String navigationInfo = getString(R.string.tooltip_storycontent_navigation);
        Toast toast = Toast.makeText(getApplicationContext(), navigationInfo, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, 0);
        toast.show();
    }

    // INTERNAL CLASSES

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class StoryContentPagerAdapter extends FragmentPagerAdapter {

        private List<Fragment> fragments = new ArrayList<Fragment>();

        /**
         * Convert the StoryContents from Story to Fragments
         * TODO: REPLACE THIS FROM HERE
         */
        public StoryContentPagerAdapter(FragmentManager fm) {
            super(fm);
            for (StoryContent content : story.getContents()) {
                this.fragments.add(StoryContentAdapter.getFragment(content));
            }
        }


        @Override
        public Fragment getItem(int position) {
//            Toast.makeText(StoryViewActivity.this,
//                    String.valueOf(mViewPager.getCurrentItem()), Toast.LENGTH_SHORT).show();
            return this.fragments.get(position);
        }

        @Override
        public int getCount() {
            return this.fragments.size();
        }
    }

    // ASYNCTASK CLASSES
    private class AsyncLoadStoryDef extends AsyncTask<Void, Integer, RestServer.ResponseType> {
        protected RestServer.ResponseType doInBackground(Void... nothingburger) {
            RestServer.ResponseType result = null;
            if (server.isOnline(getApplicationContext())) {
                story.loadStoryDef(getApplicationContext(), server);
                result = RestServer.ResponseType.SUCCESS_202;
            } else {
                result = RestServer.ResponseType.NO_INTERNET;
            }
            return result;
        }

        protected void onPostExecute(RestServer.ResponseType result) {
            Log.d("WELL Story download", result.toString());
            if (result == RestServer.ResponseType.NO_INTERNET) {
                showErrorMessage(getString(R.string.error_no_internet));
            } else if (result == RestServer.ResponseType.SUCCESS_202) {
                InitStoryContentFragments();
            }
        }
    }

    // PRIVATE HELPER METHODS
    private void showErrorMessage(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
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
                try {
                    mMediaRecorder.stop();
                    mMediaRecorder.reset();
                } catch (Exception startRecStopException) {
                    Log.e("startRecStopException", startRecStopException.getMessage());
                    startRecStopException.printStackTrace();
                }
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
                // If the play/stop is pressed multiple times
                // It leads to crash
                mMediaRecorder.stop();
                mMediaRecorder.release();
                isRecording = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.i("stopRecording", "mMediaRec is NULL");
        }
    }


    /***************************************************************************
     * UPLOAD TO DATABASE
     ***************************************************************************/

    private void uploadAudioToFirebase() {
        UploadAudioAsyncTask uploadAudio = new UploadAudioAsyncTask(this);
        uploadAudio.execute();
    }


}