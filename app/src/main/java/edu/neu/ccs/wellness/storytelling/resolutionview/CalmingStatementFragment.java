package edu.neu.ccs.wellness.storytelling.resolutionview;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import edu.neu.ccs.wellness.story.interfaces.StoryContent;
import edu.neu.ccs.wellness.storytelling.R;
import edu.neu.ccs.wellness.storytelling.utils.StoryContentAdapter;

/**
 * Created by hermansaksono on 3/4/19.
 */

public class CalmingStatementFragment extends Fragment {

    private StoryUnlockListener unlockListener;
    private View view;

    /* FACTORY METHOD */
    public static Fragment newInstance(StoryContent content) {
        Fragment fragment = new CalmingStatementFragment();
        Bundle args = new Bundle();
        args.putInt(StoryContentAdapter.KEY_ID, content.getId());
        args.putString(StoryContentAdapter.KEY_IMG_URL, "");
        args.putString(StoryContentAdapter.KEY_TEXT, content.getText());
        args.putString(StoryContentAdapter.KEY_SUBTEXT, content.getSubtext());
        args.putBoolean(StoryContentAdapter.KEY_IS_LOCKED, false);
        args.putBoolean(StoryContentAdapter.KEY_IS_ACTIONABLE, true);
        fragment.setArguments(args);
        return fragment;
    }

    // PUBLIC METHODS
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        this.view = inflater.inflate(R.layout.fragment_calming_memo, container, false);
        Button actionButton = view.findViewById(R.id.action_button);

        setContentText(
                view,
                getArguments().getString(StoryContentAdapter.KEY_TEXT),
                getArguments().getString(StoryContentAdapter.KEY_SUBTEXT));
        setActionButtonVisibilityAndListener(actionButton);

        return view;
    }

    private void setActionButtonVisibilityAndListener(Button actionButton) {
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unlockListener.unlockStory(view);
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            unlockListener = (StoryUnlockListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(((Activity) context).getLocalClassName()
                    + " must implement StoryUnlockListener");
        }
    }

    /***
     * Set View to show the Story's content
     * @param view The View in which the content will be displayed
     * @param text The Page's text contents
     */
    private void setContentText(View view, String text, String subtext) {

        TextView tv = view.findViewById(R.id.text);

        tv.setText(text);

        tv = view.findViewById(R.id.subtext);

        tv.setText(subtext);

    }
}