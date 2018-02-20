package edu.neu.ccs.wellness.storywell.monitoringview;

import android.content.res.Resources;

import java.util.Calendar;

import edu.neu.ccs.wellness.storywell.interfaces.GameLevelInterface;
import edu.neu.ccs.wellness.storywell.interfaces.GameMonitoringControllerInterface;
import edu.neu.ccs.wellness.storywell.interfaces.GameViewInterface;

/**
 * Created by hermansaksono on 2/15/18.
 */

public class MonitoringController implements GameMonitoringControllerInterface {

    /* STATIC VARIABLES */
    private static final float ISLAND_HEIGHT_RATIO_1D = 0.4f;
    private static final float ISLAND_HEIGHT_RATIO_7D = 0.125f;
    private static final float HERO_LOWEST_POSITION_X_RATIO = 0.63f;

    /* PRIVATE VARIABLES */
    private GameViewInterface gameView;
    private HeroSprite hero;
    private int numDays = 1;

    public MonitoringController(GameViewInterface gameView) {
        this.gameView = gameView;
        this.numDays = gameView.getNumDays();
    }

    @Override
    public void setLevelDesign(Resources res, GameLevelInterface levelDesign) {
        this.gameView.addBackground(levelDesign.getBaseBackground(res));
        this.gameView.addSprite(levelDesign.getCloudBg1(res));
        this.gameView.addSprite(levelDesign.getCloudBg2(res));
        this.gameView.addSprite(levelDesign.getCloudFg1(res));
        this.gameView.addSprite(levelDesign.getCloudFg2(res));
        this.addIslands(res, levelDesign);
        this.gameView.addSprite(levelDesign.getSeaFg(res,
                0.5f, getSeaHeightRatio(this.numDays),
                0.02f, 0));
    }

    @Override
    public void setHeroSprite(HeroSprite hero) {
        this.hero = hero;
        this.hero.setPosXRatio(this.getHeroPosXRatio());
        this.hero.setLowestYRatio(getHeroLowestPosYRatioToWidth(this.numDays));
        this.gameView.addSprite(this.hero);
    }

    @Override
    public void setHeroToMoveOnY(float posYRatio) {
        this.hero.setToMoving(posYRatio);
    }

    @Override
    public void start() {
        this.gameView.start();
    }

    @Override
    public void stop() {
        this.gameView.stop();
    }

    /* PRIVATE METHODS */
    private void addIslands(Resources res, GameLevelInterface levelDesign) {
        if (this.numDays == 1) {
            addOneIsland(res, this.gameView, levelDesign);
        } else {
            addSevenIslands(res, this.gameView, levelDesign);
        }
    }

    /* PRIVATE FUNCTIONS */
    private float getHeroPosXRatio() {
        if (this.numDays == 1) {
            return 0.5f;
        } else {
            int dayOfWeek = getDayOfWeek();
            return (((dayOfWeek - 1) * 2) + 1f) / 16f;
        }
    }

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

    private static void addOneIsland(Resources res, GameViewInterface gameView, GameLevelInterface levelDesign) {
        gameView.addSprite(levelDesign.getIsland(res, getDayOfWeek(), 0.5f, 1, ISLAND_HEIGHT_RATIO_1D));
    }

    private static int getDayOfWeek() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

    private static float getIslandWidthRatio(int numDays) {
        if (numDays == 1) {
            return IslandSprite.getIslandWidthRatio(ISLAND_HEIGHT_RATIO_1D);
        } else {
            return IslandSprite.getIslandWidthRatio(ISLAND_HEIGHT_RATIO_7D);
        }
    }

    private static float getSeaHeightRatio(int numDays) {
        if (numDays == 1) {
            return (1 - (IslandSprite.getIslandWidthRatio(ISLAND_HEIGHT_RATIO_1D) * 0.05f));
        } else {
            return (1 - (IslandSprite.getIslandWidthRatio(ISLAND_HEIGHT_RATIO_7D) * 0.005f));
        }
    }

    private static float getHeroLowestPosYRatioToWidth(int numDays) {
        return (getIslandWidthRatio(numDays) * HERO_LOWEST_POSITION_X_RATIO);
    }

}
