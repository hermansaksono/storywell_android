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
    // private Bitmap[] bitmapAnim = new Bitmap[Constants.SUNRAY_ROTATE_STEP];
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
    private int opacity = 0;

    private TimeInterpolator interpolator = new AccelerateInterpolator();

    private boolean isFadeIn = false;
    private long fadeInStartTime = 0;

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

        /*
        float numDegrees = 0f / Constants.SUNRAY_ROTATE_STEP;
        Matrix matrix = new Matrix();
        for (int i = 0; i < Constants.SUNRAY_ROTATE_STEP; i++) {
            matrix.setRotate(numDegrees, this.pivotX, this.pivotY);
            numDegrees += 1;
            this.bitmapAnim[i] = Bitmap.createBitmap(
                    this.bitmap, 0, 0, this.width , this.height, matrix, true);
        }
        */
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
        float drawPosY = this.posY - this.pivotY;
        canvas.drawBitmap(this.bitmap, drawPosX, drawPosY, this.paint);
        // canvas.drawBitmap(this.bitmapAnim[this.rotationStep], drawPosX, drawPosY, this.paint);
    }

    @Override
    public void update(long millisec, float density) {
        if (this.isFadeIn) {
            updateForFadeIn(millisec);
        }

        /*
        if (this.isRotating) {
            updateForRotating();
        }
        */
    }

    private void updateForFadeIn(long millisec) {
        float normalizedTime = (millisec - this.fadeInStartTime)
                / (Constants.SUNRAY_FADEIN_SECONDS * Constants.MICROSECONDS);
        Log.d("SWELL", "millisec: " + millisec);
        Log.d("SWELL", "fadeInStartTime: " + fadeInStartTime);
        Log.d("SWELL", "normalizedTime: " + normalizedTime);
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
            this.rotationStep = 0;
        }
        Log.d("SWELL", "rotating: " + this.rotationStep);
    }

    public void startFadeIn(long startMillisec) {
        this.isFadeIn = true;
        this.fadeInStartTime = startMillisec;
        this.isRotating = true;
        this.rotationStep = 0;
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
