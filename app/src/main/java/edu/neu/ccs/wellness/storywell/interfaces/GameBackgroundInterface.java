package edu.neu.ccs.wellness.storywell.interfaces;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

/**
 * Created by hermansaksono on 2/8/18.
 */

public interface GameBackgroundInterface {

    Drawable getDrawable();

    float getSpeedX();

    float getSpeedY();

    void setSpeedX(float dpSpeedX);

    void setSpeedY(float dpSpeedY);

    void draw(Canvas canvas);

    void update();
}
