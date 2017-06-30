package edu.neu.ccs.wellness.storytelling;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Toast;

import java.util.List;

import edu.neu.ccs.wellness.storytelling.interfaces.RestServer.ResponseType;
import edu.neu.ccs.wellness.storytelling.interfaces.StoryInterface;
import edu.neu.ccs.wellness.storytelling.interfaces.StorytellingException;
import edu.neu.ccs.wellness.storytelling.models.StoryManager;
import edu.neu.ccs.wellness.storytelling.models.WellnessRestServer;
import edu.neu.ccs.wellness.storytelling.models.WellnessUser;
import edu.neu.ccs.wellness.storytelling.utils.StoryCoverAdapter;

/**
 * Created by hermansaksono on 6/14/17.
 */

public class StoryListFragment extends Fragment {

    public static final String WELLNESS_SERVER_URL = "http://wellness.ccs.neu.edu/";
    public static final String STORY_API_PATH = "storytelling_dev/api/";
    private static final String ERR_NO_INTERNET = "Cannot connect to the Internet";
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
        View rootView = inflater.inflate(R.layout.fragment_story_list, container, false);
        this.gridview = (GridView) rootView.findViewById(R.id.gridview);
        this.user = new WellnessUser("family01", "tacos001");
        this.server = new WellnessRestServer(WELLNESS_SERVER_URL, 0, STORY_API_PATH, user);
        this.storyManager = StoryManager.create(server);
        new AsyncLoadStoryList(container.getContext()).execute();
        return rootView;
    }

    // PRIVATE ASYNCTASK CLASSES
    private class AsyncLoadStoryList extends AsyncTask<Void, Integer, ResponseType> {
        Context context;

        public AsyncLoadStoryList(Context context) { this.context = context; }

        protected ResponseType doInBackground(Void... voids) {
            if (storyManager.canAccessServer(this.context) == false) {
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
                Toast.makeText(context, ERR_NO_INTERNET, Toast.LENGTH_SHORT).show();
            }
            else if (result == ResponseType.SUCCESS_202) {
                //updateStoryById(1);
                Log.d("WELL", "Story list loading successful");
                stories = storyManager.getStoryList();
                gridview.setAdapter(new StoryCoverAdapter(getContext(), stories));
            }
        }

    }


    // STUPID DUMMY PRIVATE METHODS
    private void updateStoryById(int id) {
        try {
            StoryInterface story = this.storyManager.getStoryById(1);
        } catch (StorytellingException e) {
            e.printStackTrace();
        }
    }
}