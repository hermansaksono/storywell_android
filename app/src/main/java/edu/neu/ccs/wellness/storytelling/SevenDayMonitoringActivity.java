package edu.neu.ccs.wellness.storytelling;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import edu.neu.ccs.wellness.storywell.interfaces.GameLevelInterface;
import edu.neu.ccs.wellness.storywell.monitoringview.GameLevel;
import edu.neu.ccs.wellness.storywell.monitoringview.HeroSprite;
import edu.neu.ccs.wellness.storywell.monitoringview.SevenDayMonitoringView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class SevenDayMonitoringActivity extends AppCompatActivity {

    /* PRIVATE VARIABLES */
    SevenDayMonitoringView gameView;
    HeroSprite hero;

    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sevenday_monitoring);

        this.hero = new HeroSprite(getResources(), R.drawable.hero_girl_base);
        GameLevelInterface gameLevel = new GameLevel(R.color.flying_sky,
                R.drawable.gameview_sea_fg_lv01,
                R.drawable.gameview_island_lv01,
                R.drawable.gameview_clouds_fg1_lv01,
                R.drawable.gameview_clouds_bg1_lv01,
                R.drawable.gameview_clouds_fg2_lv01,
                R.drawable.gameview_clouds_bg2_lv01);
        gameLevel.setHero(this.hero);

        this.gameView = findViewById(R.id.monitoringView);
        this.gameView.setLevelDesign(getResources(), gameLevel);

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        this.gameView.start();
        //this.hero.setToMoving(150);
    }

    @Override
    protected void onPause() {
        super.onPause();

        this.gameView.stop();
    }
}
