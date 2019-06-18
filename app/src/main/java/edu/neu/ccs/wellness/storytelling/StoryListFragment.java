package edu.neu.ccs.wellness.storytelling;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import java.util.List;
import java.util.concurrent.TimeUnit;

import edu.neu.ccs.wellness.server.WellnessRestServer;
import edu.neu.ccs.wellness.story.Story;
import edu.neu.ccs.wellness.story.interfaces.StoryInterface;
import edu.neu.ccs.wellness.story.interfaces.StoryType;
import edu.neu.ccs.wellness.storytelling.settings.SynchronizedSetting.StoryListInfo;
import edu.neu.ccs.wellness.storytelling.sync.MiBandBatteryModel;
import edu.neu.ccs.wellness.storytelling.utils.StoryCoverAdapter;
import edu.neu.ccs.wellness.storytelling.viewmodel.StoryListViewModel;
import edu.neu.ccs.wellness.utils.WellnessDate;

public class StoryListFragment extends Fragment {
    private StoryListViewModel storyListViewModel;
    private Observer<List<StoryInterface>> storyListObserver;
    private Observer<StoryListInfo> storyMetadataObserver;
    private StoryCoverAdapter storyCoverAdapter;
    private View rootView;
    private GridView storyListView;
    private Parcelable storyListState;

    public static StoryListFragment newInstance() {
        return new StoryListFragment();
    }

    /**
     * On fragment creation.
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the StoryList
        this.storyListViewModel = ViewModelProviders.of(this)
                .get(StoryListViewModel.class);
    }

    /**
     * On view creation/
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        WellnessRestServer.configureDefaultImageLoader(container.getContext());
        this.rootView = inflater.inflate(R.layout.fragment_story_list, container, false);
        this.storyListView = rootView.findViewById(R.id.storyListGridview);

        //Load the detailed story on click on story book
        storyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                onStoryClick(position);
            }
        });

        return rootView;
    }

    private void observeStoryListChanges() {
        this.storyListObserver = new Observer<List<StoryInterface>>() {
            @Override
            public void onChanged(@Nullable final List<StoryInterface> stories) {
                if (stories != null) {
                    StoryListInfo metadata = storyListViewModel.getNonLiveMetadata();
                    storyCoverAdapter = new StoryCoverAdapter(stories, metadata, getContext());
                    storyListView.setAdapter(storyCoverAdapter);

                    if (storyListState != null) {
                        storyListView.onRestoreInstanceState(storyListState);
                    }
                    observeMetaDataChanges();
                }
            }
        };
        this.storyListViewModel.getStories().observe(this, this.storyListObserver);
    }

    // INVARIANT: storyListViewModel must be initialized.
    private void observeMetaDataChanges() {
        if (this.storyMetadataObserver == null) {
            this.storyMetadataObserver = new Observer<StoryListInfo>() {
                @Override
                public void onChanged(@Nullable final StoryListInfo metadata) {
                    storyCoverAdapter.setMetadata(metadata);
                    doScrollToHighlightedStory();
                }
            };
        }
        storyListViewModel.getMetadata().observe(this, this.storyMetadataObserver);
    }

    /**
     * On fragment resume.
     */
    @Override
    public void onResume() {
        super.onResume();
        this.observeStoryListChanges();
        this.tryShowAppStartDateToast(this.rootView);
        this.showBatteryLowSnackBar(this.rootView);
    }

    /**
     * On fragment pause.
     */
    @Override
    public void onPause() {
        super.onPause();
        this.storyListState = storyListView.onSaveInstanceState();
        this.storyListViewModel.getStories().removeObserver(this.storyListObserver);
        this.storyListViewModel.getMetadata().removeObserver(this.storyMetadataObserver);
    }

