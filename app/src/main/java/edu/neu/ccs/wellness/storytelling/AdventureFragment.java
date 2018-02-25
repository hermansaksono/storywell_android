package edu.neu.ccs.wellness.storytelling;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import edu.neu.ccs.wellness.storywell.interfaces.GameLevelInterface;
import edu.neu.ccs.wellness.storywell.interfaces.GameMonitoringControllerInterface;
import edu.neu.ccs.wellness.storywell.monitoringview.GameLevel;
import edu.neu.ccs.wellness.storywell.monitoringview.HeroSprite;
import edu.neu.ccs.wellness.storywell.monitoringview.MonitoringController;
import edu.neu.ccs.wellness.storywell.monitoringview.MonitoringView;

public class AdventureFragment extends Fragment {

    /* PRIVATE VARIABLES */
    private GameMonitoringControllerInterface monitoringController;
    private Typeface gameFont;
    private boolean hasProgressShown = false;

    /* CONSTRUCTOR */
    public AdventureFragment() { } // Required empty public constructor

    /* FACTORY METHOD */
    public static AdventureFragment newInstance() {
        return new AdventureFragment();
    }

    /* INTERFACE FUNCTIONS */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_flying, container, false);

        this.gameFont = ResourcesCompat.getFont(getContext(), MonitoringActivity.FONT_FAMILY);

        MonitoringView gameView = rootView.findViewById(R.id.monitoringView);
        GameLevelInterface gameLevel = getGameLevelDesign(this.gameFont);
        HeroSprite hero = new HeroSprite(getResources(), R.drawable.hero_girl_base, R.color.colorPrimaryLight);

        this.monitoringController = new MonitoringController(gameView);
        this.monitoringController.setLevelDesign(getResources(), gameLevel);
        this.monitoringController.setHeroSprite(hero);

        gameView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startMonitoringActivity();
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
    private void startMonitoringActivity() {
        Intent intent = new Intent(getContext(), MonitoringActivity.class);
        getContext().startActivity(intent);
    }

    private void startShowingProgress() {
        if (this.hasProgressShown == false) {
            this.monitoringController.setHeroToMoveOnY(0.75f);
            this.hasProgressShown = true;
        }
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
