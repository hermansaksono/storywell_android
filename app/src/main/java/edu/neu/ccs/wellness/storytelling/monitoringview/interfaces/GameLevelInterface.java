package edu.neu.ccs.wellness.storytelling.monitoringview.interfaces;

import android.content.res.Resources;

/**
 * Created by hermansaksono on 2/13/18.
 */

public interface GameLevelInterface {
    GameBackgroundInterface getBaseBackground(Resources res);

    GameSpriteInterface getIsland(Resources res, int dayOfWeek,
                                  float posXRatio, float posYRatio, float scaleRatio);

    GameSpriteInterface getSeaFg(Resources res, float posXRatio, float posYRatio, float rangeXRatio, float rangeYRatio);

    GameSpriteInterface getSeaBg(Resources res, float posXRatio, float posYRatio, float rangeXRatio, float rangeYRatio);

    GameSpriteInterface getCloudBg1(Resources res);

    GameSpriteInterface getCloudBg2(Resources res);

    GameSpriteInterface getCloudFg1(Resources res);

    GameSpriteInterface getCloudFg2(Resources res);
}
