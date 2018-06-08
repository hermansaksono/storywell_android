package edu.neu.ccs.wellness.storytelling;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import edu.neu.ccs.wellness.server.RestServer;
import edu.neu.ccs.wellness.server.WellnessRestServer;
import edu.neu.ccs.wellness.story.Story;
import edu.neu.ccs.wellness.story.interfaces.StoryContent;
import edu.neu.ccs.wellness.story.interfaces.StoryInterface;
import edu.neu.ccs.wellness.storytelling.storyview.ReflectionFragment;
import edu.neu.ccs.wellness.storytelling.utils.OnGoToFragmentListener;
import edu.neu.ccs.wellness.reflection.ReflectionManager;
import edu.neu.ccs.wellness.storytelling.utils.StoryContentAdapter;
import edu.neu.ccs.wellness.utils.CardStackPageTransformer;

public class StoryViewActivity extends AppCompatActivity
        implements OnGoToFragmentListener, ReflectionFragment.ReflectionFragmentListener {

    // CONSTANTS
    public static final int STORY_TITLE_FACE = R.font.montserrat_bold;
    public static final String STORY_TEXT_FACE = "fonts/pangolin_regular.ttf";
    public static final float PAGE_MIN_SCALE = 0.75f;

    private Storywell storywell;
    private StoryInterface story;
    private ReflectionManager reflectionManager;
    private String storyId;

    private CardStackPageTransformer cardStackTransformer;

    private SharedPreferences savePositionPreference;
    private int currentPagePosition = 0;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    @SuppressLint("StaticFieldLeak")
    public static ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_storyview);
        WellnessRestServer.configureDefaultImageLoader(getApplicationContext());
        this.storyId = getIntent().getStringExtra(Story.KEY_STORY_ID);
        this.storywell = new Storywell(getApplicationContext());
        this.reflectionManager = new ReflectionManager(this.storywell.getGroup().getName(), this.storyId);
        this.loadStory();
    }


    @Override
    public void onStart() {
        super.onStart();
        showNavigationInstruction();
    }


    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor putPositionInPref = savePositionPreference.edit();

        /**Save the position when paused*/
        putPositionInPref.putInt("lastPagePositionSharedPref", currentPagePosition);
        //TODO : Save the state of story
        putPositionInPref.apply();
        this.story.saveState(getApplicationContext(), storywell.getGroup());
    }

    @Override
    protected void onResume() {
        super.onResume();
        //tryGoToThisPage(story.getState().getCurrentPage(), mViewPager, story);
        savePositionPreference = PreferenceManager.getDefaultSharedPreferences(this);

        currentPagePosition = savePositionPreference.getInt("lastPagePositionSharedPref", 0);
        //TODO: RESTORE THE STORY STATE
    }

    @Override
    public void onGoToFragment(TransitionType transitionType, int direction) {
        mViewPager.setCurrentItem(mViewPager.getCurrentItem() + direction);
    }

    @Override
    public boolean isReflectionExists(int contentId) {
        //return story.getState().isReflectionResponded(contentId);
        return reflectionManager.isReflectionResponded(String.valueOf(contentId));
    }

    @Override
    public void doStartRecording(int contentId) {
        //story.getState().addReflection(contentId, urlRecording);
        //story.getState().save(getApplicationContext());
        if (reflectionManager.getIsPlayingStatus() == true) {
            this.reflectionManager.stopPlayback();
        }

        if (reflectionManager.getIsRecordingStatus() == false) {
            this.reflectionManager.startRecording(getApplicationContext(),
                    String.valueOf(contentId), new MediaRecorder());
        }
    }

    @Override
    public void doStopRecording() {
        if (reflectionManager.getIsRecordingStatus() == true) {
            this.reflectionManager.stopRecording();
        }
    }

    @Override
    public void doPlayOrStopRecording(int contentId) {
        if (this.reflectionManager.getIsPlayingStatus() == false) {
            playReflectionIfExists(contentId);
        } else {
            this.reflectionManager.stopPlayback();
        }
    }

    private void playReflectionIfExists(int contentId) {
        String reflectionUrl = this.reflectionManager.getRecordingURL(String.valueOf(contentId));
        if (reflectionUrl != null) {
            this.reflectionManager.startPlayback(reflectionUrl, new MediaPlayer());
        }
    }


    /***
     * Initializes the User, Server, and Story object. Then initiate an
     * AsyncTask object that loads the Story's definition from the internal
     * storage. If no saved definition in the internal storage, then make an
     * HTTP call to download the definition.
     */
    private void loadStory() {
        this.story = Story.create(getIntent().getExtras());
        new AsyncLoadStoryDef().execute();
    }

    private void loadReflectionUrls() {
        new AsyncDownloadReflectionUrls().execute();
    }

    /**
     * Show the navigation instruction on the screen
     */
    private void showNavigationInstruction() {
        String navigationInfo = getString(R.string.tooltip_storycontent_navigation);
        Toast toast = Toast.makeText(getApplicationContext(), navigationInfo, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, 10);
        toast.show();
    }

    private void showErrorMessage(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }


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
                boolean isResponseExists = story.getState().isReflectionResponded(content.getId());
                this.fragments.add(StoryContentAdapter.getFragment(content, isResponseExists));
            }
        }


        @Override
        public Fragment getItem(int position) {
            return this.fragments.get(position);
        }

        @Override
        public int getCount() {
            return this.fragments.size();
        }
    }


    /**
     * Initialize the pages in the storybook. A page is a fragment that mirrors
     * the list of StoryContent objects in the Story objects.
     * INVARIANT: The Story's stories variable has been initialized
     */
    private void initStoryContentFragments() {
        /**
         The {@link android.support.v4.view.PagerAdapter} that will provide
         fragments for each of the sections. We use a
         {@link FragmentPagerAdapter} derivative, which will keep every
         loaded fragment in memory. If this becomes too memory intensive, it
         may be best to switch to a
         {@link android.support.v4.app.FragmentStatePagerAdapter}.
         */
        StoryContentPagerAdapter mSectionsPagerAdapter = new StoryContentPagerAdapter(getSupportFragmentManager());

        // Set up the transitions
        cardStackTransformer = new CardStackPageTransformer(PAGE_MIN_SCALE);
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setPageTransformer(true, cardStackTransformer);

        tryGoToThisPage(story.getState().getCurrentPage(), mViewPager, story);
        /**
         * Detect a right swipe for reflections page
         * */
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int pos, float offset, int positionOffsetPixels) {
                if (offset > 0.95) {
                    reflectionManager.stopPlayback();
                }
            }

            @Override
            public void onPageSelected(int position) {
                tryGoToThisPage(position, mViewPager, story);
                tryUploadReflectionAudio();
                currentPagePosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }


    // PRIVATE HELPER METHODS
    private void tryGoToThisPage(int position, ViewPager viewPager, StoryInterface story) {
        int allowedPosition = getAllowedPageToGo(position);
        story.getState().setCurrentPage(allowedPosition);
        viewPager.setCurrentItem(allowedPosition);
    }

    private void tryUploadReflectionAudio() {
        if (this.reflectionManager.isUploadQueued())
            new AsyncUploadAudio().execute();;
    }

    private int getAllowedPageToGo(int goToPosition) {
        int preceedingPosition = goToPosition - 1;
        if (preceedingPosition < 0) {
            return goToPosition;
        } else {
            StoryContent precContent = story.getContentByIndex(preceedingPosition);
            if (canProceedToNextContent(precContent) == false) {
                return preceedingPosition;
            } else {
                return goToPosition;
            }
        }
    }

    /* STATIC METHODS */
    private boolean canProceedToNextContent(StoryContent precContent) {
        boolean isReflection = isReflection(precContent);
        boolean isReflectionExists = this.isReflectionExists(precContent.getId());
        if (isReflection == false) {
            return true;
        } else if (isReflectionExists == true) {
            return true;
        } else {
            return false;
        }
    }
    private static boolean isReflection(StoryContent content) {
        return content.getType().equals(StoryContent.ContentType.REFLECTION);
    }

    /* ASYNCTASK CLASSES */
    private class AsyncLoadStoryDef extends AsyncTask<Void, Integer, RestServer.ResponseType> {
        protected RestServer.ResponseType doInBackground(Void... nothingburger) {
            return story.tryLoadStoryDef(getApplicationContext(), storywell.getServer(),
                    storywell.getGroup());
        }

        protected void onPostExecute(RestServer.ResponseType result) {
            if (result == RestServer.ResponseType.NO_INTERNET) {
                showErrorMessage(getString(R.string.error_no_internet));
            } else if (result == RestServer.ResponseType.SUCCESS_202) {
                initStoryContentFragments();
                loadReflectionUrls();
            }
        }
    }

    public class AsyncDownloadReflectionUrls extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            reflectionManager.getReflectionUrlsFromFirebase();
            return null;
        }
    }

    public class AsyncUploadAudio extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            reflectionManager.uploadReflectionAudioToFirebase(story.getState());
            return null;
        }
    }
}//End of Activity