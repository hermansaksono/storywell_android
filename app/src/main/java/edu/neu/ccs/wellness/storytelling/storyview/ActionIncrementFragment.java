package edu.neu.ccs.wellness.storytelling.storyview;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import edu.neu.ccs.wellness.storytelling.HomeActivity;
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
                showIncrementDialog();
            }
        });

        return view;
    }

    private void showIncrementDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setTitle(R.string.dialog_relock_stories_title);
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                setIterationToIncrement();
            }
        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void setIterationToIncrement() {
        Storywell storywell = new Storywell(getContext());
        storywell.incrementReflectionIteration();
        Intent data = new Intent();

        data.putExtra(HomeActivity.RESULT_CODE, HomeActivity.RESULT_RESET_STORY_STATES);

        getActivity().setResult(Activity.RESULT_OK, data);
        getActivity().finish();
    }
}
