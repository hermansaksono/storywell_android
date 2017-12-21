package edu.neu.ccs.wellness.storytelling;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import edu.neu.ccs.wellness.server.RestServer;
import edu.neu.ccs.wellness.server.WellnessRestServer;
import edu.neu.ccs.wellness.server.WellnessUser;
import edu.neu.ccs.wellness.story.interfaces.StoryContent;
import edu.neu.ccs.wellness.story.interfaces.StoryInterface;
import edu.neu.ccs.wellness.story.Story;
import edu.neu.ccs.wellness.story.StoryState;
import edu.neu.ccs.wellness.storytelling.storyview.ReflectionFragment;
import edu.neu.ccs.wellness.storytelling.utils.StoryContentAdapter;
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

    private Storywell storywell;
    private StoryInterface story;

    private CardStackPageTransformer cardStackTransformer;
    private HashMap<Integer, String> reflectionUrlsHashMap;

    private SharedPreferences savePositionPreference;
    private int lastPagePosition = 0;

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
        this.reflectionUrlsHashMap = new HashMap<>();
        this.storywell = new Storywell(getApplicationContext());
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
        putPositionInPref.putInt("lastPagePositionSharedPref", lastPagePosition);
        putPositionInPref.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        savePositionPreference = PreferenceManager.getDefaultSharedPreferences(this);
        lastPagePosition = savePositionPreference.getInt("lastPagePositionSharedPref", 0);
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
        StoryState state = (StoryState) this.story.getState();
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

        /**
         * Detect a right swipe for reflections page
         * */
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

                /**Upload to Firebase if user scrolls*/
                if (uploadToFirebase) {
                    UploadAudioAsyncTask uploadAudio = new UploadAudioAsyncTask(
                            StoryViewActivity.this, lastPagePosition);
                    uploadAudio.execute();
                }
                tryGoToThisPage(position, mViewPager, story);
                story.getState().save(getApplicationContext());
                lastPagePosition = position;
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
            if (storywell.isServerOnline()) {
                story.loadStoryDef(getApplicationContext(), storywell.getServer());
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
                new AsyncDownloadReflectionUrls(
                        storywell.getGroup().getName(),
                        String.valueOf(story.getId())
                ).execute();
            }
        }
    }

    public class AsyncDownloadReflectionUrls extends AsyncTask<Void, Void, Void> {
        private DatabaseReference mDBReference = FirebaseDatabase.getInstance().getReference();
        private String groupName;
        private String storyId;

        AsyncDownloadReflectionUrls(String groupName, String storyId) {
            this.groupName = groupName;
            this.storyId = storyId;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mDBReference
                    .child(groupName)
                    .child(storyId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            reflectionsUrlHashMap = getReflectionsUrl(dataSnapshot);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            reflectionsUrlHashMap.clear();
                        }
                    });
            return null;
        }
    }//End of AsyncTask

    /* ASYNCTASK HELPER FUNCTIONS */
    private static HashMap<Integer, String> getReflectionsUrl(DataSnapshot dataSnapshot) {
        HashMap<Integer, String> reflectionUrlsHashMap = new HashMap<Integer, String>();
        if (dataSnapshot.exists()) {
            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                List<Object> listOfUrls = new ArrayList<>((Collection<?>) ((HashMap<Object, Object>) ds.getValue()).values());
                reflectionUrlsHashMap.put(Integer.parseInt(ds.getKey()), getLastReflectionsUrl(listOfUrls));
            }
        }
        return reflectionUrlsHashMap;
    }

    private static String getLastReflectionsUrl(List<Object> listOfUrl) {
        return (String) listOfUrl.get(listOfUrl.size() - 1);
    }

}//End of Activity