package edu.neu.ccs.wellness.storywell.monitoringview;

import android.animation.TimeInterpolator;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.animation.CycleInterpolator;
import android.view.animation.OvershootInterpolator;

import edu.neu.ccs.wellness.storywell.interfaces.GameSpriteInterface;
import edu.neu.ccs.wellness.utils.WellnessGraphics;

/**
 * Created by hermansaksono on 2/9/18.
 */

public class HeroSprite implements GameSpriteInterface {
    /* ENUM */
    public enum HeroStatus {
        STOP, HOVER, MOVING
    }

    /* STATIC VARIABLES */
    private final float hoverRange = 20; // dp per seconds
    private final float hoverPeriod = 2; // 2 seconds per hover
    private final float movePeriod = 2; // 2 seconds to reach destination

    /* PRIVATE VARIABLES */
    private HeroStatus status = HeroStatus.STOP;
    private TimeInterpolator interpolator;
    Bitmap bitmap;
    Drawable drawable;
    float currentPosY = 0;
    float targetPosY = 0;
    float posX = 0;
    float posY = 0;
    float pivotX;
    float pivotY;
    float angularRotation = 0;
    float speedDpps = 10;


    /* CONSTRUCTOR */
    public HeroSprite (Resources res, int drawableId) {
        Drawable drawable = res.getDrawable(drawableId);
        this.bitmap = WellnessGraphics.drawableToBitmap(drawable);
        this.bitmap = Bitmap.createScaledBitmap(this.bitmap, bitmap.getWidth()/3, bitmap.getHeight()/3, true);
        this.pivotX = this.bitmap.getWidth() / 2;
        this.pivotY = this.bitmap.getHeight() / 2;
    }

    /* PUBLIC METHODS */
    @Override
    public void onSizeChanged(int width, int height) {
        this.posX = width / 2;
        this.posY = height / 2;
        this.currentPosY = this.posY;
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
        float drawPosX = this.posX - this.pivotX;
        float drawPosY = this.posY - this.pivotY;
        canvas.drawBitmap(this.bitmap, drawPosX, drawPosY, null);
    }

    @Override
    public void update(long millisec) {
        if (this.status == HeroStatus.STOP) {

        } else if (this.status == HeroStatus.HOVER) {
            this.updateHover(millisec);
        } else if (this.status == HeroStatus.MOVING) {
            this.updateMoving(millisec);
        }
    }

    /* PUBLIC METHODS */
    public void setToStop() {
        this.interpolator = null;
        this.currentPosY = this.posY;
        this.status = HeroStatus.STOP;
    }

    public void setToHover() {
        this.interpolator = new CycleInterpolator(1);
        this.currentPosY = this.posY;
        this.status = HeroStatus.HOVER;
    }

    public void setToMoving(int offsetY) {
        this.interpolator = new OvershootInterpolator();
        this.currentPosY = this.posY;
        this.targetPosY = -offsetY;
        this.status = HeroStatus.MOVING;
    }

    /* PRIVATE HELPER FUNCTIONS */
    private void updateHover(float millisec) {
        float normalizedSecs = millisec/(hoverPeriod * 1000);
        float hoverY = this.interpolator.getInterpolation(normalizedSecs);
        float offsetY = this.hoverRange * hoverY;
        this.posY = this.currentPosY + offsetY;
    }

    private void updateMoving(float millisec) {
        float normalizedSecs = millisec/(movePeriod * 1000);
        if (normalizedSecs <= 1) {
            float offsetRatio = this.interpolator.getInterpolation(normalizedSecs);
            float offsetY = this.targetPosY * offsetRatio;
            this.posY = this.currentPosY + offsetY;
        } else {
            setToHover();
        }

    }
}
