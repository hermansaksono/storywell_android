package edu.neu.ccs.wellness.storytelling;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import edu.neu.ccs.wellness.storywell.interfaces.GameLevelInterface;
import edu.neu.ccs.wellness.storywell.interfaces.GameMonitoringControllerInterface;
import edu.neu.ccs.wellness.storywell.interfaces.GameViewInterface;
import edu.neu.ccs.wellness.storywell.monitoringview.GameLevel;
import edu.neu.ccs.wellness.storywell.monitoringview.HeroSprite;
import edu.neu.ccs.wellness.storywell.monitoringview.MonitoringController;
import edu.neu.ccs.wellness.storywell.monitoringview.OneDayMonitoringView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class OneDayMonitoringActivity extends AppCompatActivity {

    /* PRIVATE VARIABLES */
    GameMonitoringControllerInterface monitoringController;
    GameViewInterface gameView;
    HeroSprite hero;

    /* INTERFACE FUNCTIONS */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_oneday_monitoring);

        this.gameView = findViewById(R.id.monitoringView);
        this.monitoringController = new MonitoringController(this.gameView, 1);
        this.hero = new HeroSprite(getResources(), R.drawable.hero_girl_base);
        GameLevelInterface gameLevel = new GameLevel(R.color.flying_sky,
                R.drawable.gameview_sea_fg_lv01,
                R.drawable.gameview_island_lv01,
                R.drawable.gameview_clouds_fg1_lv01,
                R.drawable.gameview_clouds_bg1_lv01,
                R.drawable.gameview_clouds_fg2_lv01,
                R.drawable.gameview_clouds_bg2_lv01);

        this.monitoringController.setLevelDesign(getResources(), gameLevel);
        this.monitoringController.setHeroSprite(this.hero);

        LinearLayout linearLayout = findViewById(R.id.layoutMonitoringView);
        linearLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                hero.setToMoving(0.6f);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        this.gameView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();

        this.gameView.stop();
    }
}
