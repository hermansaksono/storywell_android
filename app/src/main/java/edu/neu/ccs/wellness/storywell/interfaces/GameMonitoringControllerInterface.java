package edu.neu.ccs.wellness.storywell.interfaces;

import android.content.res.Resources;

import edu.neu.ccs.wellness.storywell.monitoringview.HeroSprite;

/**
 * Created by hermansaksono on 2/15/18.
 */

public interface GameMonitoringControllerInterface {

    void setLevelDesign(Resources res, GameLevelInterface levelDesign);

    void setHeroSprite(HeroSprite hero);

    void setHeroToMoveOnY(float posYRatio);

    void start();

    void stop();
}
