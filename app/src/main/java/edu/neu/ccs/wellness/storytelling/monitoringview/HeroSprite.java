package edu.neu.ccs.wellness.storytelling.monitoringview;

import android.animation.TimeInterpolator;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.CycleInterpolator;

import edu.neu.ccs.wellness.storytelling.monitoringview.interfaces.GameSpriteInterface;
import edu.neu.ccs.wellness.storytelling.monitoringview.interfaces.OnAnimationCompletedListener;
import edu.neu.ccs.wellness.utils.WellnessGraphics;

/**
 * Created by hermansaksono on 2/9/18.
 */

public class HeroSprite implements GameSpriteInterface {

    /* ENUM */
    public enum HeroStatus {
        STOP, UPDATING, HOVER, MOVING_LINEAR, MOVING_PARABOLIC, COMPLETED
    }

    /* STATIC VARIABLES */
    private final static float ARC_INIT_ANGLE = 181f;
    private final static float ARC_MAX_SWEEP = 178f;
    private final static int ARC_STROKE_WIDTH = 3;

    /* PRIVATE VARIABLES */
    private Resources res;
    private HeroStatus status = HeroStatus.STOP;
    private TimeInterpolator interpolator = new CycleInterpolator(1);
    private OnAnimationCompletedListener animationCompletedListener;
    private float animationStart = 0;
    private int[] heroDrawableArray;
    private int heroDrawableId;
    private Bitmap heroBitmap;
    private int numAdultBalloons = 0;
    private int maxAdultBalloons = 1;
    private int targetAdultBalloons = 0;
    private int[] adultBalloonDrawables;
    private Bitmap adultBalloonBmp;
    private int numChildBalloons = 0;
    private int maxChildBalloons = 1;
    private int targetChildBalloons = 0;
    private int[] childBalloonDrawables;
    private Bitmap childBalloonBmp;
    private float targetRatio = 0f;
    private float currentPosX = 0;
    private float currentPosY = 0;
    private float posX = 0;
    private float posY = 0;
    private float degree = 0;
    private float mainAnimationTime = Constants.ANIM_MOVING_PERIOD;

    private float absCenterX;
    private float absCenterY;
    private float absRadiusX;
    private float absRadiusY;

    private float offsetToTargetPosY = 0;

    private int width;
    private int height;
    private boolean isVisible = true;
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

    private boolean isUpdatingGapSweep = false;
    private float arcCurrentSweep = 0;
    private float arcGapSweep = 0;
    private RectF arcRect;
    private Paint arcCurrentPaint;
    private Paint arcGapPaint;
    private int gapAnimationStart;
    private float arcGapPeriod = 0;


    /* CONSTRUCTOR */
    public HeroSprite (Resources res, int[] heroDrawableIds, int[] adultBalloonIds, int[] childBalloonIds, int colorId) {
        this.res = res;
        this.heroDrawableArray = heroDrawableIds;
        this.heroDrawableId = this.heroDrawableArray[Constants.HERO_DRAWABLE_FLYING];
        float density = res.getDisplayMetrics().density;
        float strokeWidth = ARC_STROKE_WIDTH * density;

        this.adultBalloonDrawables = adultBalloonIds;
        this.maxAdultBalloons = adultBalloonIds.length;
        this.childBalloonDrawables = childBalloonIds;
        this.maxChildBalloons = childBalloonIds.length;
        this.arcCurrentPaint = getCurrentArcPaint(res.getColor(colorId), strokeWidth);
        this.arcGapPaint = getGapArcPaint(res.getColor(colorId), strokeWidth);
    }

    /* PUBLIC METHODS */
    @Override
    public void onSizeChanged(int width, int height, float density) {
        this.width = height / 2;
        this.height = height / 2;
        this.heroBitmap = getBitmap(this.res, this.heroDrawableId, this.width , this.height);
        this.updateAdultBalloonDrawable();
        this.updateChildBalloonDrawable();

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
        Rect rect = this.getRect();
        canvas.drawArc(arcRect, ARC_INIT_ANGLE, arcCurrentSweep, false, arcCurrentPaint);
        if (this.isGapSweepNeedsToBeDrawn()) {
            canvas.drawArc(arcRect, ARC_INIT_ANGLE + arcCurrentSweep, arcGapSweep, false, arcGapPaint);
        }
        if (this.isVisible) {
            canvas.drawBitmap(this.adultBalloonBmp, null, rect, null);
            canvas.drawBitmap(this.childBalloonBmp, null, rect, null);
            canvas.drawBitmap(this.heroBitmap, null, rect, null);
        }
    }

    @Override
    public void update(long millisec, float density) {
        switch (this.status) {
            case STOP:
                break;
            case UPDATING:
                this.updateBalloons(millisec);
                break;
            case HOVER:
                this.updateHover(millisec);
                break;
            case MOVING_LINEAR:
                this.updateMovingLinear(millisec, this.mainAnimationTime);
                break;
            case MOVING_PARABOLIC:
                this.updateMovingParabolic(millisec, this.mainAnimationTime);
                break;
            case COMPLETED:
                break;
            default:
                break;
        }
    }

