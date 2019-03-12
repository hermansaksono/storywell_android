package edu.neu.ccs.wellness.storytelling.monitoringview;

import android.animation.TimeInterpolator;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.animation.CycleInterpolator;

import edu.neu.ccs.wellness.storytelling.monitoringview.interfaces.GameSpriteInterface;
import edu.neu.ccs.wellness.utils.WellnessGraphics;

/**
 * Created by hermansaksono on 2/12/18.
 */

public class SeaSprite implements GameSpriteInterface {


    /* PRIVATE VARIABLES */
    private Bitmap bitmap;
    private Drawable drawable;
    private TimeInterpolator interpolator;
    private float origX = 0;
    private float origY = 0;
    private float posX = 0;
    private float posY = 0;
    private int width = 100;
    private int height = 100;
    private float pivotX;
    private float pivotY;
    private float posXRatio = 0.5f;
    private float posYRatio = 1f;
    private float wavePeriod = 3; // four seconds to complete one wave
    private float waveWidth; // the wave goes left and right by this dp
    private float waveHeight; // the wave goes up and down by this dp
    private float rangeXRatio; // the wave goes left and right by this dp
    private float rangeYRatio; // the wave goes up and down by this dp

    /* CONSTRUCTOR */
    public SeaSprite (Resources res, int drawableId,
                      float posXRatio, float posYRatio,
                      float rangeXRatio, float rangeYRatio) {
        this.drawable = res.getDrawable(drawableId);
        // this.bitmap = WellnessGraphics.drawableToBitmap(drawable);
        this.interpolator = new CycleInterpolator(1);
        this.posXRatio = posXRatio;
        this.posYRatio = posYRatio;
        this.rangeXRatio = rangeXRatio;
        this.rangeYRatio = rangeYRatio;
    }

    /* PUBLIC METHODS */
    @Override
    public void onSizeChanged(int width, int height, float density) {
        this.width = getWidth(width, this.rangeXRatio, density);
        this.height = this.width;
        this.origX = width * this.posXRatio;
        this.origY = height * this.posYRatio;
        this.posX = this.origX;
        this.posY = this.origY;
        this.pivotX = this.width / 2;
        this.pivotY = this.height / 2;
        this.waveWidth = width * rangeXRatio;
        this.waveHeight = height * rangeYRatio;
        //this.bitmap = Bitmap.createScaledBitmap(this.bitmap, this.width , this.height, true);
        this.bitmap = WellnessGraphics.drawableToBitmap(this.drawable, this.width, this.height);
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
        float normalizedSecs = millisec / (this.wavePeriod * 1000);
        float ratio = this.interpolator.getInterpolation(normalizedSecs);
        float offsetX = this.waveWidth * ratio * density;
        float offsetY = this.waveHeight * ratio * density;
        this.posX = this.origX + offsetX;
        this.posY = this.origY + offsetY;
    }

    @Override
    public boolean isOver(float posX, float posY) {
        return false;
    }

    /* PRIVATE STATIC METHODS */
    private static int getWidth(int canvasWidth, float rangeXRatio, float density) {
        return (int) (canvasWidth * (1 + (2 * rangeXRatio * density)));
    }
}
