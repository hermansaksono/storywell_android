package edu.neu.ccs.wellness.storytelling;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import java.util.List;

import edu.neu.ccs.wellness.server.RestServer.ResponseType;
import edu.neu.ccs.wellness.storytelling.interfaces.StoryInterface;
import edu.neu.ccs.wellness.storytelling.interfaces.StoryType;
import edu.neu.ccs.wellness.storytelling.models.Story;
import edu.neu.ccs.wellness.server.WellnessRestServer;
import edu.neu.ccs.wellness.utils.StoryCoverAdapter;

public class StoryListFragment extends Fragment {
    //Keep a track of the story clicked
    public static int storyIdClicked = -1; // TODO static public variable is not a good practice

    private Storywell storywell;
    private GridView gridview;

    public static StoryListFragment newInstance() {
        return new StoryListFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        WellnessRestServer.configureDefaultImageLoader(container.getContext());
        View rootView = inflater.inflate(R.layout.fragment_story_list, container, false);
        this.gridview = (GridView) rootView.findViewById(R.id.gridview);
        this.storywell = new Storywell(this.getContext());

        new LoadStoryListAsync().execute();

        //Load the detailed story on click on story book
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                onStoryClick(position);
            }
        });

        return rootView;
    }

    /* ASYNCTASKS */
    private class LoadStoryListAsync extends AsyncTask<Void, Integer, ResponseType> {

        protected ResponseType doInBackground(Void... voids) {
            if (storywell.isServerOnline()) {
                storywell.loadStoryList();
                return ResponseType.SUCCESS_202;
            } else {
                return ResponseType.NO_INTERNET;
            }
        }

        protected void onPostExecute(ResponseType result) {
            Log.d("WELL Story list d/l", result.toString());
            if (result == ResponseType.SUCCESS_202) {
                List<StoryInterface> stories = storywell.getStoryList();
                gridview.setAdapter(new StoryCoverAdapter(getContext(), stories));
            } else if (result == ResponseType.NO_INTERNET) {
                showErrorMessage(getString(R.string.error_no_internet));
            }
        }

    }

    /* PRIVATE METHODS */
    private void onStoryClick(int position) {
        StoryInterface story = storywell.getStoryList().get(position);
        if (story.getStoryType() == StoryType.STORY) {
            startStoryViewActivity(story);
            storyIdClicked = position;
        } else {
            startAboutAcitivity();
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

    private void startAboutAcitivity() {
        Intent intent = new Intent(getContext(), AboutActivity.class);
        getContext().startActivity(intent);
    }

    private void showErrorMessage(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }
}