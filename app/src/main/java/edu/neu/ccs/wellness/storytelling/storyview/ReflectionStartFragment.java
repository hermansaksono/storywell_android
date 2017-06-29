package edu.neu.ccs.wellness.storytelling.storyview;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import edu.neu.ccs.wellness.storytelling.R;
import edu.neu.ccs.wellness.utils.OnGoToFragmentListener;
import edu.neu.ccs.wellness.utils.OnGoToFragmentListener.TransitionType;

/**
 * A Fragment to show a simple view of one artwork and one text of the Story.
 */
public class ReflectionStartFragment extends Fragment {
    private static final String STORY_TEXT_FACE = "fonts/pangolin_regular.ttf";

    private OnGoToFragmentListener mOnGoToFragmentListener;

    public ReflectionStartFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reflection_start, container, false);
        View buttonReflectionStart = view.findViewById(R.id.buttonReflectionStart);

        buttonReflectionStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnGoToFragmentListener.onGoToFragment(TransitionType.ZOOM_OUT, 1);
            }
        });

        setContentText(view, "");

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mOnGoToFragmentListener = (OnGoToFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(((Activity) context).getLocalClassName()
                    + " must implement OnReflectionBeginListener");
        }
    }

    /***
     * Set View to show the Story's content
     * @param view The View in which the content will be displayed
     * @param text The Story content's text
     */
    private void setContentText(View view, String text) {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), STORY_TEXT_FACE);
        TextView textView = (TextView) view.findViewById(R.id.text);
        TextView subtextView = (TextView) view.findViewById(R.id.subtext);

        textView.setTypeface(tf);
        subtextView.setTypeface(tf);
    }
}


