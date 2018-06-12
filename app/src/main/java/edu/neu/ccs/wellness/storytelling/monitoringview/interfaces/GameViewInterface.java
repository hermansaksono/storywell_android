package edu.neu.ccs.wellness.storytelling.monitoringview.interfaces;

import java.util.List;

/**
 * Created by hermansaksono on 2/8/18.
 */

public interface GameViewInterface {

    //void setLevelDesign(Resources res, GameLevelInterface levelDesign);

    int getNumDays();

    void addBackground(GameBackgroundInterface background);

    void removeBackground(GameBackgroundInterface background);

    List<GameBackgroundInterface> getListOfBackground();

    void addSprite(GameSpriteInterface sprite);

    void removeSprite(GameSpriteInterface sprite);

    List<GameSpriteInterface> getListOfSprite();

    void start();

    void resume();

    void pause();

    void stop();

    boolean isPlaying();

    void update(long millisec);

    long getElapsedMillisec();
}
