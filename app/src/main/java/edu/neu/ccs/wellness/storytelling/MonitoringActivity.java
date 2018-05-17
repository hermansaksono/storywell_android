package edu.neu.ccs.wellness.storytelling;

import android.graphics.Typeface;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import edu.neu.ccs.wellness.storywell.interfaces.GameLevelInterface;
import edu.neu.ccs.wellness.storywell.interfaces.GameMonitoringControllerInterface;
import edu.neu.ccs.wellness.storywell.interfaces.OnAnimationCompletedListener;
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
    private MonitoringView gameView;
    private Typeface gameFont;
    private boolean hasProgressShown = false;

    /* INTERFACE FUNCTIONS */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitoringview);

        this.gameFont = ResourcesCompat.getFont(this, FONT_FAMILY);

        this.gameView = findViewById(R.id.monitoringView);
        HeroSprite hero = new HeroSprite(getResources(), R.drawable.hero_dora,
                MonitoringActivity.getAdultBalloonDrawables(10),
                MonitoringActivity.getChildBalloonDrawables(10),
                R.color.colorPrimaryLight);
        GameLevelInterface gameLevel = getGameLevelDesign(this.gameFont);

        this.monitoringController = new MonitoringController(gameView);
        this.monitoringController.setLevelDesign(getResources(), gameLevel);
        this.monitoringController.setHeroSprite(hero);

        gameView.setOnTouchListener (new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                processTapToShowDialog(event);
                /*
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Log.d("WELLD", "touch at: " + gameView.getDayIndex(event.getX()));
                    showDetailDialog();
                }
                */
                return true;
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
        this.startShowingProgress();
    }

    @Override
    protected void onPause() {
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
    private void processTapToShowDialog(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (this.gameView.isOverAnyIsland(event)) {
                int dayIndex = gameView.getDayIndex(event.getX());
                showDetailDialog(dayIndex);
            }
        }
    }

    private void startShowingProgress() {
        if (this.hasProgressShown == false) {
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

    private void showDetailDialog(int dayIndex) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        // Create and show the dialog.
        DialogFragment newFragment = MonitoringDetailFragment.newInstance(dayIndex);
        newFragment.show(ft, "dialog");
    }

    /**
     * Show the instruction on the screen
     */
    private void showInstruction() {
        String navigationInfo = getString(R.string.tooltip_see_1_day_progress);
        Toast toast = Toast.makeText(getApplicationContext(), navigationInfo, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0,
                (int) (50 * getResources().getDisplayMetrics().density));
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

    public static int[] getAdultBalloonDrawables(int numBaloons) {
        int balloonDrawableIds[] = null;
        if (numBaloons == 10) {
            balloonDrawableIds = new int[]{
                    R.drawable.ic_a_balloons_10k_00,
                    R.drawable.ic_a_balloons_10k_01,
                    R.drawable.ic_a_balloons_10k_02,
                    R.drawable.ic_a_balloons_10k_03,
                    R.drawable.ic_a_balloons_10k_04,
                    R.drawable.ic_a_balloons_10k_05,
                    R.drawable.ic_a_balloons_10k_06,
                    R.drawable.ic_a_balloons_10k_07,
                    R.drawable.ic_a_balloons_10k_08,
                    R.drawable.ic_a_balloons_10k_09,
                    R.drawable.ic_a_balloons_10k_10};
        }
        return balloonDrawableIds;
    }

    public static int[] getChildBalloonDrawables(int numBaloons) {
        int balloonDrawableIds[] = null;
        if (numBaloons == 10) {
            balloonDrawableIds = new int[]{
                    R.drawable.ic_c_balloons_10k_00,
                    R.drawable.ic_c_balloons_10k_01,
                    R.drawable.ic_c_balloons_10k_02,
                    R.drawable.ic_c_balloons_10k_03,
                    R.drawable.ic_c_balloons_10k_04,
                    R.drawable.ic_c_balloons_10k_05,
                    R.drawable.ic_c_balloons_10k_06,
                    R.drawable.ic_c_balloons_10k_07,
                    R.drawable.ic_c_balloons_10k_08,
                    R.drawable.ic_c_balloons_10k_09,
                    R.drawable.ic_c_balloons_10k_10};
        }
        return balloonDrawableIds;
    }
}
