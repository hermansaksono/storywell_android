package edu.neu.ccs.wellness.storytelling;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import edu.neu.ccs.wellness.storytelling.interfaces.StorytellingException;
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

    private View sitem;
    private TextView tv;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_story_list, container, false);
        View oneStory = rootView.findViewById(R.id.story_item1);

        oneStory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), StoryViewActivity.class);
                startActivity(intent);
            }
        });

        this.user = new WellnessUser("family01", "tacos001");
        this.server = new WellnessRestServer(WELLNESS_SERVER_URL, 0, STORY_API_PATH, user);
        this.storyManager = StoryManager.create(server);

        //tv.setText("Loading...");
        new AsyncLoadStoryList(container.getContext()).execute();

        return rootView;
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
            try {
                Log.d("WELL", storyManager.getStoryById(1).getTitle());
                //tv.setText(storyManager.getStoryById(1).getTitle());
            } catch (StorytellingException e) {
                e.printStackTrace();
            }
        }

    }
}