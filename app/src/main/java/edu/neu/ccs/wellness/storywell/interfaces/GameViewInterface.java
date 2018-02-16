package edu.neu.ccs.wellness.storywell.interfaces;

import android.content.res.Resources;

import java.util.List;

import edu.neu.ccs.wellness.storywell.monitoringview.GameLevel;

/**
 * Created by hermansaksono on 2/8/18.
 */

public interface GameViewInterface {

    //void setLevelDesign(Resources res, GameLevelInterface levelDesign);

    void addBackground(GameBackgroundInterface background);

    void removeBackground(GameBackgroundInterface background);

    List<GameBackgroundInterface> getListOfBackground();

    void addSprite(GameSpriteInterface sprite);

    void removeSprite(GameSpriteInterface sprite);

    List<GameSpriteInterface> getListOfSprite();

    void start();

    void stop();

    boolean isPlaying();

    void update(long millisec);
}
