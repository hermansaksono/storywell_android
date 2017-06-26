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

public class ChallengeInfoFragment extends Fragment {
    private static final String STORY_TEXT_FACE = "fonts/pangolin_regular.ttf";

    public ChallengeInfoFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_challenge_info, container, false);
        setContentText(view, getString(R.string.challenge_info_title),
                getString(R.string.challenge_info_subtitle));

        return view;
    }

    /***
     * Set View to show the Challenge's content
     * @param view The View in which the content will be displayed
     * @param text The Story content's text
     */
    private void setContentText(View view, String text, String subtext) {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), STORY_TEXT_FACE);
        TextView heading = (TextView) view.findViewById(R.id.text);
        TextView subheading = (TextView) view.findViewById(R.id.subtext);

        heading.setTypeface(tf);
        subheading.setTypeface(tf);

        heading.setText(text);
        subheading.setText(subtext);
    }
}