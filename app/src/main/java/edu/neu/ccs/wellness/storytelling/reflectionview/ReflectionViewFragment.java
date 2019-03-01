package edu.neu.ccs.wellness.storytelling.reflectionview;

import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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
import edu.neu.ccs.wellness.storytelling.R;
import edu.neu.ccs.wellness.storytelling.Storywell;
import edu.neu.ccs.wellness.storytelling.storyview.ReflectionFragment;
import edu.neu.ccs.wellness.storytelling.utils.OnGoToFragmentListener;
import edu.neu.ccs.wellness.storytelling.utils.StoryContentAdapter;
import edu.neu.ccs.wellness.utils.CardStackPageTransformer;

/**
 * Created by hermansaksono on 1/17/19.
 */

public class ReflectionViewFragment extends Fragment
        implements OnGoToFragmentListener, ReflectionFragment.ReflectionFragmentListener {

    public static final float PAGE_MIN_SCALE = 0.75f;

    private Storywell storywell;
    private String groupName;
    private String storyId;
    private List<Integer> listOfReflections;
    private StoryManager storyManager;
    private StoryInterface story;
    private ReflectionManager reflectionManager;
    private CardStackPageTransformer cardStackTransformer;
    private String formattedDate;

    private View view;
    private ViewPager mViewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_reflection_activity, container, false);
        this.mViewPager = this.view.findViewById(R.id.container);

        this.storywell = new Storywell(getContext());
        this.storyManager = this.storywell.getStoryManager();
        this.groupName = this.storywell.getGroup().getName();

        this.storyId = getArguments().getString(Story.KEY_STORY_ID);
        this.listOfReflections = getListOfReflections(getArguments()
                .getStringArrayList(Story.KEY_REFLECTION_LIST));
        this.reflectionManager = new ReflectionManager(
                this.groupName, this.storyId, this.storywell.getReflectionIteration(), getContext());
        this.formattedDate = getFormattedDate(getArguments().getLong(Story.KEY_RESPONSE_TIMESTAMP,0));
        this.asyncLoadStoryDef();

        // Logging stuff
        WellnessUserLogging userLogging = new WellnessUserLogging(this.groupName);
        Bundle bundle = new Bundle();
        bundle.putString("STORY_ID", this.storyId);
        bundle.putInt("REFLECTION_START_CONTENT_ID", this.listOfReflections.get(0));
        userLogging.logEvent("VIEW_REFLECTION", bundle);

        return this.view;
    }

    private List<Integer> getListOfReflections(List<String> listOfReflectionStringIds) {
        List<Integer> listOfReflectionInts = new ArrayList<>();
        for(String stringId : listOfReflectionStringIds) {
            listOfReflectionInts.add(Integer.valueOf(stringId));
        }
        Collections.sort(listOfReflectionInts);
        return listOfReflectionInts;
    }

    @Override
    public void onGoToFragment(TransitionType transitionType, int direction) {
        mViewPager.setCurrentItem(mViewPager.getCurrentItem() + direction);
    }

    @Override
    public boolean isReflectionExists(int contentId) {
        return reflectionManager.isReflectionResponded(String.valueOf(contentId));
    }

    @Override
    public void doStartRecording(int contentId, String contentGroupId, String contentGroupName) {
        // Do nothing
    }

    @Override
    public void doStopRecording() {
        // Do nothing
    }

    @Override
    public void doStartPlay(int contentId, MediaPlayer.OnCompletionListener completionListener) {
        if (this.reflectionManager.getIsPlayingStatus() == false) {
            playReflectionIfExists(contentId, completionListener);
        }
    }

    @Override
    public void doStopPlay() {
        this.reflectionManager.stopPlayback();
    }

    private void playReflectionIfExists(
            int contentId, MediaPlayer.OnCompletionListener completionListener) {
        String reflectionUrl = this.reflectionManager.getRecordingURL(String.valueOf(contentId));
        if (reflectionUrl != null) {
            this.reflectionManager
                    .startPlayback(reflectionUrl, new MediaPlayer(), completionListener);
        }
    }

    private void asyncLoadStoryDef() {
        new AsyncLoadStoryDef().execute();
    }

    private void asyncLoadReflectionUrls() {
        new AsyncDownloadReflectionUrls().execute();
    }

    private ValueEventListener listener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            initStoryContentFragments();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            // DO NOTHING
        }
    };

    private class AsyncLoadStoryDef extends AsyncTask<Void, Integer, RestServer.ResponseType> {
        protected RestServer.ResponseType doInBackground(Void... nothingburger) {
            try {
                storyManager.loadStoryList(getContext());
                story = storyManager.getStoryById(storyId);
                return story.tryLoadStoryDef(
                        getContext(), storywell.getServer(), storywell.getGroup());
            } catch (StorytellingException e) {
                e.printStackTrace();
                return RestServer.ResponseType.OTHER;
            }
        }

        protected void onPostExecute(RestServer.ResponseType result) {
            if (result == RestServer.ResponseType.NO_INTERNET) {
                showErrorMessage(getString(R.string.error_no_internet));
            } else if (result == RestServer.ResponseType.SUCCESS_202) {
                asyncLoadReflectionUrls();
            }
        }
    }

    public class AsyncDownloadReflectionUrls extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            reflectionManager.getReflectionUrlsFromFirebase(listener);
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
                Fragment fragment = StoryContentAdapter.getFragment(content);
                fragment.getArguments().putBoolean(
                        StoryContentAdapter.KEY_CONTENT_ALLOW_EDIT, false);
                fragment.getArguments().putString(
                        StoryContentAdapter.KEY_REFLECTION_DATE, formattedDate);
                this.fragments.add(fragment);
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
        ReflectionsPagerAdapter reflectionsPagerAdapter =
                new ReflectionsPagerAdapter(getChildFragmentManager());

        // Set up the transitions
        cardStackTransformer = new CardStackPageTransformer(PAGE_MIN_SCALE);
        mViewPager = this.view.findViewById(R.id.container);
        mViewPager.setAdapter(reflectionsPagerAdapter);
        mViewPager.setPageTransformer(true, cardStackTransformer);
    }

    private void showErrorMessage(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private static final String TREASURE_DATE_FORMAT = "EEE, MMM d, yyyy";
    private static final String EMPTY_DATE_STRING = "";

    private String getFormattedDate(Long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat(TREASURE_DATE_FORMAT);
        if (timestamp > 0) {
            Date date = new Date(timestamp);
            return sdf.format(date);
        } else {
            return EMPTY_DATE_STRING;
        }
    }
}
