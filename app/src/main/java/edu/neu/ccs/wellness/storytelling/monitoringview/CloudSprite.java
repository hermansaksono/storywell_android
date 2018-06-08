package edu.neu.ccs.wellness.storytelling.monitoringview;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import edu.neu.ccs.wellness.storytelling.monitoringview.interfaces.GameSpriteInterface;
import edu.neu.ccs.wellness.utils.WellnessGraphics;

/**
 * Created by hermansaksono on 2/12/18.
 */

public class CloudSprite implements GameSpriteInterface {

    /* STATIC VALUES */
    private static final float PADDING_RATIO = 2;

    /* PRIVATE VARIABLES */
    private Bitmap bitmap;
    private float origX = 0;
    private float origY = 0;
    private float posX = 0;
    private float posY = 0;
    private int width = 100;
    private int height = 100;
    private float totalWidth;
    private float pivotX;
    private float pivotY;
    private float speedX; // move horizontally by speedX every seconds
    private float paddingLeft;
    private float paddingRight;

    private float posXRatio;
    private float posYRatio;
    private float speedXRatio;

    /* CONSTRUCTOR */
    public CloudSprite(Resources res, int drawableId, float posXRatio, float posYRatio, float speedXRatio) {
        Drawable drawable = res.getDrawable(drawableId);
        this.bitmap = WellnessGraphics.drawableToBitmap(drawable);
        this.posXRatio = posXRatio;
        this.posYRatio = posYRatio;
        this.speedXRatio = speedXRatio;
    }

    /* PUBLIC METHODS */
    @Override
    public void onSizeChanged(int width, int height, float density) {
        this.width = getSize(width, height);
        this.height = this.width;
        this.bitmap = Bitmap.createScaledBitmap(this.bitmap, this.width , this.height, true);

        this.paddingLeft = width * PADDING_RATIO;
        this.paddingRight = width * PADDING_RATIO;
        this.totalWidth = this.width + this.paddingLeft;
        this.pivotX = -this.totalWidth / 2; // TODO this will only work for negative speed
        this.pivotY = this.height / 2;

        this.origX = (width * this.posXRatio) + this.pivotX;
        this.origY = (height * this.posYRatio);
        this.posX = this.origX;
        this.posY = this.origY;

        this.speedX = (width * this.speedXRatio) / 1000;

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
        float drawPosX = this.posX - this.pivotX;
        float drawPosY = this.posY - this.pivotY;
        canvas.drawBitmap(this.bitmap, drawPosX, drawPosY, null);
    }

    @Override
    public void update(long millisec, float density) {
        float secs = millisec / 1000f;
        float offsetX = this.speedX * secs * density;
        float newPosX = this.origX + offsetX;

        this.posX = (newPosX % this.totalWidth);
    }

    @Override
    public boolean isOver(float posX, float posY) {
        return false;
    }

    /* PRIVATE STATIC FUNCTIONS */
    private static int getSize(int width, int height) {
        return Math.min(width, height);
    }
}
