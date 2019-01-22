package edu.neu.ccs.wellness.storytelling;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import edu.neu.ccs.wellness.logging.WellnessUserLogging;
import edu.neu.ccs.wellness.reflection.ReflectionManager;
import edu.neu.ccs.wellness.server.RestServer;
import edu.neu.ccs.wellness.story.Story;
import edu.neu.ccs.wellness.story.StoryManager;
import edu.neu.ccs.wellness.story.StoryReflection;
import edu.neu.ccs.wellness.story.interfaces.StoryContent;
import edu.neu.ccs.wellness.story.interfaces.StoryInterface;
import edu.neu.ccs.wellness.story.interfaces.StorytellingException;
import edu.neu.ccs.wellness.storytelling.utils.StoryContentAdapter;
import edu.neu.ccs.wellness.utils.CardStackPageTransformer;

/**
 * Created by hermansaksono on 1/17/19.
 */

public class ReflectionViewActivity extends AppCompatActivity {

    public static final float PAGE_MIN_SCALE = 0.75f;

    private Storywell storywell;
    private String groupName;
    private String storyId;
    private List<Integer> listOfReflections;
    private StoryManager storyManager;
    private StoryInterface story;
    private ReflectionManager reflectionManager;
    private CardStackPageTransformer cardStackTransformer;

    @SuppressLint("StaticFieldLeak")
    public static ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_reflectionview);
        this.storywell = new Storywell(getApplicationContext());
        this.storyManager = this.storywell.getStoryManager();
        this.groupName = this.storywell.getGroup().getName();

        this.storyId = getIntent().getStringExtra(Story.KEY_STORY_ID);
        this.listOfReflections = getListOfReflections(getIntent()
                .getStringArrayListExtra(Story.KEY_REFLECTION_LIST));
        this.reflectionManager = new ReflectionManager(
                this.groupName, this.storyId, this.storywell.getReflectionIteration());
        this.asyncLoadStoryDef();

        // Logging stuff
        WellnessUserLogging userLogging = new WellnessUserLogging(this.groupName);
        Bundle bundle = new Bundle();
        bundle.putString("STORY_ID", this.storyId);
        bundle.putInt("REFLECTION_START_CONTENT_ID", this.listOfReflections.get(0));
        userLogging.logEvent("VIEW_REFLECTION", bundle);
    }

    private List<Integer> getListOfReflections(List<String> listOfReflectionStringIds) {
        List<Integer> listOfReflectionInts = new ArrayList<>();
        for(String stringId : listOfReflectionStringIds) {
            listOfReflectionInts.add(Integer.valueOf(stringId));
        }
        return listOfReflectionInts;
    }

    @Override
    public void onStart() {
        super.onStart();
        showNavigationInstruction();
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void asyncLoadStoryDef() {
        new AsyncLoadStoryDef().execute();
    }

    private void asyncLoadReflectionUrls() {
        new AsyncDownloadReflectionUrls().execute();
    }


    private class AsyncLoadStoryDef extends AsyncTask<Void, Integer, RestServer.ResponseType> {
        protected RestServer.ResponseType doInBackground(Void... nothingburger) {
            try {
                story = storyManager.getStoryById(storyId);
                return story.tryLoadStoryDef(getApplicationContext(), storywell.getServer(),
                        storywell.getGroup());
            } catch (StorytellingException e) {
                e.printStackTrace();
                return RestServer.ResponseType.OTHER;
            }
        }

        protected void onPostExecute(RestServer.ResponseType result) {
            if (result == RestServer.ResponseType.NO_INTERNET) {
                showErrorMessage(getString(R.string.error_no_internet));
            } else if (result == RestServer.ResponseType.SUCCESS_202) {
                initStoryContentFragments();
                asyncLoadReflectionUrls();
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

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class ReflectionsPagerAdapter extends FragmentPagerAdapter {

        private List<Fragment> fragments = new ArrayList<Fragment>();

        public ReflectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            StoryReflection content = null;
            for (Integer contentId : listOfReflections) {
                content = (StoryReflection) story.getContents().get(contentId);
                this.fragments.add(StoryContentAdapter.getFragment(content));
            }

            if (content.isNextExists()) {
                StoryContent statementContent = story.getContents().get(content.getNextId());
                this.fragments.add(StoryContentAdapter.getFragment(statementContent));
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

    /* Initalizing Fragments */
    private void initStoryContentFragments() {
        ReflectionsPagerAdapter reflectionsPagerAdapter = new ReflectionsPagerAdapter(
                getSupportFragmentManager());

        // Set up the transitions
        cardStackTransformer = new CardStackPageTransformer(PAGE_MIN_SCALE);
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(reflectionsPagerAdapter);
        mViewPager.setPageTransformer(true, cardStackTransformer);
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
}
