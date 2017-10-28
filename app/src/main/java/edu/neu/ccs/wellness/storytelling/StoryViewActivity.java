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
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import edu.neu.ccs.wellness.server.RestServer;
import edu.neu.ccs.wellness.server.WellnessRestServer;
import edu.neu.ccs.wellness.server.WellnessUser;
import edu.neu.ccs.wellness.storytelling.interfaces.StoryContent;
import edu.neu.ccs.wellness.storytelling.interfaces.StoryInterface;
import edu.neu.ccs.wellness.storytelling.models.Story;
import edu.neu.ccs.wellness.storytelling.storyview.ReflectionFragment;
import edu.neu.ccs.wellness.storytelling.storyview.StoryContentAdapter;
import edu.neu.ccs.wellness.utils.CardStackPageTransformer;
import edu.neu.ccs.wellness.utils.OnGoToFragmentListener;

import static edu.neu.ccs.wellness.StreamReflectionsFirebase.reflectionsUrlHashMap;
import static edu.neu.ccs.wellness.storytelling.storyview.ReflectionFragment.isRecordingInitiated;

public class StoryViewActivity extends AppCompatActivity implements OnGoToFragmentListener {
    // CONSTANTS
    public static final String STORY_TEXT_FACE = "fonts/pangolin_regular.ttf";
    public static final float PAGE_MIN_SCALE = 0.75f;
    private WellnessUser user;
    private WellnessRestServer server;
    private StoryInterface story;
    public static boolean visitedSevenOnce = false;
    public static boolean phase2 = false;


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

//                    case 5:
//                        if (reflectionsUrlHashMap.get(6) != null) {
//                            buttonNext.setText(getText(R.string.reflection_button_next));
//                            buttonNext.setAlpha(1);
//                            buttonReplay.setText(getText(R.string.reflection_button_replay));
//                            buttonReplay.setAlpha(1);
//                            buttonRespond.setText(getText(R.string.reflection_button_answer_again));
//                            buttonNext.setVisibility(View.VISIBLE);
//                            buttonReplay.setVisibility(View.VISIBLE);
//                            isRecordingInitiated = true;
//                            Toast.makeText(getBaseContext(), "IN 5TH IF BLOCK", Toast.LENGTH_SHORT).show();
//                        }
//                        break;

                    case 6:
//                        if (reflectionsUrlHashMap.get(7) != null) {
//                            buttonNext.setText(getText(R.string.reflection_button_next));
//                            buttonReplay.setText(getText(R.string.reflection_button_replay));
//                            buttonRespond.setText(getText(R.string.reflection_button_answer_again));
//                            buttonNext.setVisibility(View.VISIBLE);
//                            buttonReplay.setVisibility(View.VISIBLE);
//                            buttonNext.setAlpha(1);
//                            buttonReplay.setAlpha(1);
//                            isRecordingInitiated = true;
//                            visitedSevenOnce = true;
//                            phase2 = true;
//                            Toast.makeText(getBaseContext(), "IN 6TH IF BLOCK", Toast.LENGTH_SHORT).show();
//                        }

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

}