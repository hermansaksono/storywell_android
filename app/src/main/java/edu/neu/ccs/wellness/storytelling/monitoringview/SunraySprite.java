package edu.neu.ccs.wellness.storytelling.monitoringview;
import android.animation.TimeInterpolator;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;

import edu.neu.ccs.wellness.storytelling.monitoringview.interfaces.GameSpriteInterface;
import edu.neu.ccs.wellness.utils.WellnessGraphics;

/**
 * Created by hermansaksono on 2/13/19.
 */

public class SunraySprite implements GameSpriteInterface {
    private Drawable drawable;
    private Bitmap bitmap;
    private Bitmap[] bitmapAnim;
    private final float rotationSpeed;
    private final float posXRatio;
    private final float posYRatio;
    private float posX = 0;
    private float posY = 0;
    private float pivotX;
    private float pivotY;
    private int width = 100;
    private int height = 100;
    private Paint paint;
    private final float density;

    private TimeInterpolator interpolator = new AccelerateInterpolator();

    private boolean isFadeIn = false;
    private int opacity = 0;
    private long fadeInStartTime = 0;

    private boolean isMovingUp = false;
    private long movingUpStartTime = 0;
    private float offsetYUp = 0;

    private int rotationStep = 0;
    private boolean isRotating = false;

    public SunraySprite(Resources res, int drawableId, float posXRatio, float posYRatio, float rotationSpeed) {
        this.drawable = res.getDrawable(drawableId);
        this.bitmap = WellnessGraphics.drawableToBitmap(drawable);
        this.posXRatio = posXRatio;
        this.posYRatio = posYRatio;
        this.rotationSpeed = rotationSpeed;
        this.paint = new Paint();
        this.paint.setAlpha(opacity);
        this.density = res.getDisplayMetrics().density;
    }

    @Override
    public void onSizeChanged(int width, int height, float density) {
        this.width = getSize(width, height) * 3;
        this.height = this.width;
        this.bitmap = Bitmap.createScaledBitmap(this.bitmap, this.width , this.height, true);
        this.posX = width * this.posXRatio;
        this.posY = height * this.posYRatio;
        this.pivotX = this.width / 2;
        this.pivotY = this.height /2;

        // int steps = Constants.SUNRAY_ROTATE_STEP;
        int steps = 1;

        this.bitmapAnim = getBitmapAnimArray(this.bitmap, this.width, this.height, steps);
    }

    private Bitmap[] getBitmapAnimArray(Bitmap bitmap, int width, int height, int steps) {
        Bitmap[] bitmaps = new Bitmap[steps];
        if (steps <= 1) {
            bitmaps[0] = bitmap;
        } else {
            float numDegrees = 0f / steps;
            Matrix matrix = new Matrix();

            for (int i = 0; i < steps; i++) {
                matrix.postRotate(numDegrees, width / 2, height / 2);
                numDegrees += 1;
                bitmaps[i] = Bitmap.createBitmap(
                        bitmap, 0, 0, width , height, matrix, true);
            }
        }

        return bitmaps;
    }

    @Override
    public float getPositionX() {
        return 0;
    }

    @Override
    public void setPositionX(float posX) {

    }

    @Override
    public float getPositionY() {
        return 0;
    }

    @Override
    public void setPositionY(float posY) {

    }

    @Override
    public float getAngularRotation() {
        return 0;
    }

    @Override
    public void setAngularRotation(float degree) {

    }

    @Override
    public void draw(Canvas canvas) {
        float drawPosX = this.posX - this.pivotX;
        float drawPosY = this.posY - this.pivotY - (this.offsetYUp * this.density);
        canvas.drawBitmap(this.bitmapAnim[0], drawPosX, drawPosY, this.paint);
        // canvas.drawBitmap(this.bitmapAnim[this.rotationStep], drawPosX, drawPosY, this.paint);
    }

    @Override
    public void update(long millisec, float density) {
        if (this.isFadeIn) {
            updateForFadeIn(millisec);
        }

        if (this.isRotating) {
            updateForRotating();
        }

        if (this.isMovingUp) {
            updateForMovingUp(millisec);
        }
    }

    private void updateForFadeIn(long millisec) {
        float normalizedTime = (millisec - this.fadeInStartTime)
                / (Constants.SUNRAY_FADEIN_SECONDS * Constants.MICROSECONDS);
        if (normalizedTime < 1) {
            this.opacity = (int) (this.interpolator.getInterpolation(normalizedTime) * 100);
        } else {
            this.opacity = 100;
            this.isFadeIn = false;
        }
        this.paint.setAlpha(opacity);
    }

    private void updateForRotating() {
        if (this.rotationStep < Constants.SUNRAY_ROTATE_STEP - 1) {
            this.rotationStep += 1;
        } else {
            this.isRotating = false;
        }
    }

    private void updateForMovingUp(long millisec) {
        float normalizedTime = (millisec - this.movingUpStartTime)
                / (Constants.SUNRAY_MOVE_UP_SECONDS * Constants.MICROSECONDS);
        if (normalizedTime < 1) {
            this.offsetYUp = normalizedTime * Constants.SUNRAY_MAX_UP;
        } else {
            this.isMovingUp = false;
        }
    }

    /* PUBLIC METHODS */
    public void startFadeIn(long startMillisec) {
        this.isFadeIn = true;
        this.fadeInStartTime = startMillisec;
        this.isRotating = true;
        this.rotationStep = 0;
        this.isMovingUp = true;
        this.movingUpStartTime = startMillisec;
    }

    public void reset() {
        this.isFadeIn = false;
        this.opacity = 0;
        this.isRotating = false;
        this.rotationStep = 0;
        this.isMovingUp = false;
        this.offsetYUp = 0;
        this.paint.setAlpha(this.opacity);
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
