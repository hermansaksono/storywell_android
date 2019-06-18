package edu.neu.ccs.wellness.storytelling;

import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import edu.neu.ccs.wellness.fitness.interfaces.AvailableChallengesInterface;
import edu.neu.ccs.wellness.fitness.interfaces.UnitChallengeInterface;
import edu.neu.ccs.wellness.reflection.ReflectionManager;
import edu.neu.ccs.wellness.server.RestServer;
import edu.neu.ccs.wellness.server.WellnessRestServer;
import edu.neu.ccs.wellness.story.Story;
import edu.neu.ccs.wellness.story.interfaces.StoryInterface;
import edu.neu.ccs.wellness.storytelling.storyview.ChallengePickerFragment;
import edu.neu.ccs.wellness.storytelling.storyview.MemoFragment;
import edu.neu.ccs.wellness.storytelling.storyview.ReflectionFragment;
import edu.neu.ccs.wellness.storytelling.storyview.StoryViewPresenter;
import edu.neu.ccs.wellness.storytelling.utils.OnGoToFragmentListener;
import edu.neu.ccs.wellness.storytelling.utils.StoryContentPagerAdapter;
import edu.neu.ccs.wellness.storytelling.utils.UserLogging;
import edu.neu.ccs.wellness.storytelling.viewmodel.ChallengePickerViewModel;
import edu.neu.ccs.wellness.utils.CardStackPageTransformer;

public class StoryViewActivity extends AppCompatActivity implements
        OnGoToFragmentListener,
        ReflectionFragment.ReflectionFragmentListener,
        ChallengePickerFragment.ChallengePickerFragmentListener,
        MemoFragment.OnResetStoryListener {

    // CONSTANTS
    public static final String STORY_TEXT_FACE = "fonts/pangolin_regular.ttf";
    public static final float PAGE_MIN_SCALE = 0.75f;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager viewPager;

    private StoryInterface story;
    private StoryViewPresenter presenter;
    private LiveData<AvailableChallengesInterface> groupChallengesLiveData;

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

        /* Create the LiveData */
        this.groupChallengesLiveData = ViewModelProviders.of(this)
                .get(ChallengePickerViewModel.class)
                .getGroupChallenges();

        UserLogging.logStoryView(this.story);
    }


    @Override
    public void onStart() {
        super.onStart();
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

    @Override
    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof ChallengePickerFragment) {
            ChallengePickerFragment challengePickerFragment = (ChallengePickerFragment) fragment;
            challengePickerFragment.setGroupChallengeLiveData(groupChallengesLiveData);
        }
    }

    @Override
    public void onResetStory() {
        Intent data = new Intent();

        data.putExtra(HomeActivity.RESULT_CODE, HomeActivity.RESULT_RESET_THIS_STORY);
        data.putExtra(HomeActivity.CODE_STORY_ID_TO_RESET, story.getId());

        setResult(Activity.RESULT_OK, data);
        finish();
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
            switch (result) {
                case NO_INTERNET:
                    showErrorMessage(getString(R.string.error_no_internet));
                    break;
                case LOGIN_EXPIRED:
                    showLoginExpiredSnackbar();
                    break;
                case SUCCESS_202:
                    loadReflectionUrls();
                    break;
                default:
                    showConnectionErrorSnackbar();
                    break;
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
                this.presenter.getCurrentPagePosition(), viewPager, story, getApplicationContext());

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

        findViewById(R.id.layout_waiting_loading).setVisibility(View.GONE);
    }

    /**
     * Show an error message on the screen.
     * @param msg The message to be shown.
     */
    private void showErrorMessage(String msg) {
        Snackbar snackbar = getSnackbar(msg);
        snackbar.setAction(R.string.button_error_cant_load_story, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        snackbar.show();
    }

    /**
     * Show a Snackbar indicating timeout.
     */
    private void showConnectionErrorSnackbar() {
        String text = getString(R.string.error_cant_load_story);
        Snackbar snackbar = getSnackbar(text);
        snackbar.setAction(R.string.button_error_cant_load_story, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        snackbar.show();
    }

    /**
     * Show a Snackbar indicating that the login has expired.
     */
    private void showLoginExpiredSnackbar() {
        String text = getString(R.string.error_login_expired);
        Snackbar snackbar = getSnackbar(text);
        snackbar.setAction(R.string.button_error_cant_load_story, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        snackbar.show();
    }

    /**
     * Get a snackbar to show.
     * @param text
     * @return
     */
    private Snackbar getSnackbar(String text) {
        View view = findViewById(R.id.main_content);
        return Snackbar.make(view, text, Snackbar.LENGTH_INDEFINITE);
    }
}