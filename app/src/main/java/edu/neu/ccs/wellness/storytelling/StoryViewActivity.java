package edu.neu.ccs.wellness.storytelling;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import edu.neu.ccs.wellness.server.RestServer;
import edu.neu.ccs.wellness.server.WellnessRestServer;
import edu.neu.ccs.wellness.server.WellnessUser;
import edu.neu.ccs.wellness.storytelling.interfaces.StoryContent;
import edu.neu.ccs.wellness.storytelling.interfaces.StoryInterface;
import edu.neu.ccs.wellness.storytelling.models.Story;
import edu.neu.ccs.wellness.storytelling.models.StoryState;
import edu.neu.ccs.wellness.storytelling.storyview.ReflectionFragment;
import edu.neu.ccs.wellness.storytelling.storyview.StoryContentAdapter;
import edu.neu.ccs.wellness.utils.CardStackPageTransformer;
import edu.neu.ccs.wellness.storytelling.utils.OnGoToFragmentListener;
import edu.neu.ccs.wellness.storytelling.utils.UploadAudioAsyncTask;

import static edu.neu.ccs.wellness.storytelling.storyview.ReflectionFragment.uploadToFirebase;
import static edu.neu.ccs.wellness.storytelling.utils.StreamReflectionsFirebase.reflectionsUrlHashMap;


public class StoryViewActivity extends AppCompatActivity
        implements OnGoToFragmentListener,
        ReflectionFragment.OnPlayButtonListener,
        ReflectionFragment.OnRecordButtonListener,
        ReflectionFragment.GetStoryListener {

    // CONSTANTS
    public static final String STORY_TEXT_FACE = "fonts/pangolin_regular.ttf";
    public static final float PAGE_MIN_SCALE = 0.75f;

    private WellnessUser user;
    private WellnessRestServer server;
    private StoryInterface story;

    private CardStackPageTransformer cardStackTransformer;

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
     * Show the navigation instruction on the screen
     */
    private void showNavigationInstruction() {
        //TODO: Replace with a SnackBar with ability to Swipe and end it
        String navigationInfo = getString(R.string.tooltip_storycontent_navigation);
        Toast toast = Toast.makeText(getApplicationContext(), navigationInfo, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, 0);
        toast.show();
    }


    /**
     * Get the recording state
     */
    @Override
    public void onPlayButtonPressed(int contentId) {
        StoryState state = (StoryState) story.getState();
//        Toast.makeText(getApplicationContext(), state.getRecordingURL(contentId), Toast.LENGTH_LONG).show();
    }

    /**
     * Update the recording state once we have the recording
     */
    @Override
    public void onRecordButtonPressed(int contentId, String urlRecording) {
        story.getState().addReflection(contentId, urlRecording);
    }

    @Override
    public StoryInterface getStoryState() {
        return this.story;
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

    /**
     * Detect a right swipe for reflections page
     * */
    /**
     * Initialize the pages in the storybook. A page is a fragment that mirrors
     * the list of StoryContent objects in the Story objects.
     * INVARIANT: The Story's stories variable has been initialized
     */
    private void InitStoryContentFragments() {
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

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setPageTransformer(true, cardStackTransformer);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                /**Stop the MediaPlayer if scrolled*/
                if (MediaPlayerSingleton.getInstance().getPlayingState()) {
                    MediaPlayerSingleton.getInstance().stopPlayback();
                }

                /**Ypload to Firebase if user scrolls*/
                if (uploadToFirebase) {
                    UploadAudioAsyncTask uploadAudio = new UploadAudioAsyncTask(
                            StoryViewActivity.this,
                            position - 1);
                    uploadAudio.execute();
                }
                tryGoToThisPage(position, mViewPager, story);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }


    // PRIVATE HELPER METHODS
    private void tryGoToThisPage(int position, ViewPager viewPager, StoryInterface story) {
        int gotoPosition = position;
        if (position - 1 >= 0) {
            StoryContent prevContent = story.getContentByIndex(position - 1);
            if (isReflection(prevContent)
                    && !isReflectionResponded(story, prevContent)) {

                //Check if there is a reflection in firebase
                if (reflectionsUrlHashMap.get(gotoPosition) == null) {
                    //If there is no file there as well, there is no recording
                    gotoPosition = position - 1;
                }

            }
        }
        viewPager.setCurrentItem(gotoPosition);
    }

    private static boolean isReflection(StoryContent content) {
        return content.getType().equals(StoryContent.ContentType.REFLECTION);
    }

    private static boolean isReflectionResponded(StoryInterface story, StoryContent content) {
        return story.getState().isReflectionResponded(content.getId());
    }


    private StoryInterface getStory() {
        return story;
    }


    private void showErrorMessage(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
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
            Log.i("WELL Story download", result.toString());
            if (result == RestServer.ResponseType.NO_INTERNET) {
                showErrorMessage(getString(R.string.error_no_internet));
            } else if (result == RestServer.ResponseType.SUCCESS_202) {
                InitStoryContentFragments();
            }
        }
    }//End of AsyncTask


}//End of Activity