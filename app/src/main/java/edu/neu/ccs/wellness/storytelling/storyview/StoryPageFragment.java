package edu.neu.ccs.wellness.storytelling.storyview;

import android.graphics.Typeface;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import edu.neu.ccs.wellness.storytelling.R;
import edu.neu.ccs.wellness.storytelling.models.StoryPage;

/**
 * A Fragment to show a simple view of one artwork and one text of the Story.
 */
public class StoryPageFragment extends Fragment {
    private static final String STORY_TEXT_FACE = "fonts/pangolin_regular.ttf";

    // CONSTRUCTORS
    /**
     * Constructor
     * @param page
     * @return
     */
    public static StoryPageFragment create(StoryPage page) {
        StoryPageFragment fragment = new StoryPageFragment();

        Bundle args = new Bundle();
        args.putString(StoryContentAdapter.KEY_TEXT, page.getText());
        fragment.setArguments(args);

        return fragment;
    }

    public static StoryPageFragment create(String text) {
        StoryPageFragment fragment = new StoryPageFragment();

        Bundle args = new Bundle();
        args.putString(StoryContentAdapter.KEY_TEXT, text);
        fragment.setArguments(args);

        return fragment;
    }

    // PUBLIC METHODS
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_story_view, container, false);

        String imageUrl = getArguments().getString(StoryContentAdapter.KEY_IMG_URL); // TODO Bahar
        String text = getArguments().getString(StoryContentAdapter.KEY_TEXT);
        setContentText(view, text);

        return view;
    }

    /***
     * Set View to show the Story's content
     * @param view The View in which the content will be displayed
     * @param text The Story content's text
     */
    private void setContentText(View view, String text) {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), STORY_TEXT_FACE);
        TextView tv = (TextView) view.findViewById(R.id.storyText);
        tv.setTypeface(tf);
        tv.setText(text);
    }
}
