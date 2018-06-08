package edu.neu.ccs.wellness.storytelling.monitoringview;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;

import edu.neu.ccs.wellness.storytelling.monitoringview.interfaces.GameBackgroundInterface;

/**
 * Created by hermansaksono on 2/8/18.
 */

public class SolidBackground implements GameBackgroundInterface {

    /* PRIVATE VARIABLES */
    ShapeDrawable shape;
    float speedX = 0;
    float speedY = 0;
    private Rect rectangle;
    private Paint paint;

    /* CONSTRUCTOR */
    public SolidBackground(int color) {
        // TODO
        // newInstance a rectangle that we'll draw later
        rectangle = new Rect(0, 0, 100, 100);

        // newInstance the Paint and set its color
        paint = new Paint();
        paint.setColor(color);
    }

    /* PUBLIC METHOD */
    @Override
    public Drawable getDrawable() { return shape; }

    @Override
    public void onAttach(int width, int height) {
        this.rectangle.set(0, 0, width, height);
    }

    @Override
    public void onSizeChanged(int width, int height) {
        this.rectangle.set(0, 0, width, height);
    }

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
        canvas.drawRect(rectangle, paint);
    }

    @Override
    public void update() {
        // TODO
    }
}
