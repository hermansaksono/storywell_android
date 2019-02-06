package edu.neu.ccs.wellness.storytelling.monitoringview;

import android.content.res.Resources;

import java.util.ArrayList;
import java.util.List;

import edu.neu.ccs.wellness.storytelling.monitoringview.interfaces.GameLevelInterface;
import edu.neu.ccs.wellness.storytelling.monitoringview.interfaces.GameMonitoringControllerInterface;
import edu.neu.ccs.wellness.storytelling.monitoringview.interfaces.GameViewInterface;
import edu.neu.ccs.wellness.storytelling.monitoringview.interfaces.OnAnimationCompletedListener;
import edu.neu.ccs.wellness.utils.WellnessDate;

/**
 * Created by hermansaksono on 2/15/18.
 */

public class MonitoringController implements GameMonitoringControllerInterface {

    /* STATIC VARIABLES */
    private static final float ISLAND_HEIGHT_RATIO_1D = 0.4f;
    private static final float ISLAND_HEIGHT_RATIO_2D = 0.20f;
    private static final float ISLAND_HEIGHT_RATIO_7D = 0.125f;
    private static final float HERO_LOWEST_POSITION_X_RATIO = 0.63f;

    /* PRIVATE VARIABLES */
    private GameViewInterface gameView;
    private HeroSprite hero;
    private int numDays = 1;
    private List<OnAnimationCompletedListener> animationCompletedListenerList;

    public MonitoringController(GameViewInterface gameView) {
        this.gameView = gameView;
        this.numDays = gameView.getNumDays();
        this.animationCompletedListenerList = new ArrayList<OnAnimationCompletedListener>();
    }

    @Override
    public void setLevelDesign(Resources res, GameLevelInterface levelDesign) {
        this.gameView.addBackground(levelDesign.getBaseBackground(res));
        this.gameView.addSprite(levelDesign.getCloudBg1(res));
        this.gameView.addSprite(levelDesign.getCloudBg2(res));
        this.gameView.addSprite(levelDesign.getCloudFg1(res));
        this.gameView.addSprite(levelDesign.getCloudFg2(res));
        /*
        this.gameView.addSprite(levelDesign.getSeaBg(res,
                0.5f, getSeaHeightRatio(this.numDays) - 0.02f,
                0.03f, 0));*/
        this.addIslands(res, levelDesign);
        this.gameView.addSprite(levelDesign.getSeaFg(res,
                0.5f, getSeaHeightRatio(this.numDays),
                0.02f, 0));
    }

    @Override
    public void setHeroSprite(HeroSprite hero) {
        this.hero = hero;
        this.hero.setClosestPosXRatio(this.getHeroPosXRatio(this.numDays));
        this.hero.setFarthestXRatio(getFarhtestPosXRatio(this.numDays));
        this.hero.setLowestYRatio(getHeroLowestPosYRatioToWidth(this.numDays));
        this.gameView.addSprite(this.hero);
    }

    @Override
    public void setHeroToMoveOnY(float posYRatio) {
        this.hero.setToMoveParabolic(posYRatio, gameView.getElapsedMillisec());
    }

    @Override
    public void setProgress(float adult, float child, float total,
                            OnAnimationCompletedListener animationCompletedListener) {
        this.hero.setToMoveParabolic(adult, child, total, gameView.getElapsedMillisec(),
                animationCompletedListener);
    }

    @Override
    public void resetProgress() {
        this.hero.reset();
    }

    @Override
    public void start() {
        this.gameView.start();
    }

    @Override
    public void resume() {
        this.gameView.resume();
    }

    @Override
    public void pause() {
        this.gameView.pause();
    }

    @Override
    public void stop() {
        this.gameView.stop();
    }

    public void setHeroIsVisible(boolean isVisible) {
        this.hero.setIsVisible(isVisible);
    }

    public void setHeroChallengeAsCompleted() {
        this.hero.setToCompleted();
    }

    /* PRIVATE METHODS */
    private void addIslands(Resources res, GameLevelInterface levelDesign) {
        if (this.numDays == 1) {
            addOneIsland(res, this.gameView, levelDesign);
        } else if (this.numDays == 2) {
            addTwoIsland(res, this.gameView, levelDesign);
        } else if (this.numDays == 7) {
            addSevenIslands(res, this.gameView, levelDesign);
        }
    }

    /* PRIVATE FUNCTIONS */

