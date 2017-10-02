package edu.neu.ccs.wellness.storytelling;

import android.content.Context;
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

import edu.neu.ccs.wellness.storytelling.interfaces.RestServer.ResponseType;
import edu.neu.ccs.wellness.storytelling.interfaces.StoryInterface;
import edu.neu.ccs.wellness.storytelling.models.Story;
import edu.neu.ccs.wellness.storytelling.models.StoryManager;
import edu.neu.ccs.wellness.storytelling.models.WellnessRestServer;
import edu.neu.ccs.wellness.storytelling.models.WellnessUser;
import edu.neu.ccs.wellness.storytelling.utils.StoryCoverAdapter;

/**
 * Created by hermansaksono on 6/14/17.
 */

public class StoryListFragment extends Fragment {
    private static final String STATIC_API_PATH = "story_static/";
    private static final String EXAMPLE_IMAGE_RESOURCE = "temp/story0_pg0.png";
    private static final String EXAMPLE_IMAGE_FILENAME = "story0page0";

    private StoryManager storyManager;
    private WellnessUser user;
    private WellnessRestServer server;
    private List<StoryInterface> stories;

    private GridView gridview;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        WellnessRestServer.configureDefaultImageLoader(container.getContext());
        View rootView = inflater.inflate(R.layout.fragment_story_list, container, false);
        this.gridview = (GridView) rootView.findViewById(R.id.gridview);
        this.loadStoryList();
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                startStoryViewActivity(position);
            }
        });

        return rootView;
    }

    // PRIVATE ASYNCTASK CLASSES
    private class AsyncLoadStoryList extends AsyncTask<Void, Integer, ResponseType> {
        Context context;

        public AsyncLoadStoryList(Context context) { this.context = context; }

        protected ResponseType doInBackground(Void... voids) {
            if (server.isOnline(getContext()) == false) {
                return ResponseType.NO_INTERNET;
            }
            else if (storyManager.isStoryListSet() != true) {
                storyManager.loadStoryList(this.context);
                return ResponseType.SUCCESS_202;
            }
            return null;
        }

        protected void onPostExecute(ResponseType result) {
            if (result == ResponseType.NO_INTERNET) {
                showErrorMessage(getString(R.string.error_no_internet));
            }
            else if (result == ResponseType.SUCCESS_202) {
                stories = storyManager.getStoryList();
                gridview.setAdapter(new StoryCoverAdapter(getContext(), stories));
            }
        }

    }

    // PRIVATE METHODS
    private void loadStoryList () {
        this.user = new WellnessUser(WellnessRestServer.DEFAULT_USER,
                WellnessRestServer.DEFAULT_PASS);
        this.server = new WellnessRestServer(WellnessRestServer.WELLNESS_SERVER_URL, 0,
                WellnessRestServer.STORY_API_PATH, user);
        this.storyManager = StoryManager.create(server);
        new AsyncLoadStoryList(getContext()).execute();
    }

    private void startStoryViewActivity(int position) {
        StoryInterface story = stories.get(position);

        Intent intent = new Intent(getContext(), StoryViewActivity.class);
        intent.putExtra(Story.KEY_STORY_ID, story.getId());
        intent.putExtra(Story.KEY_STORY_TITLE, story.getTitle());
        intent.putExtra(Story.KEY_STORY_COVER, story.getCoverUrl());
        intent.putExtra(Story.KEY_STORY_DEF, story.getDefUrl());
        intent.putExtra(Story.KEY_STORY_IS_CURRENT, true);
        getContext().startActivity(intent);
    }

    private void showErrorMessage(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }
}