package edu.neu.ccs.wellness.storytelling;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import edu.neu.ccs.wellness.storywell.interfaces.GameLevelInterface;
import edu.neu.ccs.wellness.storywell.interfaces.GameMonitoringControllerInterface;
import edu.neu.ccs.wellness.storywell.interfaces.GameViewInterface;
import edu.neu.ccs.wellness.storywell.monitoringview.GameLevel;
import edu.neu.ccs.wellness.storywell.monitoringview.HeroSprite;
import edu.neu.ccs.wellness.storywell.monitoringview.MonitoringController;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class SevenDayMonitoringActivity extends AppCompatActivity {

    /* PRIVATE VARIABLES */
    GameMonitoringControllerInterface monitoringController;

    /* PUBLIC METHODS */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sevenday_monitoring);

        GameViewInterface gameView = findViewById(R.id.monitoringView);
        GameLevelInterface gameLevel = getGameLevelDesign();
        HeroSprite hero = new HeroSprite(getResources(), R.drawable.hero_girl_base);

        this.monitoringController = new MonitoringController(gameView, 7);
        this.monitoringController.setLevelDesign(getResources(), gameLevel);
        this.monitoringController.setHeroSprite(hero);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        this.monitoringController.start();
    }

    @Override
    protected void onPause() {
        super.onPause();

        this.monitoringController.stop();
    }

    /* PRIVATE STATIC METHODS */
    public static GameLevelInterface getGameLevelDesign() {
        GameLevelInterface gameLevelDesign = new GameLevel(R.color.flying_sky,
                R.drawable.gameview_sea_fg_lv01,
                R.drawable.gameview_island_lv01,
                R.drawable.gameview_clouds_fg1_lv01,
                R.drawable.gameview_clouds_bg1_lv01,
                R.drawable.gameview_clouds_fg2_lv01,
                R.drawable.gameview_clouds_bg2_lv01);
        return gameLevelDesign;
    }
}
