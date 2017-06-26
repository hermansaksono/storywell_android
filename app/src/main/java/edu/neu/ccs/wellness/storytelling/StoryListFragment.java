package edu.neu.ccs.wellness.storytelling;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import edu.neu.ccs.wellness.storytelling.interfaces.RestServer.ResponseType;
import edu.neu.ccs.wellness.storytelling.interfaces.StoryInterface;
import edu.neu.ccs.wellness.storytelling.interfaces.StorytellingException;
import edu.neu.ccs.wellness.storytelling.models.StoryManager;
import edu.neu.ccs.wellness.storytelling.models.WellnessRestServer;
import edu.neu.ccs.wellness.storytelling.models.WellnessUser;
import edu.neu.ccs.wellness.storytelling.utils.ImageAdapter;

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
    private static final String STORYLIST_FONT = "fonts/pangolin_regular.ttf";

    private StoryManager storyManager;
    private WellnessUser user;
    private WellnessRestServer server;
    private List<StoryInterface> stories;

    private GridView gridview;
    private LayoutInflater inflater;
    private ViewGroup container;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_story_list, container, false);
        this.gridview = (GridView) rootView.findViewById(R.id.gridview);
        this.inflater = inflater;
        this.container = container;
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
                updateStoryById(1);
                Log.d("WELL", "Story list loading successful");
                stories = storyManager.getStoryList();
                List<View> viewList = initViewList();
                renderStories(viewList);
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

    // PRIVATE METHODS
    private void setTextViewTypeface(TextView tv, String fontAsset) {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), fontAsset);
        tv.setTypeface(tf);
    }

    private List<View> initViewList() {
        List<View> viewList = new ArrayList<>();
        for (StoryInterface s : this.stories) {
            View book = inflater.inflate(R.layout.booklayout_storylist, container, false);
            viewList.add(book);
        }
        return viewList;
    }

    private List<View> storyListToViewList(List<StoryInterface> stories, List<View> viewList) {
        List<View> ret = new ArrayList<>();
        for (int i = 0 ; i < stories.size(); i++) {
            View view = viewList.get(i);
            StoryInterface story = stories.get(i);
            ImageView imageView = (ImageView) view.findViewById(R.id.imageview_cover_art);
            ViewGroup.LayoutParams lp = imageView.getLayoutParams();
            lp.width = 85;
            lp.height = 85;
            imageView.requestLayout();
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
            //TODO make this asynchronous
            this.server.getImage(EXAMPLE_IMAGE_RESOURCE, imageView, EXAMPLE_IMAGE_FILENAME,
                    this.getContext(), 500, 500);
            TextView textView = (TextView) view.findViewById(R.id.textview_book_name);
            textView.setText(story.getTitle());
            setTextViewTypeface(textView, STORYLIST_FONT);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(view.getContext(), StoryViewActivity.class);
                    startActivity(intent);
                }
            });
            ret.add(view);
        }
        return ret;
    }

    private void renderStories(List<View> viewList) {
        gridview.setAdapter(new ImageAdapter(getContext(), storyListToViewList(stories, viewList)));
    }
}