    /* PRIVATE STATIC FUNCTIONS */
    private static void addSevenIslands(Resources res, GameViewInterface gameView, GameLevelInterface levelDesign) {
        gameView.addSprite(levelDesign.getIsland(res, 1, 1f/16, 1f, ISLAND_HEIGHT_RATIO_7D));
        gameView.addSprite(levelDesign.getIsland(res, 2, 3f/16, 1f, ISLAND_HEIGHT_RATIO_7D));
        gameView.addSprite(levelDesign.getIsland(res, 3, 5f/16, 1f, ISLAND_HEIGHT_RATIO_7D));
        gameView.addSprite(levelDesign.getIsland(res, 4, 7f/16, 1f, ISLAND_HEIGHT_RATIO_7D));
        gameView.addSprite(levelDesign.getIsland(res, 5, 9f/16, 1f, ISLAND_HEIGHT_RATIO_7D));
        gameView.addSprite(levelDesign.getIsland(res, 6, 11f/16,1f, ISLAND_HEIGHT_RATIO_7D));
        gameView.addSprite(levelDesign.getIsland(res, 7, 13f/16,1f, ISLAND_HEIGHT_RATIO_7D));
        gameView.addSprite(levelDesign.getIsland(res, 0, 15f/16,1f, ISLAND_HEIGHT_RATIO_7D));
    }

    private static void addTwoIsland(Resources res, GameViewInterface gameView, GameLevelInterface levelDesign) {
        int dayOfWeek = WellnessDate.getDayOfWeek();

        //gameView.addSprite(levelDesign.getIsland(res, getDay(dayOfWeek), 0.25f, 0.9f, ISLAND_HEIGHT_RATIO_2D));
        //gameView.addSprite(levelDesign.getIsland(res, getDay(dayOfWeek + 1), 0.75f, 0.9f, ISLAND_HEIGHT_RATIO_2D));
        gameView.addSprite(levelDesign.getIsland(res, getDay(dayOfWeek), 0.20f, 1, ISLAND_HEIGHT_RATIO_2D));
        gameView.addSprite(levelDesign.getIsland(res, getDay(dayOfWeek + 1), 0.80f, 1, ISLAND_HEIGHT_RATIO_2D));

    }

    private static void addOneIsland(Resources res, GameViewInterface gameView, GameLevelInterface levelDesign) {
        gameView.addSprite(levelDesign.getIsland(res, WellnessDate.getDayOfWeek(), 0.5f, 1, ISLAND_HEIGHT_RATIO_1D));
    }

    private static int getDay(int day) {
        if (day <= 7)
            return day;
        else
            return day % 7;
    }

    private static float getIslandWidthRatio(int numDays) {
        if (numDays == 1) {
            return IslandSprite.getIslandWidthRatio(ISLAND_HEIGHT_RATIO_1D);
        } else if (numDays == 2){
            return IslandSprite.getIslandWidthRatio(ISLAND_HEIGHT_RATIO_2D);
        } else {
            return IslandSprite.getIslandWidthRatio(ISLAND_HEIGHT_RATIO_7D);
        }
    }

    private static float getSeaHeightRatio(int numDays) {
        float ratio;
        if (numDays <= 2) {
            //ratio = (1 - (IslandSprite.getIslandWidthRatio(ISLAND_HEIGHT_RATIO_1D) * 0.005f)) - 0.1f;
            ratio = (1 - (IslandSprite.getIslandWidthRatio(ISLAND_HEIGHT_RATIO_1D) * 0.005f));
        } else {
            ratio = (1 - (IslandSprite.getIslandWidthRatio(ISLAND_HEIGHT_RATIO_7D) * 0.005f));
        }
        return ratio;
    }

    private static float getHeroPosXRatio(int numDays) {
        if (numDays == 1) {
            return 0.5f;
        } else if (numDays == 2) {
            return ISLAND_HEIGHT_RATIO_2D;
        } else {
            int dayOfWeek = WellnessDate.getDayOfWeek();
            return (((dayOfWeek - 1) * 2) + 1f) / 16f;
        }
    }

    private static float getFarhtestPosXRatio(int numDays) {
        if (numDays == 1) {
            return 0.5f;
        } else if (numDays == 2) {
            return 1 - ISLAND_HEIGHT_RATIO_2D;
        } else {
            //return 3/16;//(1 + (2 * (getDayOfWeek() - 1))) / 16f;

            int dayOfWeek = WellnessDate.getDayOfWeek();
            return (((dayOfWeek) * 2) + 1f) / 16f;
        }
    }

    private static float getHeroLowestPosYRatioToWidth(int numDays) {
        if (numDays <= 2) {
            //return (getIslandWidthRatio(numDays) * (HERO_LOWEST_POSITION_X_RATIO)) + 0.15f;
            return (getIslandWidthRatio(numDays) * HERO_LOWEST_POSITION_X_RATIO);
        } else {
            return (getIslandWidthRatio(numDays) * (HERO_LOWEST_POSITION_X_RATIO));
        }
    }

}
