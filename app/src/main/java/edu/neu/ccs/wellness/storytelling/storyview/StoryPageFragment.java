package edu.neu.ccs.wellness.storytelling.storyview;

import android.graphics.Typeface;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import edu.neu.ccs.wellness.storytelling.R;

/**
 * A Fragment to show a simple view of one artwork and one text of the Story.
 */
public class StoryPageFragment extends Fragment {
    private static final String STORY_TEXT_FACE = "fonts/pangolin_regular.ttf";


<<<<<<< HEAD:app/src/main/java/edu/neu/ccs/wellness/storytelling/StoryContentFragment.java

    public StoryContentFragment() {
=======
    public StoryPageFragment() {
>>>>>>> 89a817f6c5fa6ffebc9d65063c2c3acb4b78d95f:app/src/main/java/edu/neu/ccs/wellness/storytelling/storyview/StoryPageFragment.java
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_story_view, container, false);
        setContentText(view, getString(R.string.story_default_text));

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
        tv.setText(getString(R.string.story_default_text));
    }
}