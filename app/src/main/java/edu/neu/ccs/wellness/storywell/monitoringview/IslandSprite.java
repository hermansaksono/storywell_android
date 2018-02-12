package edu.neu.ccs.wellness.storywell.monitoringview;

import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;

import edu.neu.ccs.wellness.storywell.interfaces.GameSpriteInterface;

/**
 * Created by hermansaksono on 2/8/18.
 */

public class IslandSprite implements GameSpriteInterface {

    BitmapDrawable bitmap;
    float posX = 0;
    float posY = 0;
    float angularRotation = 0;

    /* CONSTRUCTOR */
    public IslandSprite (int imageResId) {
        // TODO
    }

    /* PUBLIC METHODS */
    @Override
    public void onSizeChanged(int width, int height) {

    }

    @Override
    public float getPositionX() { return this.posX; }

    @Override
    public void setPositionX(float posX) { this.posX = posX; }

    @Override
    public float getPositionY() { return this.posY; }

    @Override
    public void setPositionY(float posY) { this.posY = posY; }

    @Override
    public float getAngularRotation() { return this.angularRotation; }

    @Override
    public void setAngularRotation(float degree) { this.angularRotation = degree; }

    @Override
    public void draw(Canvas canvas) {
        bitmap.draw(canvas);
    }

    @Override
    public void update(float seconds) {
        // TODO
    }
}
