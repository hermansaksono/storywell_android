package edu.neu.ccs.wellness.storytelling;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import java.util.List;

import edu.neu.ccs.wellness.story.interfaces.StoryInterface;
import edu.neu.ccs.wellness.story.interfaces.StoryType;
import edu.neu.ccs.wellness.story.Story;
import edu.neu.ccs.wellness.server.WellnessRestServer;
import edu.neu.ccs.wellness.storytelling.settings.SynchronizedSetting.StoryListInfo;
import edu.neu.ccs.wellness.storytelling.viewmodel.StoryListViewModel;
import edu.neu.ccs.wellness.storytelling.utils.StoryCoverAdapter;

public class StoryListFragment extends Fragment {
    private StoryListViewModel storyListViewModel;
    private Observer<List<StoryInterface>> storyListObserver;
    private Observer<StoryListInfo> storyMetadataObserver;
    private StoryCoverAdapter storyCoverAdapter;
    private GridView gridview;

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
        View rootView = inflater.inflate(R.layout.fragment_story_list, container, false);
        this.gridview = rootView.findViewById(R.id.storyListGridview);

        this.observeStoryListChanges();

        //Load the detailed story on click on story book
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
                StoryListInfo metadata = storyListViewModel.getNonLiveMetadata();
                storyCoverAdapter = new StoryCoverAdapter(stories, metadata, getContext());
                gridview.setAdapter(storyCoverAdapter);
                observeMetaDataChanges();
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
        // this.doScrollToHighlightedStory();
    }

    /**
     * On fragment pause.
     */
    @Override
    public void onPause() {
        super.onPause();
        this.storyListViewModel.getStories().removeObserver(this.storyListObserver);
        this.storyListViewModel.getMetadata().removeObserver(this.storyMetadataObserver);
    }

    /**
     * Scroll to the highlighted story
     */
    public void doScrollToHighlightedStory() {
        if (this.storyCoverAdapter == null) {
            return;
        } else {
            String highlightedStoryId =
                    storyListViewModel.getMetadata().getValue().getHighlightedStoryId();

            if (!highlightedStoryId.isEmpty()) {
                int position = storyCoverAdapter.getStoryPosition(highlightedStoryId);
                this.scrollToThisStory(position);
                this.storyListViewModel.removeStoryFromHighlight(highlightedStoryId);
            }
        }
    }

    private void scrollToThisStory(int position) {
        if (position >= 0) {
            this.gridview.smoothScrollToPosition(position);
        }
    }

    /**
     * Do the actions when a user taps on one of the item in the story gridview.
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
        getContext().startActivity(intent);
    }

    private void startAboutActivity() {
        Intent intent = new Intent(getContext(), AboutActivity.class);
        getContext().startActivity(intent);
    }

    private void showErrorMessage(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }
}