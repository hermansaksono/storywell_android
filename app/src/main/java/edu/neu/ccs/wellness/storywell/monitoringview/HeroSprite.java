package edu.neu.ccs.wellness.storywell.monitoringview;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.Log;

import edu.neu.ccs.wellness.storywell.interfaces.GameSpriteInterface;
import edu.neu.ccs.wellness.utils.WellnessGraphics;

/**
 * Created by hermansaksono on 2/9/18.
 */

public class HeroSprite implements GameSpriteInterface {
    /* STATIC VARIABLES */
    private static Paint paint;

    Bitmap bitmap;
    Drawable drawable;
    float posX = 0;
    float posY = 0;
    float width = 0;
    float height = 0;
    float angularRotation = 0;
    float speedDpps = 10;

    /* CONSTRUCTOR */
    public HeroSprite (Resources res, int drawableId) {
        Drawable drawable = res.getDrawable(drawableId);
        this.bitmap = WellnessGraphics.drawableToBitmap(drawable);

    }

    /* PUBLIC METHODS */
    @Override
    public void onSizeChanged(int width, int height) {
        this.bitmap = Bitmap.createScaledBitmap(this.bitmap, bitmap.getWidth()/4, bitmap.getHeight()/4, true);
        this.posX = (width - bitmap.getWidth()) / 2;
        this.posY = (height - bitmap.getHeight()) / 2;
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
        canvas.drawBitmap(this.bitmap, this.posX, this.posY, null);
    }

    @Override
    public void update(float seconds) {
        this.posY = this.posY - this.speedDpps;
        Log.d("WELL", "updating pos Y " + this.posY);
        // TODO
    }
}
