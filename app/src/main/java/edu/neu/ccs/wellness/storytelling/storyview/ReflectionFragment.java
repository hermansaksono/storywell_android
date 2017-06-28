package edu.neu.ccs.wellness.storytelling.storyview;

import android.graphics.Typeface;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import edu.neu.ccs.wellness.storytelling.R;
import edu.neu.ccs.wellness.storytelling.models.StoryReflection;

/**
 * A Fragment to show a simple view of one artwork and one text of the Story.
 */
public class ReflectionFragment extends Fragment {
    private static final String STORY_TEXT_FACE = "fonts/pangolin_regular.ttf";
    private static final String KEY_TEXT = "KEY_TEXT";


    public ReflectionFragment() {
    }

    // CONSTRUCTORS
    /**
     * Demo Constructor
     * @param text
     * @return
     */
    public static ReflectionFragment create(String text) {
        ReflectionFragment fragment = new ReflectionFragment();

        Bundle args = new Bundle();
        args.putString(KEY_TEXT, text);
        fragment.setArguments(args);

        return fragment;
    }

    /**
     * Constructor
     * @param page
     * @return
     */
    public static ReflectionFragment create(StoryReflection page) {
        ReflectionFragment fragment = new ReflectionFragment();

        Bundle args = new Bundle();
        args.putString(KEY_TEXT, page.getText());
        fragment.setArguments(args);

        return fragment;
    }

    // PUBLIC METHODS
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reflection_view, container, false);
        String text = getArguments().getString(KEY_TEXT);
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
        TextView itv = (TextView) view.findViewById(R.id.reflectionInstruction);
        TextView tv = (TextView) view.findViewById(R.id.reflectionText);
        TextView stv = (TextView) view.findViewById(R.id.reflectionSubtext);

        itv.setTypeface(tf);
        tv.setTypeface(tf);
        stv.setTypeface(tf);
        tv.setText(text);
    }
}


