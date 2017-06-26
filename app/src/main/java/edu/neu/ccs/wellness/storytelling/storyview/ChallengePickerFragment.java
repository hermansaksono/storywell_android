package edu.neu.ccs.wellness.storytelling.storyview;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import edu.neu.ccs.wellness.storytelling.R;

/**
 * Created by hermansaksono on 6/25/17.
 */

public class ChallengePickerFragment extends Fragment {
    private static final String STORY_TEXT_FACE = "fonts/pangolin_regular.ttf";

    public ChallengePickerFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_challenge_picker, container, false);
        setContentText(view, getString(R.string.reflection_text));

        return view;
    }

    /***
     * Set View to show the Challenge's content
     * @param view The View in which the content will be displayed
     * @param text The Story content's text
     */
    private void setContentText(View view, String text) {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), STORY_TEXT_FACE);
        TextView heading = (TextView) view.findViewById(R.id.text);
        TextView subheading = (TextView) view.findViewById(R.id.subtext);

        heading.setTypeface(tf);
        subheading.setTypeface(tf);
    }
}