    @Override
    public boolean isOver(float posX, float posY) {
        return this.isOverX(posX) && this.isOverY(posY);
    }

    /* PUBLIC METHODS */
    public void reset() {
        this.animationStart = 0;
        this.posX = 0;//this.width * this.closestPosXRatioToWidth;
        this.posY = 0;//this.absLowestPosY;

        float angleRad = (float) Math.toRadians(ARC_INIT_ANGLE);
        this.posX = (float) (this.absCenterX + this.absRadiusX * Math.cos(angleRad));
        this.posY = (float) (this.absCenterY + this.absRadiusY * Math.sin(angleRad));
        this.setToStop();

        this.numAdultBalloons = 0;
        this.numChildBalloons = 0;
        this.updateAdultBalloonDrawable();
        this.updateChildBalloonDrawable();

        this.arcCurrentSweep = 0;
        this.arcGapSweep = 0;
    }

    public boolean getIsVisible() {
        return this.isVisible;
    }

    public void setIsVisible(boolean isVisible) {
        this.isVisible = isVisible;
    }

    public float getMainAnimationTimeInSeconds() { return this.mainAnimationTime; }

    public void setMainAnimationTimeInSeconds(float timeInSeconds) {
        this.mainAnimationTime = timeInSeconds;
    }

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

    public void setToUpdateBalloons(float adultProgress, float childProgress, float overallProgress) {
        this.targetRatio = overallProgress;
        this.targetAdultBalloons = (int) Math.floor(adultProgress * this.maxAdultBalloons);
        this.targetChildBalloons = (int) Math.floor(childProgress * this.maxChildBalloons);
        this.status = HeroStatus.UPDATING;
        this.interpolator = new AccelerateDecelerateInterpolator();
    }

    public void setToMoveParabolic(float adultProgress, float childProgress, float overallProgress,
                                   long startMillisec,
                                   OnAnimationCompletedListener animationCompletedListener) {
        this.targetAdultBalloons = (int) Math.floor(adultProgress * this.maxAdultBalloons);
        this.targetChildBalloons = (int) Math.floor(childProgress * this.maxChildBalloons);
        this.animationCompletedListener = animationCompletedListener;
        this.setToMoveParabolic(overallProgress, startMillisec);
    }

    public void setToMoveParabolic(float overallProgress, long startMillisec) {
        this.targetRatio = overallProgress;
        this.status = HeroStatus.MOVING_PARABOLIC;
        this.interpolator = new AccelerateDecelerateInterpolator();
        this.animationStart = startMillisec;
    }

    public void setToMoveUpRel(float posYRatio) {
        int offsetY = (int) (this.absRangeY * posYRatio);
        int normalizedOffsetY = offsetY - (int) (this.absLowestPosY - this.currentPosY);
        this.currentPosY = this.posY;
        this.offsetToTargetPosY = - normalizedOffsetY;
        this.status = HeroStatus.MOVING_LINEAR;
        this.interpolator = new AccelerateDecelerateInterpolator();
    }

    public void setToCompleted() {
        this.status = HeroStatus.COMPLETED;
        this.updateBitmapToCompleted();
    }


    /* PRIVATE HELPER FUNCTIONS */
    public boolean isOverX(float posX) {
        float posXStart = this.posX - this.pivotX;
        float posXEnd = posXStart + this.width;
        return (posXStart <= posX) && (posX <= posXEnd);
    }

    public boolean isOverY(float posY) {
        float posYStart = this.posY - this.pivotY;
        float posYEnd = posYStart + this.width;
        return (posYStart <= posY) && (posY <= posYEnd);
    }

    private Rect getRect() {
        float drawPosX = this.posX - this.pivotX;
        float drawPosY = this.posY - this.pivotY;
        return new Rect(
                (int)drawPosX, (int)drawPosY,
                (int)(drawPosX + this.width), (int)(drawPosY + this.height));
    }

    private void updateBalloons(float millisec) {
        float normalizedSecs = millisec
                / (Constants.ANIM_BALLOON_UPDATE_PERIOD * MonitoringView.MICROSECONDS);
        float interpolatedRatio = this.interpolator.getInterpolation(normalizedSecs);

        int newAdultBalloons = (int) Math.floor(interpolatedRatio * this.maxAdultBalloons);
        int newChildBalloons = (int) Math.floor(interpolatedRatio * this.maxChildBalloons);

        if (newAdultBalloons > this.numAdultBalloons
                && newAdultBalloons <= this.targetAdultBalloons) {
            this.numAdultBalloons = newAdultBalloons;
            this.updateAdultBalloonDrawable();
        }

        if (newChildBalloons > numChildBalloons
                && newChildBalloons <= this.targetChildBalloons) {
            this.numChildBalloons = newChildBalloons;
            this.updateChildBalloonDrawable();
        }

        if (this.numAdultBalloons == this.targetAdultBalloons
                && this.numChildBalloons == this.targetChildBalloons) {
            this.setToMoveParabolic(this.targetRatio, 0);
            this.animationStart = (long) millisec;
        }
    }

