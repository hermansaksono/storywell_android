package edu.neu.ccs.wellness.storytelling.reflectionview;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import java.util.Date;
import java.util.List;

import edu.neu.ccs.wellness.reflection.CalmingManager;
import edu.neu.ccs.wellness.reflection.ReflectionManager;
import edu.neu.ccs.wellness.reflection.ResponseManager;
import edu.neu.ccs.wellness.reflection.TreasureItem;
import edu.neu.ccs.wellness.reflection.TreasureItemType;
import edu.neu.ccs.wellness.server.RestServer.ResponseType;
import edu.neu.ccs.wellness.story.CalmingReflectionSet;
import edu.neu.ccs.wellness.story.StoryReflection;
import edu.neu.ccs.wellness.story.interfaces.StoryContent;
import edu.neu.ccs.wellness.story.interfaces.StoryInterface;
import edu.neu.ccs.wellness.story.interfaces.StorytellingException;
import edu.neu.ccs.wellness.storytelling.R;
import edu.neu.ccs.wellness.storytelling.Storywell;
import edu.neu.ccs.wellness.storytelling.storyview.ReflectionFragment;
import edu.neu.ccs.wellness.storytelling.utils.OnGoToFragmentListener;
import edu.neu.ccs.wellness.storytelling.utils.StoryContentAdapter;
import edu.neu.ccs.wellness.storytelling.utils.UserLogging;
import edu.neu.ccs.wellness.storytelling.viewmodel.CalmingReflectionViewModel;
import edu.neu.ccs.wellness.utils.CardStackPageTransformer;

/**
 * Created by hermansaksono on 1/17/19.
 */

