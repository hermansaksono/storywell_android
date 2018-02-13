package edu.neu.ccs.wellness.storywell.monitoringview;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;

import edu.neu.ccs.wellness.storywell.interfaces.GameSpriteInterface;
import edu.neu.ccs.wellness.utils.WellnessGraphics;

/**
 * Created by hermansaksono on 2/8/18.
 */

public class IslandSprite implements GameSpriteInterface {

    /* PRIVATE VARIABLES */
    private Bitmap bitmap;
    private float posX = 0;
    private float posY = 0;
    private int width = 100;
    private int height = 100;
    private float pivotX;
    private float pivotY;
    private String text;
    private Paint textPaint;
    private int fontSize = 14;
    private int textOffsetX;

    /* CONSTRUCTOR */
    public IslandSprite (Resources res, int drawableId, String text) {
        Drawable drawable = res.getDrawable(drawableId);
        this.bitmap = WellnessGraphics.drawableToBitmap(drawable);
        this.width = drawable.getMinimumWidth() / 2;
        this.height = drawable.getMinimumHeight() / 2;
        this.pivotX = this.width / 2;
        this.pivotY = this.height;
        this.bitmap = Bitmap.createScaledBitmap(this.bitmap, this.width , this.height, true);

        this.text = text;
        this.textPaint = new TextPaint();
        this.textPaint.setAntiAlias(true);
        this.textPaint.setTextSize(70);
        this.textPaint.setColor(Color.WHITE);
        this.textPaint.setStyle(Paint.Style.FILL);
        this.textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        this.textOffsetX = getTextOffset(this.text, this.textPaint, this.width);
    }

    /* PUBLIC METHODS */
    @Override
    public void onSizeChanged(int width, int height) {
        this.posX = width / 2;
        this.posY = height;
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
        float drawPosX, drawPosY;

        drawPosX = this.posX - this.pivotX;
        drawPosY = this.posY - this.pivotY;
        canvas.drawBitmap(this.bitmap, drawPosX, drawPosY, null);

        drawPosX = this.posX - this.textOffsetX;
        drawPosY = this.posY - (int) (this.height * 0.4);
        canvas.drawText(this.text, drawPosX, drawPosY, this.textPaint);
    }

    @Override
    public void update(long millisec) {
        // DO NOTHING
    }

    /* PRIVATE STATIC HELPER FUNCTION */
    private static int getTextOffset(String text, Paint paint, int width) {
        float textWidth = paint.measureText(text);
        //return (int)((width-textWidth)/2f) - (int)(paint.getTextSize());
        return (int) (textWidth/2f);
    }
}
