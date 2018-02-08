package edu.neu.ccs.wellness.storywell.monitoringview;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;

import edu.neu.ccs.wellness.storywell.interfaces.GameBackgroundInterface;

/**
 * Created by hermansaksono on 2/8/18.
 */

public class SolidBackground implements GameBackgroundInterface {

    /* PRIVATE VARIABLES */
    ShapeDrawable shape;
    float speedX = 0;
    float speedY = 0;

    /* CONSTRUCTOR */
    public SolidBackground(int color) {
        // TODO
    }

    /* PUBLIC METHOD */
    @Override
    public Drawable getDrawable() { return shape; }

    @Override
    public float getSpeedX() { return speedX; }

    @Override
    public float getSpeedY() { return speedY; }

    @Override
    public void setSpeedX(float dpSpeedX) { } // Do nothing

    @Override
    public void setSpeedY(float dpSpeedY) { } // Do nothing

    @Override
    public void draw(Canvas canvas) {
        shape.draw(canvas);
    }
}
