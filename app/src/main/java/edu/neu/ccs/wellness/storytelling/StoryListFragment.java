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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.neu.ccs.wellness.story.interfaces.StoryInterface;
import edu.neu.ccs.wellness.story.interfaces.StoryType;
import edu.neu.ccs.wellness.story.Story;
import edu.neu.ccs.wellness.server.WellnessRestServer;
import edu.neu.ccs.wellness.storytelling.viewmodel.StoryListViewModel;
import edu.neu.ccs.wellness.storytelling.utils.StoryCoverAdapter;
import edu.neu.ccs.wellness.tracking.Event;
import edu.neu.ccs.wellness.tracking.Param;
import edu.neu.ccs.wellness.tracking.UserTrackDetails;
import edu.neu.ccs.wellness.tracking.WellnessUserTracking;

public class StoryListFragment extends Fragment {
    private StoryListViewModel storyListViewModel;
    private GridView gridview;
    private Storywell storywell;
    private WellnessUserTracking wellnessUserTracking;
    private Bundle eventParams;

    public static StoryListFragment newInstance() {
        return new StoryListFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        WellnessRestServer.configureDefaultImageLoader(container.getContext());
        View rootView = inflater.inflate(R.layout.fragment_story_list, container, false);
        this.gridview = rootView.findViewById(R.id.gridview);
        //this.storywell = new Storywell(this.getContext());

        // Load the StoryList
        this.storyListViewModel = ViewModelProviders.of(this).get(StoryListViewModel.class);
        storyListViewModel.getStories().observe(this, new Observer<List<StoryInterface>>() {
            @Override
            public void onChanged(@Nullable final List<StoryInterface> stories) {
                gridview.setAdapter(new StoryCoverAdapter(getContext(), stories));
            }
        });


        //Load the detailed story on click on story book
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                onStoryClick(position);
            }
        });

        storywell = new Storywell(getActivity().getApplicationContext());

        //TODO userId in WellnessUser ??
        wellnessUserTracking = storywell.getUserTracker("108");
        eventParams = new Bundle();
        eventParams.putString(Param.FRAGMENT_NAME, "StoryListFragment");
        wellnessUserTracking.logEvent(Event.FRAGMENT_OPEN, eventParams);

        return rootView;
    }

    /* PRIVATE METHODS */
    private void onStoryClick(int position) {
        StoryInterface story = storyListViewModel.getStories().getValue().get(position);
        wellnessUserTracking = storywell.getUserTracker("108");
        eventParams = new Bundle();
        eventParams.putString(Param.CONTENT_TYPE, "Story");
        wellnessUserTracking.logEvent(Event.CONTENT_SELECT, eventParams);

        if (story.getStoryType() == StoryType.STORY) {
            startStoryViewActivity(story);
        } else {
            startAboutActivity();
        }
    }

    private void startStoryViewActivity(StoryInterface story) {
        Intent intent = new Intent(getContext(), StoryViewActivity.class);
        intent.putExtra(Story.KEY_STORY_ID, story.getId());
        intent.putExtra(Story.KEY_STORY_TITLE, story.getTitle());
        intent.putExtra(Story.KEY_STORY_COVER, story.getCoverUrl());
        intent.putExtra(Story.KEY_STORY_DEF, story.getDefUrl());
        intent.putExtra(Story.KEY_STORY_IS_CURRENT, true);
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