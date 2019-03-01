package edu.neu.ccs.wellness.storytelling;

import android.animation.ValueAnimator;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ViewAnimator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import edu.neu.ccs.wellness.server.WellnessRestServer;
import edu.neu.ccs.wellness.storytelling.homeview.ChallengeCompletedDialog;
import edu.neu.ccs.wellness.storytelling.homeview.HomeAdventurePresenter;
import edu.neu.ccs.wellness.storytelling.settings.SynchronizedSetting;
import edu.neu.ccs.wellness.storytelling.settings.SynchronizedSettingRepository;
import edu.neu.ccs.wellness.utils.WellnessIO;

/**
 * Created by hermansaksono on 2/27/19.
 */

public class ResolutionActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int NUM_SECTORS = 12;
    private static final int SECTOR_DEFAULT = 0;
    private static final int SECTOR_ANSWER = 1;
    private static final int SECTOR_PASS = 2;
    private static final int[] SECTOR_TYPES = {
            SECTOR_DEFAULT,
            SECTOR_ANSWER,
            SECTOR_PASS
    };
    private static final int[] SECTOR_FREQUENCIES = {6, 4, 2};
    private static final Random RANDOM = new Random();

    private static final int[] SECTOR_DRAWABLES = {
            R.drawable.art_roulette_baloon_generic,
            R.drawable.art_roulette_baloon_answer,
            R.drawable.art_roulette_baloon_pass
    };
    private static final int[] SECTOR_IMAGEVIEWS = {
            R.id.roulette_balloon_00,
            R.id.roulette_balloon_01,
            R.id.roulette_balloon_02,
            R.id.roulette_balloon_03,
            R.id.roulette_balloon_04,
            R.id.roulette_balloon_05,
            R.id.roulette_balloon_06,
            R.id.roulette_balloon_07,
            R.id.roulette_balloon_08,
            R.id.roulette_balloon_09,
            R.id.roulette_balloon_10,
            R.id.roulette_balloon_11,
    };
    private static final int ONE_ROTATION_DEGREE = 360;
    private static final int ONE_SECTOR_ANGLE = ONE_ROTATION_DEGREE / NUM_SECTORS;
    private static final int HALF_SECTOR_ANGLE = ONE_SECTOR_ANGLE / 2;
    private static final int QUARTER_SECTOR_ANGLE = ONE_SECTOR_ANGLE / 4;
    private static final int EXTRA_SPIN_DEGREE = ONE_ROTATION_DEGREE * 9;
    private static final int SPIN_DURATION_MILLIS = 7 * 1000;

    private static final int VIEW_OUTCOME_PASS = 2;
    private static final int VIEW_OUTCOME_ANSWER = 3;
    private static final int VIEW_OUTCOME_REGULAR = 4;


    private ViewAnimator resolutionViewAnimator;
    private ImageView rouletteArrowImg;
    private ImageView rouletteHighlightImg;
    private int pickedSectorid = 0;
    private int pickedSectorType;
    private List<Integer> sectorIds;
    private Storywell storywell;
    private SynchronizedSetting setting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WellnessRestServer.configureDefaultImageLoader(this);
        setContentView(R.layout.activity_resolution);

        this.storywell = new Storywell(this);
        this.setting = this.storywell.getSynchronizedSetting();

        this.resolutionViewAnimator = findViewById(R.id.resolution_view_animator);
        ViewGroup introLayout = findViewById(R.id.resolution_intro);
        ViewGroup rouletteLayout = findViewById(R.id.roulette_layout);
        this.rouletteArrowImg = findViewById(R.id.roulette_arrow);
        this.rouletteHighlightImg = findViewById(R.id.roulette_highlight);

        this.sectorIds = getRandomizedSectors();
        setBaloonImageViews(rouletteLayout, sectorIds);

        introLayout.findViewById(R.id.resolution_next_button).setOnClickListener(this);
        rouletteLayout.findViewById(R.id.spin_button).setOnClickListener(this);
        findViewById(R.id.read_next_story_button).setOnClickListener(this);
        findViewById(R.id.answer_reflection_button).setOnClickListener(this);
        findViewById(R.id.pick_challenges_button).setOnClickListener(this);
    }

    /**
     * Handle click events in the ResolutionActivity.
     * @param view
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.resolution_next_button:
                doShowRouletteScreen(view);
                break;
            case R.id.spin_button:
                doSpinNeedleAndShowOutcome(view);
                break;
            case R.id.read_next_story_button:
                showOutcomePassDialog(view);
                break;
            case R.id.answer_reflection_button:
                // Do nothing for now
                break;
            case R.id.pick_challenges_button:
                // Do nothing for now
                break;
        }
    }

    /**
     * Show the Roulette screen.
     * @param view
     */
    private void doShowRouletteScreen(View view) {
        view.setEnabled(false);
        resolutionViewAnimator.setInAnimation(this, R.anim.view_move_left_next);
        resolutionViewAnimator.setOutAnimation(this, R.anim.view_move_left_current);
        resolutionViewAnimator.showNext();
    }


    /**
     * Hide the Spin button, spin the needle, then show the outcome.
     * @param view
     */
    private void doSpinNeedleAndShowOutcome(View view) {
        view.setEnabled(false);
        pickedSectorid = RANDOM.nextInt(NUM_SECTORS);
        spinRoulette(rouletteArrowImg, pickedSectorid);
        view.animate().alpha(0).setDuration(1000).start();
        rouletteHighlightImg.animate().alpha(1).setDuration(500).start();
    }

    /**
     * Spin the roulette and the highlighter.
     * @param pickedSectorid
     */
    private void spinRoulette(ImageView rouletteArrowImg, final int pickedSectorid) {
        int baseDegree = (pickedSectorid * ONE_SECTOR_ANGLE);
        int shiftDegree = RANDOM.nextInt(QUARTER_SECTOR_ANGLE);
        final int totalDegree = baseDegree + shiftDegree + EXTRA_SPIN_DEGREE;
        Log.d("SWELL", "Balloon roulette sector: " + pickedSectorid);

        RotateAnimation rotateAnim = new RotateAnimation(0, totalDegree,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        rotateAnim.setDuration(SPIN_DURATION_MILLIS);
        rotateAnim.setFillAfter(true);
        rotateAnim.setInterpolator(new DecelerateInterpolator());
        rotateAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                spinHighlighter(0, totalDegree, rouletteHighlightImg);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                showOutcome(SECTOR_TYPES[sectorIds.get(pickedSectorid)]);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        rouletteArrowImg.startAnimation(rotateAnim);
    }

    /**
     * Place the highlighter circle to move to the balloon being pointed by the needle.
     * @param startDegree
     * @param endDegree
     * @param highlighterView
     */
    private void spinHighlighter(int startDegree, int endDegree, final View highlighterView) {
        ValueAnimator highlightAnimatorAngle = ValueAnimator.ofInt(startDegree, endDegree);
        highlightAnimatorAngle.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams)
                        highlighterView.getLayoutParams();

                int degree = (int) animation.getAnimatedValue();
                int animatedDegree = Math.abs((degree + HALF_SECTOR_ANGLE) / ONE_SECTOR_ANGLE)
                        * ONE_SECTOR_ANGLE;
                layoutParams.circleAngle = animatedDegree;
                highlighterView.setLayoutParams(layoutParams);
                highlighterView.requestLayout();
            }
        });
        highlightAnimatorAngle.setInterpolator(new DecelerateInterpolator());
        highlightAnimatorAngle.setDuration(SPIN_DURATION_MILLIS);
        highlightAnimatorAngle.start();
    }

    /**
     * Show the balloon that the user win
     * @param sectorType
     */
    private void showOutcome(int sectorType) {
        resolutionViewAnimator.setInAnimation(this, R.anim.view_in_zoom_in_delayed);
        resolutionViewAnimator.setOutAnimation(this, R.anim.view_out_zoom_in_delayed);

        switch (sectorType) {
            case SECTOR_PASS:
                resolutionViewAnimator.setDisplayedChild(VIEW_OUTCOME_PASS);
                animateBalloonOutcome(R.id.outcome_balloon_pass_image);
                break;
            case SECTOR_ANSWER:
                resolutionViewAnimator.setDisplayedChild(VIEW_OUTCOME_ANSWER);
                animateBalloonOutcome(R.id.outcome_balloon_answer_image);
                break;
            case SECTOR_DEFAULT:
                resolutionViewAnimator.setDisplayedChild(VIEW_OUTCOME_REGULAR);
                animateBalloonOutcome(R.id.outcome_balloon_regular_image);
                break;
            default:
                resolutionViewAnimator.setDisplayedChild(VIEW_OUTCOME_REGULAR);
                break;
        }
    }

    private void animateBalloonOutcome(int imageViewResId) {
        ImageView ballonView = findViewById(imageViewResId);
        ballonView.animate()
                .translationY(30)
                .setInterpolator(new CycleInterpolator(100))
                .setDuration(2 * 60 * 1000)
                .start();
    }

    private void showOutcomePassDialog(final View view) {
        final SynchronizedSetting setting = this.storywell.getSynchronizedSetting();
        if (setting.getStoryChallengeInfo().getIsSet()) {
            String title = setting.getStoryChallengeInfo().getStoryTitle();
            String coverImageUri = setting.getStoryChallengeInfo().getStoryCoverImageUri();
            AlertDialog dialog = ChallengeCompletedDialog.newInstance(title, coverImageUri, view.getContext(),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            HomeAdventurePresenter.unlockCurrentStoryChallenge(
                                    getApplicationContext());
                            finishActivityAndGoToStories();
                        }
                    });
            dialog.show();
        }
    }

    private void finishActivityAndGoToStories() {
        WellnessIO.getSharedPref(this).edit()
                .putInt(HomeActivity.KEY_DEFAULT_TAB, HomeActivity.TAB_STORYBOOKS)
                .apply();
        this.finish();
    }


    /**
     * Randomize the baloons inside the roulette view group;
     * @param rouletteViewGroup
     * @param sectors
     */
    private static void setBaloonImageViews(ViewGroup rouletteViewGroup, List<Integer> sectors) {
        for (int i = 0; i < SECTOR_IMAGEVIEWS.length; i++) {
            ImageView imageView = rouletteViewGroup.findViewById(SECTOR_IMAGEVIEWS[i]);
            imageView.setImageResource(SECTOR_DRAWABLES[sectors.get(i)]);
        }
    }

    private static List<Integer> getRandomizedSectors() {
        List<Integer> randoms = new ArrayList<>();

        for (int i=0; i < SECTOR_FREQUENCIES.length; i++) {
            int size = SECTOR_FREQUENCIES[i];
            int value = SECTOR_TYPES[i];
            List<Integer> subsequence = Collections.nCopies(size, value);
            randoms.addAll(subsequence);
        }
        Collections.shuffle(randoms);
        return randoms;
    }
}
