package edu.neu.ccs.wellness.storytelling.storyview;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import edu.neu.ccs.wellness.storytelling.R;

/**
 * A Fragment to show a simple view of one artwork and one text of the Story.
 */
public class StoryCoverFragment extends Fragment {
    private static final String STORY_TEXT_FACE = "fonts/pangolin_regular.ttf";
    private static TextView storyTitle;
    private static ImageView storyCover;


    public StoryCoverFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.fragment_story_cover, container, false);
        //setContentText(view, getString(R.string.reflection_text));
        this.storyTitle = (TextView) rootview.findViewById(R.id.text);
        this.storyCover = (ImageView) rootview.findViewById(R.id.storyImage);

        return rootview;
    }

    /***
     * Set View to show the Story's content
     * @param view The View in which the content will be displayed
     * @param text The Story content's text
     */
    private void setContentText(View view, String text) {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), STORY_TEXT_FACE);
        TextView textView = (TextView) view.findViewById(R.id.text);

        textView.setTypeface(tf);
        //stv.setTypeface(tf);
        //tv.setText(text);
    }
}


