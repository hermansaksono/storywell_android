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
 * A Fragment to show a simple view of one artwork and one text of the Story.
 */
public class StatementFragment extends Fragment {
    private static final String STORY_TEXT_FACE = "fonts/pangolin_regular.ttf";


    public StatementFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statement_view, container, false);
        setContentText(view, getString(R.string.statement_text));

        return view;
    }

    /***
     * Set View to show the Story's content
     * @param view The View in which the content will be displayed
     * @param text The Story content's text
     */
    private void setContentText(View view, String text) {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), STORY_TEXT_FACE);
        TextView textView = (TextView) view.findViewById(R.id.text);
        TextView instructionView = (TextView) view.findViewById(R.id.subtext);

        textView.setTypeface(tf);
        instructionView.setTypeface(tf);
        textView.setText(text);
    }
}


