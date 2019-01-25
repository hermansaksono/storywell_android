package edu.neu.ccs.wellness.storytelling;

import android.content.SharedPreferences;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import edu.neu.ccs.wellness.logging.WellnessUserLogging;
import edu.neu.ccs.wellness.server.RestServer;
import edu.neu.ccs.wellness.server.WellnessRestServer;
import edu.neu.ccs.wellness.story.Story;
import edu.neu.ccs.wellness.story.StoryChallenge;
import edu.neu.ccs.wellness.story.interfaces.StoryContent;
import edu.neu.ccs.wellness.story.interfaces.StoryInterface;
import edu.neu.ccs.wellness.storytelling.storyview.ReflectionFragment;
import edu.neu.ccs.wellness.storytelling.storyview.StoryViewPresenter;
import edu.neu.ccs.wellness.storytelling.utils.OnGoToFragmentListener;
import edu.neu.ccs.wellness.reflection.ReflectionManager;
import edu.neu.ccs.wellness.storytelling.utils.StoryContentPagerAdapter;
import edu.neu.ccs.wellness.storytelling.viewmodel.CompletedChallengesViewModel;
import edu.neu.ccs.wellness.utils.CardStackPageTransformer;

public class StoryViewActivity extends AppCompatActivity
        implements OnGoToFragmentListener, ReflectionFragment.ReflectionFragmentListener {

    // CONSTANTS
    public static final int STORY_TITLE_FACE = R.font.montserrat_bold;
    public static final String STORY_TEXT_FACE = "fonts/pangolin_regular.ttf";
    public static final float PAGE_MIN_SCALE = 0.75f;

    //private Storywell storywell;
    private StoryInterface story;
    //private ReflectionManager reflectionManager;
    private StoryViewPresenter presenter;
    //private String groupName;
    //private String storyId;

    //private CardStackPageTransformer cardStackTransformer;

    //private SharedPreferences savePositionPreference;
    //private int currentPagePosition = 0;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    // @SuppressLint("StaticFieldLeak")
    //public static ViewPager viewPager;
    public ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WellnessRestServer.configureDefaultImageLoader(getApplicationContext());
        setContentView(R.layout.activity_storyview);

        this.viewPager = findViewById(R.id.container);
        this.viewPager.setPageTransformer(true,
                new CardStackPageTransformer(PAGE_MIN_SCALE));

        this.story = Story.create(getIntent().getExtras());
        //this.storywell = new Storywell(getApplicationContext());
        //this.groupName = this.storywell.getGroup().getName();
        // this.reflectionManager = new ReflectionManager(this.groupName, this.storyId, this.storywell.getReflectionIteration());
        this.presenter = new StoryViewPresenter(this, this.story);
        this.loadStory();

        this.presenter.logEvent();
        /*
        WellnessUserLogging userLogging = new WellnessUserLogging(this.groupName);
        Bundle bundle = new Bundle();
        bundle.putString("STORY_ID", this.storyId);
        userLogging.logEvent("READ_STORY", bundle);
        */
    }


    @Override
    public void onStart() {
        super.onStart();
        showNavigationInstruction();
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.presenter.doRefreshStoryState(this);
        /*
        //tryGoToThisPage(story.getState().getCurrentPage(), viewPager, story);
        savePositionPreference = PreferenceManager.getDefaultSharedPreferences(this);

        currentPagePosition = savePositionPreference.getInt("lastPagePositionSharedPref", 0);
        */
    }


    @Override
    protected void onPause() {
        super.onPause();
        this.presenter.doSaveStoryState(this);
        /*
        SharedPreferences.Editor putPositionInPref = savePositionPreference.edit();

        // Save the position when paused
        putPositionInPref.putInt("lastPagePositionSharedPref", currentPagePosition);
        putPositionInPref.apply();
        this.story.saveState(getApplicationContext(), storywell.getGroup());
        */
    }

    @Override
    public void onGoToFragment(TransitionType transitionType, int direction) {
        viewPager.setCurrentItem(viewPager.getCurrentItem() + direction);
    }

    @Override
    public boolean isReflectionExists(int contentId) {
        return this.presenter.isReflectionExists(contentId);
        //return this.presenter.isReflectionExists(String.valueOf(contentId));
    }

    @Override
    public void doStartRecording(int contentId, String contentGroupId, String contentGroupName) {
        this.presenter.doStartRecording(contentId, contentGroupId, contentGroupName);
        /*
        if (reflectionManager.getIsPlayingStatus() == true) {
            this.reflectionManager.stopPlayback();
        }

        if (reflectionManager.getIsRecordingStatus() == false) {
            this.reflectionManager.startRecording(getApplicationContext(),
                    String.valueOf(contentId), contentGroupId, contentGroupName,
                    new MediaRecorder());
        }
        */
    }

    @Override
    public void doStopRecording() {
        this.presenter.doStopRecording();
        /*
        if (reflectionManager.getIsRecordingStatus() == true) {
            this.reflectionManager.stopRecording();
        }
        */
    }

    @Override
    public void doStartPlay(int contentId, OnCompletionListener completionListener) {
        this.presenter.doStartPlay(contentId, completionListener);
        /*
        if (this.reflectionManager.getIsPlayingStatus() == false) {
            playReflectionIfExists(contentId, completionListener);
        }
        */
    }

    @Override
    public void doStopPlay() {
        this.presenter.doStopPlay();
        // this.reflectionManager.stopPlayback();
    }

    /*
    private void playReflectionIfExists(int contentId, OnCompletionListener completionListener) {
        String reflectionUrl = this.reflectionManager.getRecordingURL(String.valueOf(contentId));
        if (reflectionUrl != null) {
            this.reflectionManager.startPlayback(reflectionUrl, new MediaPlayer(), completionListener);
        }
    }
    */

    /* DATA LOADING METHODS AND CLASSES */
    /***
     * Initiate AsyncTask object that loads the Story's definition from the internal
     * storage. If no saved definition in the internal storage, then make an
     * HTTP call to download the definition. Once completed, load the reflection urls.
     */
    private void loadStory() {
        new LoadStoryDefAsync().execute();
    }

    private class LoadStoryDefAsync extends AsyncTask<Void, Integer, RestServer.ResponseType> {
        protected RestServer.ResponseType doInBackground(Void... nothingburger) {
            return presenter.asyncLoadStory(getApplicationContext());
            /*
            return story.tryLoadStoryDef(
                    getApplicationContext(), storywell.getServer(), storywell.getGroup());
                    */
        }

        protected void onPostExecute(RestServer.ResponseType result) {
            if (result == RestServer.ResponseType.NO_INTERNET) {
                showErrorMessage(getString(R.string.error_no_internet));
            } else if (result == RestServer.ResponseType.SUCCESS_202) {
                loadReflectionUrls();
            }
        }
    }

    /**
     * Initiate the reflection urls using {@link ReflectionManager}. Once completed,
     * initiate the fragments to show the story contents.
     */
    private void loadReflectionUrls() {
        new LoadReflectionUrlsAsync().execute();
    }

    private ValueEventListener readyToInitContentslistener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            initStoryContentFragments();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            // DO NOTHING
        }
    };

    public class LoadReflectionUrlsAsync extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            presenter.loadReflectionUrls(readyToInitContentslistener);
            // reflectionManager.getReflectionUrlsFromFirebase(readyToInitContentslistener);
            return null;
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
        StoryContentPagerAdapter mSectionsPagerAdapter = new StoryContentPagerAdapter(
                getSupportFragmentManager(), this.story);

        // Set up the transitions
        //this.cardStackTransformer = new CardStackPageTransformer(PAGE_MIN_SCALE);
        this.viewPager.setAdapter(mSectionsPagerAdapter);

        this.presenter.tryGoToThisPage(story.getState().getCurrentPage(), viewPager, story);
        /**
         * Detect a right swipe for reflections page
         * */
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int pos, float offset, int positionOffsetPixels) {
                if (offset > 0.95) {
                    presenter.doStopPlay();
                    // reflectionManager.stopPlayback();
                }
            }

            @Override
            public void onPageSelected(int position) {
                presenter.tryGoToThisPage(position, viewPager, story);
                presenter.uploadReflectionAudio();
                // tryUploadReflectionAudio();
                // currentPagePosition = position;
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
        String navigationInfo = getString(R.string.tooltip_storycontent_navigation);
        Toast toast = Toast.makeText(getApplicationContext(), navigationInfo, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, 10);
        toast.show();
    }

    /**
     * Show an error message on the screen.
     * @param msg The message to be shown.
     */
    private void showErrorMessage(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }


    /*
    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.

    public class StoryContentPagerAdapter extends FragmentPagerAdapter {

        private List<Fragment> fragments = new ArrayList<Fragment>();
        public StoryContentPagerAdapter(FragmentManager fm) {
            super(fm);
            for (StoryContent content : story.getContents()) {
                // boolean isResponseExists = story.getState().isReflectionResponded(content.getId());
                // this.fragments.add(StoryContentAdapter.getFragment(content, isResponseExists));

                this.fragments.add(StoryContentAdapter.getFragment(content));
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
    */


    // PRIVATE HELPER METHODS

    /*
    private void tryGoToThisPage(int position, ViewPager viewPager, StoryInterface story) {
        int allowedPosition = getAllowedPageToGo(position);
        story.getState().setCurrentPage(allowedPosition);
        viewPager.setCurrentItem(allowedPosition);
    }
    private void tryUploadReflectionAudio() {
        if (this.reflectionManager.isUploadQueued())
            new AsyncUploadAudio().execute();
    }

    public class AsyncUploadAudio extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            reflectionManager.uploadReflectionAudioToFirebase();
            return null;
        }
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
    private boolean canProceedToNextContent(StoryContent precContent) {
        switch (precContent.getType()) {
            case REFLECTION:
                return canProceedFromThisReflection(precContent);
            case CHALLENGE:
                return canProceedFromThisChallenge(precContent);
            default:
                return true;
        }
    }

    private boolean canProceedFromThisReflection(StoryContent precContent) {
        return this.isReflectionExists(precContent.getId());
    }

    private boolean canProceedFromThisChallenge(StoryContent precContent) {
        if (this.presenter.isCompletedChallengesListReady()) {
            StoryChallenge storyChallenge = (StoryChallenge)
                    story.getContentByIndex(currentPagePosition);
            return this.presenter
                    .isThisChallengeCompleted(storyChallenge.getChallengeId());
        } else {
            return false;
        }
    }
    */
}