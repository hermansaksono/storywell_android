package edu.neu.ccs.wellness.storytelling;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import edu.neu.ccs.wellness.server.RestServer;
import edu.neu.ccs.wellness.storytelling.interfaces.StoryContent;
import edu.neu.ccs.wellness.storytelling.interfaces.StoryInterface;
import edu.neu.ccs.wellness.storytelling.models.Story;
import edu.neu.ccs.wellness.server.WellnessRestServer;
import edu.neu.ccs.wellness.server.WellnessUser;
import edu.neu.ccs.wellness.storytelling.storyview.StoryContentAdapter;
import edu.neu.ccs.wellness.utils.CardStackPageTransformer;
import edu.neu.ccs.wellness.utils.OnGoToFragmentListener;

public class StoryViewActivity extends AppCompatActivity implements OnGoToFragmentListener {
    // CONSTANTS
    public static final String STORY_TEXT_FACE = "fonts/pangolin_regular.ttf";
    public static final float PAGE_MIN_SCALE = 0.75f;
    private WellnessUser user;
    private WellnessRestServer server;
    private StoryInterface story;

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
    private ViewPager mViewPager;

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
        this.user = new WellnessUser(WellnessRestServer.DEFAULT_USER,
                WellnessRestServer.DEFAULT_PASS);
        this.server = new WellnessRestServer(WellnessRestServer.WELLNESS_SERVER_URL, 0,
                WellnessRestServer.STORY_API_PATH, user);
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
    }

    /**
     * Show the navigation instruction on the screen
     */
    private void showNavigationInstruction() {
        String navigationInfo = getString(R.string.tooltip_storycontent_navigation);
        Toast toast = Toast.makeText(getApplicationContext(), navigationInfo, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM|Gravity.CENTER, 0, 0);
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
         */
        public StoryContentPagerAdapter(FragmentManager fm) {
            super(fm);
            for (StoryContent content : story.getContents()) {
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

    // ASYNCTASK CLASSES
    private class AsyncLoadStoryDef extends AsyncTask<Void, Integer, RestServer.ResponseType> {
        protected RestServer.ResponseType doInBackground(Void... nothingburger){
            RestServer.ResponseType result = null;
            if (server.isOnline(getApplicationContext())) {
                story.loadStoryDef(getApplicationContext(), server);
                result = RestServer.ResponseType.SUCCESS_202;
            }
            else {
                result = RestServer.ResponseType.NO_INTERNET;
            }
            return result;
        }

        protected void onPostExecute(RestServer.ResponseType result) {
            if (result == RestServer.ResponseType.NO_INTERNET) {
                showErrorMessage(getString(R.string.error_no_internet));
            }
            else if (result == RestServer.ResponseType.SUCCESS_202) {
                InitStoryContentFragments();
            }
        }
    }

    // PRIVATE HELPER METHODS
    private void showErrorMessage(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }
}