public class ReflectionViewFragment extends Fragment
        implements OnGoToFragmentListener, ReflectionFragment.ReflectionFragmentListener {

    public static final float PAGE_MIN_SCALE = 0.75f;
    private static final String EMPTY_DATE_STRING = "";

    private Storywell storywell;
    private String groupName;
    private int reflectionIteration;
    private long reflectionMinEpoch;
    private int treasureParentType;
    private String treasureParentId;
    private List<Integer> treasureContents;
    private List<StoryContent> pageContentList;
    private ResponseManager responseManager;
    private boolean allowEditContent = false;
    private String formattedDate;

    private View view;
    private ViewPager viewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.view = inflater.inflate(
                R.layout.fragment_reflection_view, container, false);
        this.viewPager = this.view.findViewById(R.id.container);
        this.storywell = new Storywell(getContext());
        this.groupName = this.storywell.getGroup().getName();
        this.reflectionIteration = this.storywell.getReflectionIteration();
        this.reflectionMinEpoch = this.storywell.getReflectionIterationMinEpoch();

        Bundle bundle = getArguments();
        this.treasureParentType = bundle.getInt(TreasureItem.KEY_TYPE, 0);
        this.treasureParentId = bundle.getString(TreasureItem.KEY_PARENT_ID);
        this.treasureContents = bundle.getIntegerArrayList(TreasureItem.KEY_CONTENTS);
        this.allowEditContent = bundle.getBoolean(StoryContentAdapter.KEY_CONTENT_ALLOW_EDIT,
                false);
        this.formattedDate = getFormattedDate(
                bundle.getLong(TreasureItem.KEY_LAST_UPDATE_TIMESTAMP,0));


        this.responseManager = getResponseManager(groupName, treasureParentType, treasureParentId,
                reflectionIteration, reflectionMinEpoch, getContext());

        this.loadContents(this.treasureParentType);
        UserLogging.logViewTreasure(this.treasureParentId, this.treasureContents.get(0));
        return this.view;
    }

    private static ResponseManager getResponseManager(String groupName, int treasureType,
                                                      String treasureParentId,
                                                      int reflectionIteration,
                                                      long reflectionMinEpoch,
                                                      Context context) {
        switch (treasureType) {
            case TreasureItemType.STORY_REFLECTION:
                return new ReflectionManager(groupName, treasureParentId,
                        reflectionIteration, reflectionMinEpoch, context);
            case TreasureItemType.CALMING_PROMPT:
                return new CalmingManager (groupName, treasureParentId,
                        reflectionIteration, reflectionMinEpoch, context);
            default:
                return new ReflectionManager(groupName, treasureParentId,
                        reflectionIteration, reflectionMinEpoch, context);
        }
    }

    private String getFormattedDate(Long timestamp) {
        String format = getString(R.string.reflection_date_info);
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        if (timestamp > 0) {
            Date date = new Date(timestamp);
            return sdf.format(date);
        } else {
            return EMPTY_DATE_STRING;
        }
    }

    /* INHERITED METHODS */
    @Override
    public void onGoToFragment(TransitionType transitionType, int direction) {
        viewPager.setCurrentItem(viewPager.getCurrentItem() + direction);
    }

    @Override
    public boolean isReflectionExists(int contentId) {
        return responseManager.isReflectionResponded(String.valueOf(contentId));
    }

    @Override
    public void doStartRecording(int contentId, String contentGroupId, String contentGroupName) {
        if (responseManager.getIsPlayingStatus() == true) {
            this.responseManager.stopPlayback();
        }

        if (responseManager.getIsRecordingStatus() == false) {
            this.responseManager.startRecording(
                    String.valueOf(contentId),
                    contentGroupId,
                    contentGroupName,
                    new MediaRecorder());
        }
    }

    @Override
    public void doStopRecording() {
        if (responseManager.getIsRecordingStatus() == true) {
            this.responseManager.stopRecording();
        }
    }

    @Override
    public void doStartPlay(int contentId, MediaPlayer.OnCompletionListener completionListener) {
        if (this.responseManager.getIsPlayingStatus() == false) {
            playReflectionIfExists(contentId, completionListener);
        }
    }

    private void playReflectionIfExists(
            int contentId, MediaPlayer.OnCompletionListener completionListener) {
        String reflectionUrl = this.responseManager.getRecordingURL(String.valueOf(contentId));
        if (reflectionUrl != null) {
            this.responseManager
                    .startPlayback(reflectionUrl, new MediaPlayer(), completionListener);
        }
    }

    @Override
    public void doStopPlay() {
        this.responseManager.stopPlayback();
    }

    /**
     * Load contents depending on the type of {@link TreasureItem} that was given.
     * @param type
     */
    private void loadContents(int type) {
        switch (type) {
            case TreasureItemType.STORY_REFLECTION:
                new LoadStoryDefAndReflectionUris().execute();
                break;
            case TreasureItemType.CALMING_PROMPT:
                loadCalmingReflectionAndResponseUris();
                break;
        }
    }

    /**
     * Load story contents asynchronously, load reflection uris, then update the UI.
     */
    private class LoadStoryDefAndReflectionUris extends AsyncTask<Void, Integer, Boolean> {

        protected Boolean doInBackground(Void... nothingburger) {
            try {
                pageContentList = loadStoryDef(treasureParentId, getContext());
                loadResponseUris();
                return !pageContentList.isEmpty();
            } catch (StorytellingException e) {
                e.printStackTrace();
                return false;
            }
        }

        protected void onPostExecute(Boolean isSuccessful) {
            if (isSuccessful) {
                // Don't do anything
            } else {
                showErrorMessage(getString(R.string.error_no_internet));
            }
        }

        private List<StoryContent> loadStoryDef(String storyId, Context context)
                throws StorytellingException {
            Storywell storywell = new Storywell(context);
            storywell.loadStoryList(true);
            StoryInterface story = storywell.getStoryManager().getStoryById(storyId);
            ResponseType responseType = story.tryLoadStoryDef(
                    context, storywell.getServer(), storywell.getGroup());

            if (responseType.equals(ResponseType.SUCCESS_202)) {
                return story.getContents();
            } else {
                return new ArrayList<>();
            }

        }
    }

    /**
     * Load reflection URIs.
     */
    private void loadResponseUris() {
        responseManager.getReflectionUrlsFromFirebase(this.reflectionMinEpoch,
                new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                initStoryContentFragments();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // DO NOTHING
            }
        });
    }

    /**
     * Initialize the pager with StoryContents.
     */
    private void initStoryContentFragments() {
        ReflectionsPagerAdapter reflectionsPagerAdapter =
                new ReflectionsPagerAdapter(getChildFragmentManager());

        // Set up the transitions
        CardStackPageTransformer transformer = new CardStackPageTransformer(PAGE_MIN_SCALE);
        viewPager = this.view.findViewById(R.id.container);
        viewPager.setAdapter(reflectionsPagerAdapter);
        viewPager.setPageTransformer(true, transformer);
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
            for (Integer contentId : treasureContents) {
                content = (StoryReflection) pageContentList.get(contentId);
                Fragment fragment = StoryContentAdapter.getFragment(content, getContext());
                fragment.getArguments()
                        .putBoolean(StoryContentAdapter.KEY_CONTENT_ALLOW_EDIT, allowEditContent);
                fragment.getArguments().putString(
                        StoryContentAdapter.KEY_REFLECTION_DATE, formattedDate);
                this.fragments.add(fragment);
            }

            if (content.isNextExists()) {
                StoryContent statementContent = pageContentList.get(content.getNextId());
                this.fragments.add(StoryContentAdapter.getFragment(statementContent, getContext()));
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
     * Load calming prompt contents asynchronously, load reflection uris, then update the UI.
     */
    private void loadCalmingReflectionAndResponseUris() {
        CalmingReflectionViewModel viewModel = ViewModelProviders.of(this)
                .get(CalmingReflectionViewModel.class);

        viewModel.getCalmingReflectionGroup(treasureParentId).observe(
                this, new Observer<CalmingReflectionSet>() {
                    @Override
                    public void onChanged(@Nullable CalmingReflectionSet calmingReflectionSet) {
                        if (calmingReflectionSet != null) {
                            pageContentList = calmingReflectionSet.getContents();
                            loadResponseUris();
                        }
                    }
                });
    }

    /**
     * Show error message.
     * @param msg
     */
    private void showErrorMessage(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }
}
