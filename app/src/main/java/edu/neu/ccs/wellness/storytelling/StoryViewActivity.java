package edu.neu.ccs.wellness.storytelling;

import android.media.MediaPlayer.OnCompletionListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import edu.neu.ccs.wellness.fitness.interfaces.UnitChallengeInterface;
import edu.neu.ccs.wellness.reflection.ReflectionManager;
import edu.neu.ccs.wellness.server.RestServer;
import edu.neu.ccs.wellness.server.WellnessRestServer;
import edu.neu.ccs.wellness.story.Story;
import edu.neu.ccs.wellness.story.interfaces.StoryInterface;
import edu.neu.ccs.wellness.storytelling.storyview.ChallengePickerFragment;
import edu.neu.ccs.wellness.storytelling.storyview.ReflectionFragment;
import edu.neu.ccs.wellness.storytelling.storyview.StoryViewPresenter;
import edu.neu.ccs.wellness.storytelling.utils.OnGoToFragmentListener;
import edu.neu.ccs.wellness.storytelling.utils.StoryContentPagerAdapter;
import edu.neu.ccs.wellness.utils.CardStackPageTransformer;

public class StoryViewActivity extends AppCompatActivity implements
        OnGoToFragmentListener,
        ReflectionFragment.ReflectionFragmentListener,
        ChallengePickerFragment.ChallengePickerFragmentListener {

    // CONSTANTS
    public static final int STORY_TITLE_FACE = R.font.montserrat_bold;
    public static final String STORY_TEXT_FACE = "fonts/pangolin_regular.ttf";
    public static final float PAGE_MIN_SCALE = 0.75f;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager viewPager;

    private StoryInterface story;
    private StoryViewPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WellnessRestServer.configureDefaultImageLoader(getApplicationContext());
        setContentView(R.layout.activity_storyview);

        this.viewPager = findViewById(R.id.container);
        this.viewPager.setPageTransformer(true,
                new CardStackPageTransformer(PAGE_MIN_SCALE));

        this.story = Story.create(getIntent().getExtras());
        this.presenter = new StoryViewPresenter(this, this.story);
        this.loadStory();

        this.presenter.logEvent();
    }


    @Override
    public void onStart() {
        super.onStart();
        // showNavigationInstruction(); Disabling this for now. We're using a permanent text.
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.presenter.doRefreshStoryState(this);
    }


    @Override
    protected void onPause() {
        super.onPause();
        this.presenter.doSaveStoryState(this);
    }

    @Override
    public void onGoToFragment(TransitionType transitionType, int direction) {
        viewPager.setCurrentItem(viewPager.getCurrentItem() + direction);
    }

    @Override
    public boolean isReflectionExists(int contentId) {
        return this.presenter.isReflectionExists(contentId);
    }

    @Override
    public void doStartRecording(int contentId, String contentGroupId, String contentGroupName) {
        this.presenter.doStartRecording(contentId, contentGroupId, contentGroupName);
    }

    @Override
    public void doStopRecording() {
        this.presenter.doStopRecording();
    }

    @Override
    public void doStartPlay(int contentId, OnCompletionListener completionListener) {
        this.presenter.doStartPlay(contentId, completionListener);
    }

    @Override
    public void doStopPlay() {
        this.presenter.doStopPlay();
    }

    @Override
    public void onChallengePicked(UnitChallengeInterface unitChallenge) {
        this.presenter.setCurrentStoryChapterAsLocked(this);
    }

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
                getSupportFragmentManager(), this.story, getApplicationContext());

        this.viewPager.setAdapter(mSectionsPagerAdapter);

        this.presenter.tryGoToThisPage(
                story.getState().getCurrentPage(), viewPager, story, getApplicationContext());

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int pos, float offset, int positionOffsetPixels) {
                if (offset > 0.95) {
                    presenter.doStopPlay();
                }
            }

            @Override
            public void onPageSelected(int position) {
                presenter.tryGoToThisPage(position, viewPager, story, getApplicationContext());
                presenter.uploadReflectionAudio();
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
}