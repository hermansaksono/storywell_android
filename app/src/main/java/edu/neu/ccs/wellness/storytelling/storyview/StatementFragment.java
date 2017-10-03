package edu.neu.ccs.wellness.storytelling.storyview;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import edu.neu.ccs.wellness.storytelling.R;
import edu.neu.ccs.wellness.storytelling.StoryViewActivity;

/**
 * A Fragment to show a simple view of one artwork and one text of the Story.
 */
public class StatementFragment extends Fragment {
    public StatementFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statement_view, container, false);

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
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(),
                StoryViewActivity.STORY_TEXT_FACE);
        TextView tv = (TextView) view.findViewById(R.id.text);
        TextView stv = (TextView) view.findViewById(R.id.subtext);

        tv.setTypeface(tf);
        tv.setText(text);

        stv.setTypeface(tf);
    }
}


