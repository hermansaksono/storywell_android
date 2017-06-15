package edu.neu.ccs.wellness.storytelling;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import edu.neu.ccs.wellness.storytelling.models.StoryManager;
import edu.neu.ccs.wellness.storytelling.models.WellnessRestServer;
import edu.neu.ccs.wellness.storytelling.models.WellnessUser;

/**
 * Created by hermansaksono on 6/14/17.
 */

public class StoryListFragment extends Fragment {

    public static final String WELLNESS_SERVER_URL = "http://wellness.ccs.neu.edu/";
    public static final String STORY_API_PATH = "storytelling_dev/api/";

    private StoryManager storyManager;
    private WellnessUser user;
    private WellnessRestServer server;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.user = new WellnessUser("family01", "tacos001");
        this.server = new WellnessRestServer(WELLNESS_SERVER_URL, 0, STORY_API_PATH, user);
        this.storyManager = StoryManager.create(server);
        //getSharedPreferences(StoryManager.PREFS_NAME, 0);
        new AsyncLoadStoryList(getActivity()).execute();
    }


    // PRIVATE METHODS

    // PRIVATE ASYNCTASK CLASSES
    private class AsyncLoadStoryList extends AsyncTask<Void, Integer, Void> {
        Context context;

        public AsyncLoadStoryList(Context context) { this.context = context; }

        protected Void doInBackground(Void... voids) {
            if (storyManager.isStoryListSet() != true) {
                storyManager.loadStoryList(this.context);
            }
            return null;
        }

        protected void onPostExecute(Void result) {

        }

    }
}