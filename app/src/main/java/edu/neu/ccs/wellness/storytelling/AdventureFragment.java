package edu.neu.ccs.wellness.storytelling;

import android.annotation.SuppressLint;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;

import edu.neu.ccs.wellness.fitness.interfaces.GroupFitnessInterface;
import edu.neu.ccs.wellness.story.interfaces.StoryInterface;
import edu.neu.ccs.wellness.storytelling.adventureview.OneDayGroupFitnessViewModel;
import edu.neu.ccs.wellness.storytelling.homeview.StoryListViewModel;
import edu.neu.ccs.wellness.storytelling.utils.StoryCoverAdapter;
import edu.neu.ccs.wellness.storywell.interfaces.GameLevelInterface;
import edu.neu.ccs.wellness.storywell.interfaces.GameMonitoringControllerInterface;
import edu.neu.ccs.wellness.storywell.interfaces.OnAnimationCompletedListener;
import edu.neu.ccs.wellness.storywell.monitoringview.GameLevel;
import edu.neu.ccs.wellness.storywell.monitoringview.HeroSprite;
import edu.neu.ccs.wellness.storywell.monitoringview.MonitoringController;
import edu.neu.ccs.wellness.storywell.monitoringview.MonitoringView;
import edu.neu.ccs.wellness.utils.WellnessDate;

public class AdventureFragment extends Fragment {

    /* PRIVATE VARIABLES */
    private GameMonitoringControllerInterface monitoringController;
    private MonitoringView gameView;
    private Typeface gameFont;
    private boolean hasProgressShown = false;
    private OneDayGroupFitnessViewModel oneDayGroupFitnessViewModel;

    /* CONSTRUCTOR */
    public AdventureFragment() { } // Required empty public constructor

    /* FACTORY METHOD */
    public static AdventureFragment newInstance() {
        return new AdventureFragment();
    }

    /* INTERFACE FUNCTIONS */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_flying, container, false);

        this.gameFont = ResourcesCompat.getFont(getContext(), MonitoringActivity.FONT_FAMILY);
        this.gameView = rootView.findViewById(R.id.monitoringView);

        GameLevelInterface gameLevel = MonitoringActivity.getGameLevelDesign(this.gameFont);
        HeroSprite hero = new HeroSprite(getResources(), R.drawable.hero_dora,
                MonitoringActivity.getAdultBalloonDrawables(10),
                MonitoringActivity.getChildBalloonDrawables(10),
                R.color.colorPrimaryLight);

        this.monitoringController = new MonitoringController(gameView);
        this.monitoringController.setLevelDesign(getResources(), gameLevel);
        this.monitoringController.setHeroSprite(hero);

        // Load the StoryList
        this.oneDayGroupFitnessViewModel = ViewModelProviders.of(this).get(OneDayGroupFitnessViewModel.class);
        oneDayGroupFitnessViewModel.getGroupFitness().observe(this, new Observer<GroupFitnessInterface>() {
            @Override
            public void onChanged(@Nullable final GroupFitnessInterface groupFitness) {
                // TODO DO SOMETHING
            }
        });

        this.gameView.setOnTouchListener (new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                processTapToShowMonitoring(event);
                return true;
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        this.monitoringController.start();
        this.startShowingProgress();
    }

    @Override
    public void onPause() {
        super.onPause();
        this.monitoringController.stop();
        this.hasProgressShown = false;
    }

    @Override
    public void onStop() {
        super.onStop();
        this.hasProgressShown = false;
    }

    /* PRIVATE METHODS */
    private void processTapToShowMonitoring(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (gameView.isOverAnyIsland(event)) {
                startMonitoringActivity();
            }
        }
    }

    private void startMonitoringActivity() {
        Intent intent = new Intent(getContext(), MonitoringActivity.class);
        getContext().startActivity(intent);
    }

    private void startShowingProgress() {
        if (this.hasProgressShown == false) {
            //this.monitoringController.setHeroToMoveOnY(0.75f);
            this.monitoringController.setProgress(0.4f, 0.8f, 0.6f,
                    new OnAnimationCompletedListener() {
                        @Override
                        public void onAnimationCompleted() {
                            showInstruction();
                        }
                    });
            this.hasProgressShown = true;
        }
    }

    /**
     * Show the instruction on the screen
     */
    private void showInstruction() {
        String instruction = String.format(
                getString(R.string.tooltip_see_7_day_adventure),
                WellnessDate.getDayOfWeek(WellnessDate.getDayOfWeek()));
        Toast toast = Toast.makeText(getContext(), instruction, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0,
                (int) (60 * getResources().getDisplayMetrics().density));
        toast.show();
    }

    /* PRIVATE STATIC METHODS */
    public static GameLevelInterface getGameLevelDesign(Typeface gameFont) {
        GameLevelInterface gameLevelDesign = new GameLevel(R.color.flying_sky,
                R.drawable.gameview_sea_fg_lv01,
                R.drawable.gameview_island_lv01,
                R.drawable.gameview_clouds_fg1_lv01,
                R.drawable.gameview_clouds_bg1_lv01,
                R.drawable.gameview_clouds_fg2_lv01,
                R.drawable.gameview_clouds_bg2_lv01,
                gameFont);
        return gameLevelDesign;
    }
}
