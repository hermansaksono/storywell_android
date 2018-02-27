package edu.neu.ccs.wellness.storywell.monitoringview;

import android.animation.TimeInterpolator;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.CycleInterpolator;

import edu.neu.ccs.wellness.storywell.interfaces.GameSpriteInterface;
import edu.neu.ccs.wellness.utils.WellnessGraphics;

/**
 * Created by hermansaksono on 2/9/18.
 */

public class HeroSprite implements GameSpriteInterface {

    /* ENUM */
    public enum HeroStatus {
        STOP, HOVER, MOVING_LINEAR, MOVING_PARABOLIC
    }

    /* STATIC VARIABLES */
    private final static float ARC_INIT_ANGLE = 181f;
    private final static float ARC_MAX_SWEEP = 178f;
    private final float hoverRange = 5;  // dp per seconds
    private final float hoverPeriod = 4; // seconds per hover
    private final float movePeriod = 4;  // seconds to reach destination
    private final float gapPeriod = 2;   // seconds to reach destination

    /* PRIVATE VARIABLES */
    private HeroStatus status = HeroStatus.STOP;
    private TimeInterpolator interpolator = new CycleInterpolator(1);
    private Bitmap bitmap;
    private float targetRatio = 0f;
    private float currentPosX = 0;
    private float currentPosY = 0;
    private float posX = 0;
    private float posY = 0;
    private float degree = 0;

    private float absCenterX;
    private float absCenterY;
    private float absRadiusX;
    private float absRadiusY;

    private float offsetToTargetPosY = 0;

    private int width;
    private int height;
    private int heroPivot;
    private float absClosestPosX;
    private float absFarthestPosX;
    private float absLowestPosY;  // absolute position the Hero can go down
    private float absHighestPosY; // absolute position the Hero can go highest
    private float absRangeX;
    private float absRangeY;
    private float closestPosXRatioToWidth;
    private float farthestPosXRatioToWidth;
    private float lowestPosYRatioToWidth;
    private float islandHeight;
    private float pivotX;
    private float pivotY;
    private Matrix matrix;
    private float angularRotation = 0;

    private boolean drawGapSweep = false;
    private float arcCurrentSweep = 0;
    private float arcGapSweep = 0;
    private RectF arcRect;
    private Paint arcCurrentPaint;
    private Paint arcGapPaint;
    private int gapAnimationStart;


    /* CONSTRUCTOR */
    public HeroSprite (Resources res, int drawableId, int colorId) {
        Drawable drawable = res.getDrawable(drawableId);
        float density = res.getDisplayMetrics().density;
        this.bitmap = WellnessGraphics.drawableToBitmap(drawable);
        this.width = drawable.getMinimumWidth() / 3;
        this.height = drawable.getMinimumHeight() / 3;
        this.matrix = new Matrix();
        //this.bitmap = Bitmap.createScaledBitmap(this.bitmap, this.width , this.height, true);

        float strokeWidth = 3 * density;
        this.arcCurrentPaint = getCurrentArcPaint(res.getColor(colorId), strokeWidth);
        this.arcGapPaint = getGapArcPaint(res.getColor(colorId), strokeWidth);
    }

    /* PUBLIC METHODS */
    @Override
    public void onSizeChanged(int width, int height, float density) {
        this.width = height / 2;
        this.height = height / 2;
        this.bitmap = Bitmap.createScaledBitmap(this.bitmap, this.width , this.height, true);

        this.heroPivot = (int) (this.height / 7f);
        this.islandHeight = width * this.lowestPosYRatioToWidth;

        this.absClosestPosX = width * this.closestPosXRatioToWidth;
        this.absFarthestPosX = width * this.farthestPosXRatioToWidth;
        this.absLowestPosY = height - this.islandHeight;
        this.absHighestPosY = this.height;
        this.absRangeX = this.absFarthestPosX - this.absClosestPosX;
        this.absRangeY = this.absLowestPosY - this.absHighestPosY;
        this.absCenterX = (this.absRangeX / 2) + this.absClosestPosX;
        this.absCenterY = this.absLowestPosY;
        this.absRadiusX = this.absRangeX / 2;
        this.absRadiusY = this.absRangeY;

        this.arcRect = new RectF(absClosestPosX, absHighestPosY - heroPivot,
                absFarthestPosX, absLowestPosY + absRangeY + heroPivot);

        this.posX = width * this.closestPosXRatioToWidth;
        this.posY = this.absLowestPosY;
        this.currentPosX = this.posX;
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
        canvas.drawArc(arcRect, ARC_INIT_ANGLE, arcCurrentSweep, false, arcCurrentPaint);
        canvas.drawArc(arcRect, ARC_INIT_ANGLE + arcCurrentSweep, arcGapSweep, false, arcGapPaint);
        canvas.drawBitmap(this.bitmap, this.matrix, null);
    }

