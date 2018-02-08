package edu.neu.ccs.wellness.storywell.interfaces;

import java.util.List;

/**
 * Created by hermansaksono on 2/8/18.
 */

public interface GameViewInterface {

    void addBackground(GameBackgroundInterface background);

    void removeBackground(GameBackgroundInterface background);

    List<GameBackgroundInterface> getListOfBackground();

    void addSprite(GameSpriteInterface sprite);

    void removeSprite(GameSpriteInterface sprite);

    List<GameSpriteInterface> getListOfSprite();
}
