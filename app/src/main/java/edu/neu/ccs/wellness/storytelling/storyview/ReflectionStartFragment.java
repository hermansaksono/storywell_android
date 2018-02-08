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
import edu.neu.ccs.wellness.storytelling.StoryViewActivity;
import edu.neu.ccs.wellness.storytelling.utils.OnGoToFragmentListener;
import edu.neu.ccs.wellness.storytelling.utils.OnGoToFragmentListener.TransitionType;

/**
 * A Fragment to show a simple view of one artwork and one text of the Story.
 */
public class ReflectionStartFragment extends Fragment {
    private OnGoToFragmentListener onGoToFragmentListener;

    public ReflectionStartFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reflection_start, container, false);
        View buttonReflectionStart = view.findViewById(R.id.buttonReflectionStart);

        buttonReflectionStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onGoToFragmentListener.onGoToFragment(TransitionType.ZOOM_OUT, 1);
            }
        });

        setContentText(view, getArguments().getString("KEY_TEXT"),
                getArguments().getString("KEY_SUBTEXT"));
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            onGoToFragmentListener = (OnGoToFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(((Activity) context).getLocalClassName()
                    + " must implement OnReflectionBeginListener");
        }
    }

    /***
     * Set View to show the Story's content
     * @param view The View in which the content will be displayed
     * @param text The Reflection start's text
     * @param subtext The Reflection start's subtext
     */
    private void setContentText(View view, String text, String subtext) {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(),
                StoryViewActivity.STORY_TEXT_FACE);
        TextView tv = (TextView) view.findViewById(R.id.text);
        TextView stv = (TextView) view.findViewById(R.id.subtext);

        tv.setText(text);
        tv.setTypeface(tf);

        stv.setText(subtext);
        stv.setTypeface(tf);
    }
}