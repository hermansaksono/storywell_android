package edu.neu.ccs.wellness.storytelling.monitoringview.interfaces;

import android.content.res.Resources;

import edu.neu.ccs.wellness.storytelling.monitoringview.HeroSprite;

/**
 * Created by hermansaksono on 2/15/18.
 */

public interface GameMonitoringControllerInterface {

    void setLevelDesign(Resources res, GameLevelInterface levelDesign);

    void setHeroSprite(HeroSprite hero);

    void setHeroToMoveOnY(float posYRatio);

    void setProgress(float adult, float child, float total, OnAnimationCompletedListener listener);

    void resetProgress();

    void start();

    void stop();
}
