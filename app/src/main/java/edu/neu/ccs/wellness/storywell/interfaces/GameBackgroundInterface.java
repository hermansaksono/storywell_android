package edu.neu.ccs.wellness.storywell.interfaces;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

/**
 * Created by hermansaksono on 2/8/18.
 */

public interface GameBackgroundInterface {

    Drawable getDrawable();

    void onAttach(int width, int height);

    void onSizeChanged(int width, int height);

    float getSpeedX();

    float getSpeedY();

    void setSpeedX(float dpSpeedX);

    void setSpeedY(float dpSpeedY);

    void draw(Canvas canvas);

    void update();
}
