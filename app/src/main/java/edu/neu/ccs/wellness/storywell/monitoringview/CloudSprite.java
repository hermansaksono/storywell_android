package edu.neu.ccs.wellness.storywell.monitoringview;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import edu.neu.ccs.wellness.storywell.interfaces.GameSpriteInterface;
import edu.neu.ccs.wellness.utils.WellnessGraphics;

/**
 * Created by hermansaksono on 2/12/18.
 */

public class CloudSprite implements GameSpriteInterface {


    /* PRIVATE VARIABLES */
    private Bitmap bitmap;
    private float origX = 0;
    private float origY = 0;
    private float posX = 0;
    private float posY = 0;
    private int width = 100;
    private int height = 100;
    private float pivotX;
    private float pivotY;
    private float speedX; // move horizontally by speedX every seconds
    private float speedXPerFrame;
    private float paddingX;

    /* CONSTRUCTOR */
    public CloudSprite(Resources res, int drawableId, float posX, float posY, float scale, float speedX) {
        Drawable drawable = res.getDrawable(drawableId);
        this.bitmap = WellnessGraphics.drawableToBitmap(drawable);
        this.width = (int) (drawable.getMinimumWidth() * scale);
        this.height = (int) (drawable.getMinimumHeight() * scale);
        this.pivotX = this.width / 2;
        this.pivotY = this.height / 2;
        this.bitmap = Bitmap.createScaledBitmap(this.bitmap, this.width , this.height, true);
        this.origX = posX;
        this.origY = posY;
        this.speedX = speedX;
        this.speedXPerFrame = speedX / 1000;
        this.paddingX = this.width;
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
    public float getAngularRotation() { /* DO NOTHING */ return 0; }

    @Override
    public void setAngularRotation(float degree) { /* DO NOTHING */ }

    @Override
    public void draw(Canvas canvas) {
        float drawPosX = this.posX + this.paddingX;
        float drawPosY = this.posY - this.pivotY;
        canvas.drawBitmap(this.bitmap, drawPosX, drawPosY, null);
    }

    @Override
    public void update(long millisec, float density) {
        float secs = millisec / 1000f;
        float offsetX = this.speedX * secs * density;
        float newPosX = this.origX + offsetX;

        this.posX = newPosX % (this.width + this.paddingX);
        this.posY = this.origY;
    }
}