    @Override
    public void update(long millisec, float density) {
        if (this.status == HeroStatus.STOP) {

        } else if (this.status == HeroStatus.HOVER) {
            this.updateHover(millisec);
        } else if (this.status == HeroStatus.MOVING_LINEAR) {
            this.updateMovingLinear(millisec);
        } else if (this.status == HeroStatus.MOVING_PARABOLIC) {
            this.updateMovingParabolic(millisec);
        }
    }

    /* PUBLIC METHODS */
    public void setClosestPosXRatio(float closestPosXRatio) {
        this.closestPosXRatioToWidth = closestPosXRatio;
    }

    public void setFarthestXRatio(float farthestPosXRatio) {
        this.farthestPosXRatioToWidth = farthestPosXRatio;
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

    public void setToMoveParabolic(float ratio) {
        this.targetRatio = ratio;
        this.status = HeroStatus.MOVING_PARABOLIC;
        this.interpolator = new AccelerateDecelerateInterpolator();
    }

    public void setToMoveUpRel(float posYRatio) {
        int offsetY = (int) (this.absRangeY * posYRatio);
        int normalizedOffsetY = offsetY - (int) (this.absLowestPosY - this.currentPosY);
        this.currentPosY = this.posY;
        this.offsetToTargetPosY = - normalizedOffsetY;
        this.status = HeroStatus.MOVING_LINEAR;
        this.interpolator = new AccelerateDecelerateInterpolator();
    }


    /* PRIVATE HELPER FUNCTIONS */
    private void updateHover(float millisec) {
        float normalizedSecs = millisec/(hoverPeriod * MonitoringView.MICROSECONDS);
        float interpolatedRatio = this.interpolator.getInterpolation(normalizedSecs);
        float offsetY = this.hoverRange * interpolatedRatio;
        this.posY = this.currentPosY + offsetY;
        updateGapSweep(millisec);
    }

    private void updateMovingParabolic(float millisec) {
        float normalizedSecs = millisec/(movePeriod * MonitoringView.MICROSECONDS);

        this.arcGapSweep = 0;

        if (normalizedSecs <= 1) {
            float interpolatedRatio = this.interpolator.getInterpolation(normalizedSecs);
            float progressRatio = interpolatedRatio * this.targetRatio;
            float angleDeg = (ARC_INIT_ANGLE + (progressRatio * ARC_MAX_SWEEP));
            float angleRad = (float) Math.toRadians(angleDeg);

            this.posX = (float) (this.absCenterX + this.absRadiusX * Math.cos(angleRad));
            this.posY = (float) (this.absCenterY + this.absRadiusY * Math.sin(angleRad));
            this.arcCurrentSweep = ARC_MAX_SWEEP * progressRatio;
        } else {
            this.drawGapSweep = true;
            this.gapAnimationStart = (int) millisec;
            setToHover();
        }
    }

    private void updateMovingLinear(float millisec) {
        float normalizedSecs = millisec/(movePeriod * MonitoringView.MICROSECONDS);
        if (normalizedSecs <= 1) {
            float offsetRatio = this.interpolator.getInterpolation(normalizedSecs);
            float offsetY = this.offsetToTargetPosY * offsetRatio;
            this.posY = this.currentPosY + offsetY;
        } else {
            setToHover();
        }
    }

    private void updateGapSweep(float millisec) {
        if (this.drawGapSweep) {
            float normalizedSecs = (millisec - this.gapAnimationStart) / (gapPeriod * MonitoringView.MICROSECONDS);
            this.arcGapSweep = (ARC_MAX_SWEEP - this.arcCurrentSweep) * normalizedSecs;

            if (this.arcCurrentSweep + this.arcGapSweep >= ARC_MAX_SWEEP) {
                this.drawGapSweep = false;
            }
        }
    }

    /* PRIVATE STATIC HELPER METHODS */
    private static Paint getCurrentArcPaint (int color, float strokeWidth) {
        Paint arcPaint =  getArcPaint(color, strokeWidth);
        arcPaint.setPathEffect(
                new DashPathEffect(new float[] {0.5f * strokeWidth, 2 * strokeWidth}, 0));
        return arcPaint;
    }

    private static Paint getGapArcPaint (int color, float strokeWidth) {
        Paint arcPaint = getArcPaint(color, strokeWidth);
        arcPaint.setAlpha(70);
        arcPaint.setPathEffect(
                new DashPathEffect(new float[]{1 * strokeWidth, 5 * strokeWidth}, 0));
        return arcPaint;
    }

    private static Paint getArcPaint(int color, float strokeWidth) {
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setStrokeWidth(strokeWidth);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        return paint;
    }
}
