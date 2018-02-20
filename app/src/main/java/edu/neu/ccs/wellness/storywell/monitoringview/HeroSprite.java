package edu.neu.ccs.wellness.storywell.monitoringview;

import android.animation.TimeInterpolator;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
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
    private final float hoverRange = 5; // dp per seconds
    private final float hoverPeriod = 4; // 2 seconds per hover
    private final float movePeriod = 2;  // 2 seconds to reach destination

    /* PRIVATE VARIABLES */
    private HeroStatus status = HeroStatus.HOVER;
    private TimeInterpolator interpolator = new CycleInterpolator(1);
    private Bitmap bitmap;
    private float posXRatio = 0.5f;
    private float posYRatio = 0.5f;
    private float currentPosY = 0;
    private float targetPosY = 0;
    private float posX = 0;
    private float posY = 0;
    private float degree = 0;
    private float minPosY = 1;
    private float maxPosY = 0;
    private float lowestPosYRatioToWidth = 0;
    private int width = 100;
    private int height = 100;
    private float pivotX;
    private float pivotY;
    private Matrix matrix;
    private float angularRotation = 0;


    /* CONSTRUCTOR */
    public HeroSprite (Resources res, int drawableId) {
        Drawable drawable = res.getDrawable(drawableId);
        this.bitmap = WellnessGraphics.drawableToBitmap(drawable);
        this.width = drawable.getMinimumWidth() / 3;
        this.height = drawable.getMinimumHeight() / 3;
        this.matrix = new Matrix();
        this.bitmap = Bitmap.createScaledBitmap(this.bitmap, this.width , this.height, true);
    }

    /* PUBLIC METHODS */
    @Override
    public void onSizeChanged(int width, int height, float density) {
        this.width = height / 2;
        this.height = height / 2;
        this.bitmap = Bitmap.createScaledBitmap(this.bitmap, this.width , this.height, true);

        this.minPosY = height - (width * this.lowestPosYRatioToWidth);
        this.maxPosY = height - this.height;

        this.posX = width * this.posXRatio;
        this.posY = this.minPosY;
        this.currentPosY = this.posY;
        this.pivotX = this.width / 2;
        this.pivotY = this.height;
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
        this.matrix.reset();
        this.matrix.postRotate(this.degree, pivotX, pivotY-300);
        this.matrix.postTranslate(drawPosX, drawPosY);
        canvas.drawBitmap(this.bitmap, this.matrix, null);
    }

    @Override
    public void update(long millisec, float density) {
        if (this.status == HeroStatus.STOP) {

        } else if (this.status == HeroStatus.HOVER) {
            this.updateHover(millisec, density);
        } else if (this.status == HeroStatus.MOVING) {
            this.updateMoving(millisec, density);
        }
    }

    /* PUBLIC METHODS */
    public void setPosXRatio(float posXRatio) {
        this.posXRatio = posXRatio;
    }

    public void setLowestYRatio(float lowestPosYRatio) {
        this.lowestPosYRatioToWidth = lowestPosYRatio;
    }

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
    private void updateHover(float millisec, float density) {
        float normalizedSecs = millisec/(hoverPeriod * 1000);
        float ratio = this.interpolator.getInterpolation(normalizedSecs) - 1;
        float offsetY = this.hoverRange * ratio * density;
        //this.degree = 2 * ratio;
        this.posY = this.currentPosY + offsetY;
    }

    private void updateMoving(float millisec, float density) {
        float normalizedSecs = millisec/(movePeriod * 1000);
        this.currentPosY = this.posY;
        if (normalizedSecs <= 1) {
            float offsetRatio = this.interpolator.getInterpolation(normalizedSecs);
            float offsetY = this.targetPosY * offsetRatio * density;
            this.posY = this.currentPosY + offsetY;
        } else {
            setToHover();
        }
    }
}
