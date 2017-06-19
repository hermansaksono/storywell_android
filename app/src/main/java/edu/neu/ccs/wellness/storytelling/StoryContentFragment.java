package edu.neu.ccs.wellness.storytelling;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A Fragment to show a simple view of one artwork and one text of the Story.
 */
public class StoryContentFragment extends Fragment {

    public StoryContentFragment() {
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
        TextView tv = (TextView) view.findViewById(R.id.storyText);
        tv.setText(getString(R.string.story_default_text));
    }
}
