package edu.neu.ccs.wellness.storytelling;

import android.graphics.Typeface;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import edu.neu.ccs.wellness.storywell.interfaces.GameLevelInterface;
import edu.neu.ccs.wellness.storywell.interfaces.GameMonitoringControllerInterface;
import edu.neu.ccs.wellness.storywell.monitoringview.GameLevel;
import edu.neu.ccs.wellness.storywell.monitoringview.HeroSprite;
import edu.neu.ccs.wellness.storywell.monitoringview.MonitoringController;
import edu.neu.ccs.wellness.storywell.monitoringview.MonitoringDetailFragment;
import edu.neu.ccs.wellness.storywell.monitoringview.MonitoringView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MonitoringActivity extends AppCompatActivity {

    /* STATIC VARIABLES */
    public static final int FONT_FAMILY = R.font.montserrat_bold;

    /* PRIVATE VARIABLES */
    private GameMonitoringControllerInterface monitoringController;
    private Typeface gameFont;

    /* INTERFACE FUNCTIONS */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitoring);

        this.gameFont = ResourcesCompat.getFont(this, FONT_FAMILY);

        final MonitoringView gameView = findViewById(R.id.monitoringView);
        HeroSprite hero = new HeroSprite(getResources(), R.drawable.hero_girl_base, R.color.colorPrimaryLight);
        GameLevelInterface gameLevel = getGameLevelDesign(this.gameFont);

        this.monitoringController = new MonitoringController(gameView);
        this.monitoringController.setLevelDesign(getResources(), gameLevel);
        this.monitoringController.setHeroSprite(hero);

        gameView.setOnTouchListener (new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Log.d("WELLD", "touch at: " + gameView.getDayIndex(event.getX()));
                    showDetailDialog();
                }
                return true;
            }
        });

        LinearLayout linearLayout = findViewById(R.id.layoutMonitoringView);
        linearLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                monitoringController.setHeroToMoveOnY(0.7f);
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

        this.monitoringController.start();
    }

    @Override
    protected void onPause() {
        super.onPause();

        this.monitoringController.stop();
    }

    private void showDetailDialog() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        // Create and show the dialog.
        DialogFragment newFragment = MonitoringDetailFragment.newInstance();
        newFragment.show(ft, "dialog");
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
