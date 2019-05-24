package edu.neu.ccs.wellness.storytelling.resolutionview;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
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

import edu.neu.ccs.wellness.logging.WellnessUserLogging;
import edu.neu.ccs.wellness.reflection.CalmingManager;
import edu.neu.ccs.wellness.reflection.ResponseManager;
import edu.neu.ccs.wellness.reflection.TreasureItem;
import edu.neu.ccs.wellness.reflection.TreasureItemType;
import edu.neu.ccs.wellness.story.CalmingReflectionSet;
import edu.neu.ccs.wellness.story.StoryReflection;
import edu.neu.ccs.wellness.story.StoryStatement;
import edu.neu.ccs.wellness.story.interfaces.StoryContent;
import edu.neu.ccs.wellness.storytelling.R;
import edu.neu.ccs.wellness.storytelling.Storywell;
import edu.neu.ccs.wellness.storytelling.resolutionview.CalmingStatementFragment;
import edu.neu.ccs.wellness.storytelling.storyview.ReflectionFragment;
import edu.neu.ccs.wellness.storytelling.utils.OnGoToFragmentListener;
import edu.neu.ccs.wellness.storytelling.utils.StoryContentAdapter;
import edu.neu.ccs.wellness.storytelling.utils.UserLogging;
import edu.neu.ccs.wellness.storytelling.viewmodel.CalmingReflectionViewModel;
import edu.neu.ccs.wellness.utils.CardStackPageTransformer;

/**
 * Created by hermansaksono on 1/17/19.
 */

public class CalmingViewFragment extends Fragment implements
        OnGoToFragmentListener,
        ReflectionFragment.ReflectionFragmentListener {

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
        this.view = inflater.inflate(R.layout.fragment_reflection_view, container, false);
        this.viewPager = this.view.findViewById(R.id.container);
        this.storywell = new Storywell(getContext());
        this.groupName = this.storywell.getGroup().getName();
        this.reflectionIteration = this.storywell.getReflectionIteration();
        this.reflectionMinEpoch = this.storywell.getReflectionIterationMinEpoch();

        Bundle bundle = getArguments();
        this.treasureParentType = TreasureItemType.CALMING_PROMPT;
        this.treasureParentId = bundle.getString(TreasureItem.KEY_PARENT_ID);
        this.treasureContents = bundle.getIntegerArrayList(TreasureItem.KEY_CONTENTS);
        this.allowEditContent = bundle.getBoolean(StoryContentAdapter.KEY_CONTENT_ALLOW_EDIT,
                false);
        this.formattedDate = getFormattedDate(
                bundle.getLong(TreasureItem.KEY_LAST_UPDATE_TIMESTAMP,0));

        this.responseManager = new CalmingManager (
                groupName, treasureParentId, reflectionIteration, reflectionMinEpoch, getContext());

        this.loadCalmingReflectionAndResponseUris();
        UserLogging.logViewTreasure(this.treasureParentId, this.treasureContents.get(0));
        return this.view;
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
     * Load reflection URIs.
     */
    private void loadResponseUris() {
        this.responseManager.getReflectionUrlsFromFirebase(this.reflectionMinEpoch,
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
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int pos, float offset, int positionOffsetPixels) {
                if (offset > 0.95) {
                    doStopPlay();
                }
            }

            @Override
            public void onPageSelected(int position) {
                uploadReflectionAudio();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private boolean uploadReflectionAudio() {
        if (this.responseManager.isUploadQueued()) {
            new AsyncUploadAudio().execute();
            return true;
        } else {
            return false;
        }
    }

    public class AsyncUploadAudio extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            responseManager.uploadReflectionAudioToFirebase();
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
                StoryStatement statementContent =
                        (StoryStatement) pageContentList.get(content.getNextId());
                Fragment statementFragment = CalmingStatementFragment.newInstance(statementContent);
                this.fragments.add(statementFragment);
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
     * Show error message.
     * @param msg
     */
    private void showErrorMessage(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }
}