    /**
     * Scroll to the highlighted story
     */
    public void doScrollToHighlightedStory() {
        String highlightedStoryId = storyListViewModel.getMetadata().getValue()
                .getHighlightedStoryId();

        if (!highlightedStoryId.isEmpty()) {
            int position = storyCoverAdapter.getStoryPosition(highlightedStoryId);
            this.scrollToThisStory(position);
            this.storyListViewModel.removeStoryFromHighlight(highlightedStoryId);
        }
    }

    private void scrollToThisStory(int position) {
        if (position >= 0) {
            this.storyListView.smoothScrollToPosition(position);
        }
    }

    /**
     * Do the actions when a user taps on one of the item in the story storyListView.
     * @param position
     */
    private void onStoryClick(int position) {
        StoryInterface story = storyListViewModel.getStories().getValue().get(position);

        if (story.getStoryType() == StoryType.STORY) {
            removeStoryFromUnreadList(story);
            startStoryViewActivity(story);
        } else {
            startAboutActivity();
        }
    }

    private void removeStoryFromUnreadList(StoryInterface story) {
        storyListViewModel.removeStoryFromUnread(story.getId());
    }

    private void startStoryViewActivity(StoryInterface story) {
        Intent intent = new Intent(getContext(), StoryViewActivity.class);
        intent.putExtra(Story.KEY_STORY_ID, story.getId());
        intent.putExtra(Story.KEY_STORY_TITLE, story.getTitle());
        intent.putExtra(Story.KEY_STORY_COVER, story.getCoverUrl());
        intent.putExtra(Story.KEY_STORY_DEF, story.getDefUrl());
        intent.putExtra(Story.KEY_STORY_IS_CURRENT, true);
        intent.putExtra(Story.KEY_STORY_IS_LOCKED, story.isLocked());
        intent.putExtra(Story.KEY_STORY_NEXT_STORY_ID, story.getNextStoryId());
        //getContext().startActivity(intent);
        getActivity().startActivityForResult(intent, HomeActivity.CODE_STORYVIEW_RESULT);
    }

    private void startAboutActivity() {
        Intent intent = new Intent(getContext(), AboutActivity.class);
        getContext().startActivity(intent);
    }

    private void tryShowAppStartDateToast(View view) {
        long now = WellnessDate.getBeginningOfDay().getTimeInMillis();
        long startTime = new Storywell(getContext()).getSynchronizedSetting().getAppStartDate();

        long timeDiff = startTime - now;

        if (timeDiff > 0) {
            int days = (int) TimeUnit.DAYS.convert(timeDiff, TimeUnit.MILLISECONDS);
            showAppStartDateToast(days, view);
        }
    }

    private void showAppStartDateToast(int days, View view) {
        String dayText = getResources().getQuantityString(
                R.plurals.home_info_app_start_date, days, days);
        String text = getString(R.string.home_info_app_start_date, dayText);
        Snackbar.make(view, text, Snackbar.LENGTH_INDEFINITE)
                .show();
    }

    private void showBatteryLowSnackBar(View view) {
        MiBandBatteryModel miBandBatteryModel = new MiBandBatteryModel(view.getContext());
        String adultName = miBandBatteryModel.getCaregiverName();
        String childName = miBandBatteryModel.getChildName();

        boolean isAdultLow = miBandBatteryModel.isCaregiverBatteryLevelLow();
        boolean isChildLow = miBandBatteryModel.isChildBatteryLevelLow();

        if (isAdultLow && isChildLow) {
            String text = getString(R.string.home_info_people_battery_low, adultName, childName);
            Snackbar.make(view, text, Snackbar.LENGTH_LONG).show();
        } else if (isAdultLow) {
            String text = getString(R.string.home_info_person_battery_low, adultName);
            Snackbar.make(view, text, Snackbar.LENGTH_LONG).show();
        } else if (isChildLow) {
            String text = getString(R.string.home_info_person_battery_low, childName);
            Snackbar.make(view, text, Snackbar.LENGTH_LONG).show();
        }
    }

    private void showErrorMessage(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }
}