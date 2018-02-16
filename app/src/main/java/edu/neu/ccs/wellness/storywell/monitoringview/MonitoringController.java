package edu.neu.ccs.wellness.storywell.monitoringview;

import android.content.res.Resources;

import java.util.Calendar;

import edu.neu.ccs.wellness.storywell.interfaces.GameLevelInterface;
import edu.neu.ccs.wellness.storywell.interfaces.GameMonitoringControllerInterface;
import edu.neu.ccs.wellness.storywell.interfaces.GameSpriteInterface;
import edu.neu.ccs.wellness.storywell.interfaces.GameViewInterface;

/**
 * Created by hermansaksono on 2/15/18.
 */

public class MonitoringController implements GameMonitoringControllerInterface {

    private GameViewInterface gameView;
    private HeroSprite hero;
    private int numDays = 1;

    public MonitoringController(GameViewInterface gameView, int numDays) {
        this.gameView = gameView;
        this.numDays = numDays;
    }

    @Override
    public void setLevelDesign(Resources res, GameLevelInterface levelDesign) {
        this.gameView.addBackground(levelDesign.getBaseBackground(res));
        this.addIslands(res, levelDesign);
        this.gameView.addSprite(levelDesign.getCloudBg1(res));
        this.gameView.addSprite(levelDesign.getCloudBg2(res));
        this.gameView.addSprite(levelDesign.getCloudFg1(res));
        this.gameView.addSprite(levelDesign.getCloudFg2(res));
        this.gameView.addSprite(levelDesign.getSeaFg(res,0.02f, 0));
    }

    @Override
    public void setHeroSprite(HeroSprite hero) {
        this.hero = hero;
        this.hero.setPosXRatio(this.getHeroPosXRatio());
        this.gameView.addSprite(this.hero);
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
            int dayOfWeek = 1;//getDayOfWeek();
            return (((dayOfWeek - 1) * 2) + 1f) / 16f;
        }
    }

    /* PRIVATE STATIC FUNCTIONS */
    private static void addSevenIslands(Resources res, GameViewInterface gameView, GameLevelInterface levelDesign) {
        gameView.addSprite(levelDesign.getIsland(res, 1, 1f/16, 1f,1f/8));
        gameView.addSprite(levelDesign.getIsland(res, 2, 3f/16, 1f,1f/8));
        gameView.addSprite(levelDesign.getIsland(res, 3, 5f/16, 1f,1f/8));
        gameView.addSprite(levelDesign.getIsland(res, 4, 7f/16, 1f,1f/8));
        gameView.addSprite(levelDesign.getIsland(res, 5, 9f/16, 1f,1f/8));
        gameView.addSprite(levelDesign.getIsland(res, 6, 11f/16,1f,1f/8));
        gameView.addSprite(levelDesign.getIsland(res, 7, 13f/16,1f,1f/8));
        gameView.addSprite(levelDesign.getIsland(res, 0, 15f/16,1f,1f/8));
    }

    private static void addOneIsland(Resources res, GameViewInterface gameView, GameLevelInterface levelDesign) {
        gameView.addSprite(levelDesign.getIsland(res, getDayOfWeek(), 0.5f, 1, 0.4f));
    }

    private static int getDayOfWeek() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

}
