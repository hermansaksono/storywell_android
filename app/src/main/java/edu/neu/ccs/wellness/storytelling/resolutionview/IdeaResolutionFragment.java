package edu.neu.ccs.wellness.storytelling.resolutionview;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.CycleInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewAnimator;

import java.util.Random;

import edu.neu.ccs.wellness.storytelling.R;
import edu.neu.ccs.wellness.storytelling.settings.SynchronizedSettingRepository;
import edu.neu.ccs.wellness.storytelling.utils.UserLogging;

/**
 * Created by hermansaksono on 4/6/19.
 */

public class IdeaResolutionFragment extends Fragment {

    private static final int IDEA_VIEW_INTRO = 0;
    private static final int IDEA_VIEW_PICKER = 1;
    private static final int IDEA_VIEW_CONCLUSION = 2;
    private static final int[] IDEA_ARRAYS = new int[] {
        R.array.pa_obstacles_solution_1,
        R.array.pa_obstacles_solution_2,
        R.array.pa_obstacles_solution_3,
        R.array.pa_obstacles_solution_4,
        R.array.pa_obstacles_solution_5
    };

    private boolean isDemoMode;
    private View rootView;
    private ViewAnimator viewAnimator;
    private ListView obstaclesList;
    private GridLayout ideaBalloonsGrid;
    private Button unlockButton;
    private StoryUnlockListener storyUnlockListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.isDemoMode = SynchronizedSettingRepository.getLocalInstance(getContext()).isDemoMode();
        this.rootView = inflater.inflate(
                R.layout.layout_resolution_outcome_idea_0_root, container, false);
        this.viewAnimator = rootView.findViewById(R.id.resolution_answer_view_animator);
        this.viewAnimator.setInAnimation(getContext(), R.anim.view_in_static);
        this.viewAnimator.setOutAnimation(getContext(), R.anim.view_out_zoom_out);

        // Prepare the obstacles list
        this.obstaclesList = viewAnimator.getChildAt(IDEA_VIEW_INTRO)
                .findViewById(R.id.obstacles_listview);
        this.obstaclesList.setAdapter(getObstaclesAdapter(getActivity()));
        this.obstaclesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                animateBallonsRandomly(ideaBalloonsGrid, 0);
                updateIdeaBalloonsThenShowThem(ideaBalloonsGrid, position, viewAnimator);
            }
        });

        // Prepare the idea balloons
        this.ideaBalloonsGrid = viewAnimator.getChildAt(IDEA_VIEW_PICKER)
                .findViewById(R.id.idea_balloons_gridview);

        // Set event for unlocking story
        this.unlockButton = viewAnimator.getChildAt(IDEA_VIEW_CONCLUSION)
                .findViewById(R.id.idea_unlock_chapter_button);
        this.unlockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storyUnlockListener.unlockStory(rootView);
            }
        });

        return this.rootView;
    }

    private static void animateBallonsRandomly(final GridLayout ideaBalloonsGrid, int butNot) {
        int numBalloons = ideaBalloonsGrid.getChildCount();
        int balloonToBeAnimatedIndex = new Random().nextInt(numBalloons);
        while (balloonToBeAnimatedIndex == butNot) {
            balloonToBeAnimatedIndex = new Random().nextInt(numBalloons);
        }
        final int newButNot = balloonToBeAnimatedIndex;

        View balloonImage = ideaBalloonsGrid.getChildAt(balloonToBeAnimatedIndex);

        balloonImage.animate()
                .rotation(10)
                .setInterpolator(new CycleInterpolator(2))
                .setDuration(1500)
                .withEndAction(new Runnable() {
                    public void run() {
                        animateBallonsRandomly(ideaBalloonsGrid, newButNot);
                    }
                })
                .start();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            storyUnlockListener = (StoryUnlockListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(((Activity) context).getLocalClassName()
                    + " must implement StoryUnlockListener");
        }
    }

    private static ListAdapter getObstaclesAdapter(Context context) {
        String[] obstacles = context.getResources().getStringArray(R.array.pa_obstacles);
        ArrayAdapter<String> obstaclesAdapter =
                new ArrayAdapter<>(context, R.layout.item_obstacle, obstacles);
        return obstaclesAdapter;
    }

    private static void updateIdeaBalloonsThenShowThem(
            GridLayout gridView, int position, final ViewAnimator viewAnim) {
        String[] ideas = gridView.getContext().getResources().getStringArray(IDEA_ARRAYS[position]);
        int randomizedIdeaIndex = new Random().nextInt(ideas.length);
        String idea = ideas[randomizedIdeaIndex];
        TextView ideaTV = viewAnim.getChildAt(IDEA_VIEW_CONCLUSION).findViewById(R.id.idea_text);

        ideaTV.setText(idea);

        gridView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showIdea(viewAnim);
            }
        });

        viewAnim.setDisplayedChild(IDEA_VIEW_PICKER);

        UserLogging.logResolutionIdeaChosen(position, randomizedIdeaIndex);
    }

    private static void showIdea(ViewAnimator viewAnim) {
        viewAnim.setDisplayedChild(IDEA_VIEW_CONCLUSION);
    }
}
