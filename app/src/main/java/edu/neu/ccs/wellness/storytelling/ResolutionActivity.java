package edu.neu.ccs.wellness.storytelling;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by hermansaksono on 2/27/19.
 */

public class ResolutionActivity extends AppCompatActivity {

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
            R.id.roulette_balloon_12,
    };
    private static final int ONE_SECTOR_ANGLE = 360 / NUM_SECTORS;
    private static final int EXTRA_SPIN_DEGREE = 360 * 9;
    private static final int SPIN_DURATION_MILLIS = 7 * 1000;


    private ImageView rouletteArrowImg;
    private int pickedSectorid;
    private int pickedSectorType;
    private List<Integer> sectorIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resolution);

        ViewGroup rouletteLayout = findViewById(R.id.roulette_layout);
        this.rouletteArrowImg = findViewById(R.id.roulette_arrow);

        this.sectorIds = getRandomizedSectors();
        setBaloonImageViews(rouletteLayout, sectorIds);

        Button spinButton = findViewById(R.id.spin_button);
        spinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickedSectorid = RANDOM.nextInt(NUM_SECTORS);
                spinRoulette(rouletteArrowImg, pickedSectorid);
            }
        });
    }

    /**
     * Spin the roulette
     * @param pickedSectorid
     */
    private static void spinRoulette(ImageView rouletteArrowImg, int pickedSectorid) {
        int baseDegree = (pickedSectorid * ONE_SECTOR_ANGLE) + (RANDOM.nextInt(ONE_SECTOR_ANGLE));
        int totalDegree = baseDegree + EXTRA_SPIN_DEGREE;

        RotateAnimation rotateAnim = new RotateAnimation(0, totalDegree,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        rotateAnim.setDuration(SPIN_DURATION_MILLIS);
        rotateAnim.setFillAfter(true);
        rotateAnim.setInterpolator(new DecelerateInterpolator());

        rouletteArrowImg.startAnimation(rotateAnim);
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

    private static List<ImageView> getBalloonImageViews(ViewGroup rouletteViewGroup) {
        List<ImageView> ballonsImageViews = new ArrayList<>();
        for (int i = 1; i < rouletteViewGroup.getChildCount(); i++) {
            ImageView balloon = (ImageView) rouletteViewGroup.getChildAt(i);
            ballonsImageViews.add(balloon);
        }
        return ballonsImageViews;
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
