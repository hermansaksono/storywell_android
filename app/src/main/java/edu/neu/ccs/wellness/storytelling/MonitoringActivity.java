package edu.neu.ccs.wellness.storytelling;

import android.annotation.SuppressLint;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

import edu.neu.ccs.wellness.storywell.monitoringview.HeroSprite;
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
        this.gameView.addSprite(hero);
    }

    @Override
    protected void onResume() {
        super.onResume();

        this.gameView.start();
        this.hero.setToHover();
        this.hero.setToMoving(1200);
    }
}