    private void updateBalloonsAlong(float millisec) {
        float normalizedSecs = (millisec - this.animationStart) /
                (Constants.ANIM_BALLOON_UPDATE_PERIOD * MonitoringView.MICROSECONDS);
        float interpolatedRatio = this.interpolator.getInterpolation(normalizedSecs);

        int newAdultBalloons = (int) Math.floor(interpolatedRatio * this.maxAdultBalloons);
        int newChildBalloons = (int) Math.floor(interpolatedRatio * this.maxChildBalloons);

        if (newAdultBalloons > this.numAdultBalloons
                && newAdultBalloons <= this.targetAdultBalloons) {
            this.numAdultBalloons = newAdultBalloons;
            this.updateAdultBalloonDrawable();
        }

        if (newChildBalloons > numChildBalloons
                && newChildBalloons <= this.targetChildBalloons) {
            this.numChildBalloons = newChildBalloons;
            this.updateChildBalloonDrawable();
        }
    }

    private void updateHover(float millisec) {
        float normalizedSecs = (millisec - this.animationStart)
                / (Constants.ANIM_HOVER_PERIOD * MonitoringView.MICROSECONDS);
        float interpolatedRatio = this.interpolator.getInterpolation(normalizedSecs);
        float offsetY = Constants.ANIM_HOVER_RANGE * interpolatedRatio;
        this.posY = this.currentPosY + offsetY;
        updateGapSweep(millisec);
    }

    private void updateMovingParabolic(float millisec, float durationInSeconds) {
        float normalizedSecs = (millisec - this.animationStart)
                / (durationInSeconds * MonitoringView.MICROSECONDS);

        this.arcGapSweep = 0;

        if (normalizedSecs <= 1) {
            float interpolatedRatio = this.interpolator.getInterpolation(normalizedSecs);
            float progressRatio = interpolatedRatio * this.targetRatio;
            float angleDeg = (ARC_INIT_ANGLE + (progressRatio * ARC_MAX_SWEEP));
            float angleRad = (float) Math.toRadians(angleDeg);

            this.posX = (float) (this.absCenterX + this.absRadiusX * Math.cos(angleRad));
            this.posY = (float) (this.absCenterY + this.absRadiusY * Math.sin(angleRad));
            this.arcCurrentSweep = ARC_MAX_SWEEP * progressRatio;
            this.updateBalloonsAlong(millisec);
        } else {
            this.isUpdatingGapSweep = true;
            this.gapAnimationStart = (int) millisec;
            this.animationStart = (long) millisec;
            this.arcGapPeriod = Constants.ANIM_ARC_GAP_PERIOD * (1 - this.targetRatio);
            setToHover();
        }
    }

    private void updateMovingLinear(float millisec, float durationInSeconds) {
        float normalizedSecs = millisec
                / (durationInSeconds * MonitoringView.MICROSECONDS);
        if (normalizedSecs <= 1) {
            float offsetRatio = this.interpolator.getInterpolation(normalizedSecs);
            float offsetY = this.offsetToTargetPosY * offsetRatio;
            this.posY = this.currentPosY + offsetY;
        } else {
            setToHover();
        }
    }

    private void updateAdultBalloonDrawable() {
        this.adultBalloonBmp = getBitmap(this.res, this.adultBalloonDrawables[this.numAdultBalloons],
                this.width, this.height);
    }

    private void updateChildBalloonDrawable() {
        this.childBalloonBmp = getBitmap(this.res, this.childBalloonDrawables[this.numChildBalloons],
                this.width, this.height);
    }

    private void updateGapSweep(float millisec) {
        if (this.isUpdatingGapSweep) {
            //float normalizedSecs = (millisec - this.gapAnimationStart) / (ARC_GAP_PERIOD * MonitoringView.MICROSECONDS);
            float normalizedSecs = (millisec - this.gapAnimationStart)
                    / (this.arcGapPeriod * MonitoringView.MICROSECONDS);
            this.arcGapSweep = (ARC_MAX_SWEEP - this.arcCurrentSweep) * normalizedSecs;

            if (this.arcCurrentSweep + this.arcGapSweep >= ARC_MAX_SWEEP) {
                this.isUpdatingGapSweep = false;
                this.runOnAnimationCompleteListener();
            }
        }
    }

    private boolean isGapSweepNeedsToBeDrawn() {
        return this.arcGapSweep != Float.POSITIVE_INFINITY;
    }

    private void runOnAnimationCompleteListener () {
        this.animationCompletedListener.onAnimationCompleted();
        this.animationCompletedListener = null;
    }

    private void updateBitmapToCompleted() {
        this.heroBitmap = getBitmap(
                this.res, this.heroDrawableArray[Constants.HERO_DRAWABLE_COMPLETE],
                this.width, this.height);
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
                new DashPathEffect(new float[]{1 * strokeWidth, 2 * strokeWidth}, 0));
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

    private static Bitmap getBitmap(Resources res, int drawableId, int width, int height) {
        Drawable drawable = res.getDrawable(drawableId);
        Bitmap bitmap = WellnessGraphics.drawableToBitmap(drawable, width, height);
        return bitmap;
    }
}
