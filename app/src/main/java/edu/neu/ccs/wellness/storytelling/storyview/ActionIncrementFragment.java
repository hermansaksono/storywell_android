package edu.neu.ccs.wellness.storytelling.storyview;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import edu.neu.ccs.wellness.storytelling.R;
import edu.neu.ccs.wellness.storytelling.Storywell;

/**
 * Created by hermansaksono on 1/28/19.
 */

public class ActionIncrementFragment extends Fragment {

    // PUBLIC METHODS
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_story_action_increment,
                container, false);
        Button actionButton = view.findViewById(R.id.action_button);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setIterationToIncrement();
            }
        });

        return view;
    }

    private void setIterationToIncrement() {
        Storywell storywell = new Storywell(getContext());
        storywell.incrementReflectionIteration();
    }
}
