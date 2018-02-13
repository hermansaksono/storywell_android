package edu.neu.ccs.wellness.storytelling;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import edu.neu.ccs.wellness.storywell.monitoringview.CloudSprite;
import edu.neu.ccs.wellness.storywell.monitoringview.HeroSprite;
import edu.neu.ccs.wellness.storywell.monitoringview.IslandSprite;
import edu.neu.ccs.wellness.storywell.monitoringview.SeaSprite;
import edu.neu.ccs.wellness.storywell.monitoringview.SevenDayMonitoringView;
import edu.neu.ccs.wellness.storywell.monitoringview.SolidBackground;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MonitoringActivity extends AppCompatActivity {

    /* PRIVATE VARIABLES */
    SevenDayMonitoringView gameView;
    SolidBackground sky;
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

        setContentView(R.layout.activity_monitoring);

    }

    @Override
    protected void onStart() {
        super.onStart();

        this.gameView = findViewById(R.id.monitoringView);
        this.sky = new SolidBackground(getResources().getColor(R.color.flying_sky));
        this.hero = new HeroSprite(getResources(), R.drawable.art_flying);

        this.gameView.addBackground(sky);
        this.gameView.addSprite(new SeaSprite(getResources(), R.drawable.gameview_sea_bg_lv01, 10, 40));
        this.gameView.addSprite(new IslandSprite(getResources(), R.drawable.gameview_island_lv01, "WED"));
        this.gameView.addSprite(new CloudSprite(getResources(), R.drawable.gameview_clouds_bg1_lv01, -900, 200, 0.75f, -30));
        this.gameView.addSprite(new CloudSprite(getResources(), R.drawable.gameview_clouds_bg2_lv01, 200, 200, 0.75f, -30));
        this.gameView.addSprite(new CloudSprite(getResources(), R.drawable.gameview_clouds_fg1_lv01, -1200, 200, 0.65f, -50));
        this.gameView.addSprite(new CloudSprite(getResources(), R.drawable.gameview_clouds_fg2_lv01, -600, 600, 0.65f, -50));
        this.gameView.addSprite(hero);
        this.gameView.addSprite(new SeaSprite(getResources(), R.drawable.gameview_sea_fg_lv01, 10, 10));
    }

    @Override
    protected void onResume() {
        super.onResume();

        this.gameView.start();
        this.hero.setToHover();
        //this.hero.setToMoving(300);
    }

    @Override
    protected void onPause() {
        super.onPause();

        this.gameView.stop();
    }
}
