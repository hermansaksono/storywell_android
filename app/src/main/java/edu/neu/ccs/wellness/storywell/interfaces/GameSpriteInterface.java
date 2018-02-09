package edu.neu.ccs.wellness.storywell.interfaces;

import android.graphics.Canvas;

/**
 * Created by hermansaksono on 2/8/18.
 */

public interface GameSpriteInterface {

    float getPositionX();

    void setPositionX(float posX);

    float getPositionY();

    void setPositionY(float posY);

    float getAngularRotation();

    void setAngularRotation(float degree);

    void draw(Canvas canvas);

    void update();
}
