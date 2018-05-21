package edu.neu.ccs.wellness.storytelling;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import edu.neu.ccs.wellness.fitness.interfaces.GroupFitnessInterface;
import edu.neu.ccs.wellness.storytelling.adventureview.OneDayGroupFitnessViewModel;
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

        // Load the Fitness data
        /*
        this.oneDayGroupFitnessViewModel = ViewModelProviders.of(this).get(OneDayGroupFitnessViewModel.class);
        oneDayGroupFitnessViewModel.getGroupFitness().observe(this, new Observer<GroupFitnessInterface>() {
            @Override
            public void onChanged(@Nullable final GroupFitnessInterface groupFitness) {
                // TODO DO SOMETHING
            }
        });
        */

        this.gameView.setOnTouchListener (new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                processTap(event);
                return true;
            }
        });

        FloatingActionButton fab = rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        return rootView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUserVisibleHint(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        this.monitoringController.start();
        //this.startShowingProgress();
    }

    @Override
    public void onPause() {
        super.onPause();
        this.monitoringController.stop();
        //this.hasProgressShown = false;
    }

    @Override
    public void onStop() {
        super.onStop();
        //this.hasProgressShown = false;
    }

    /* PRIVATE METHODS */
    private void processTap(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (gameView.isOverHero(event)) {
                this.startShowingProgress();
            }
        }
    }

    private void startMonitoringActivity() {
        Intent intent = new Intent(getContext(), MonitoringActivity.class);
        getContext().startActivity(intent);
    }

    private void startShowingProgress() {
        if (this.hasProgressShown == false) {
            this.monitoringController.setProgress(0.4f, 0.8f, 0.6f,
                    new OnAnimationCompletedListener() {
                        @Override
                        public void onAnimationCompleted() {
                            showPostAnimationMessage();
                        }
                    });
            this.hasProgressShown = true;
        }
    }

    /**
     * Show the instruction on the screen
     */
    public void showPostAnimationMessage() {
        final Snackbar snackbar = getPostAdventureRefreshSnackbar(getActivity());
        snackbar.setAction(R.string.button_adventure_refresh, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startMonitoringActivity();
                        //snackbar.dismiss();
                    }
                })
                .show();
    }

    public static Snackbar getPreAdventureRefreshSnackbar(Activity activity) {
        String instruction = activity.getString(R.string.tooltip_see_monitoring_progress);
        return getSnackbar(instruction, activity);
    }

    // STATIC PUBLIC SNACKBAR METHODS
    public static Snackbar getPostAdventureRefreshSnackbar(Activity activity) {
        String message = activity.getString(R.string.tooltip_snackbar_progress_ongoing);
        return getSnackbar(message, activity).setDuration(Snackbar.LENGTH_INDEFINITE);
    }

    private static Snackbar getSnackbar(String text, Activity activity) {
        Snackbar snackbar = Snackbar.make(activity.findViewById(R.id.layoutMonitoringView), text,
                Snackbar.LENGTH_LONG);
        snackbar = setSnackBarTheme(snackbar, activity.getApplicationContext());
        return snackbar;
    }

    private static Snackbar setSnackBarTheme(Snackbar snackbar, Context context) {
        snackbar.getView().setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
        return snackbar;
    }

    private static void showToast(String text, int xOffset, int yOffset, int gravity, Context context) {
        Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        toast.setGravity(gravity, xOffset, yOffset);
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
