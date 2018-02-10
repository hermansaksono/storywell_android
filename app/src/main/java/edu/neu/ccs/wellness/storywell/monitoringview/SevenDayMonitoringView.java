package edu.neu.ccs.wellness.storywell.monitoringview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import edu.neu.ccs.wellness.storytelling.R;
import edu.neu.ccs.wellness.storywell.interfaces.GameBackgroundInterface;
import edu.neu.ccs.wellness.storywell.interfaces.GameSpriteInterface;
import edu.neu.ccs.wellness.storywell.interfaces.GameViewInterface;

/**
 * Created by hermansaksono on 2/8/18.
 */

public class SevenDayMonitoringView extends View implements GameViewInterface{

    /* PRIVATE VARIABLES */
    int width;
    int height;
    List<GameBackgroundInterface> backgrounds = new ArrayList<GameBackgroundInterface>();
    List<GameSpriteInterface> sprites= new ArrayList<GameSpriteInterface>();

    /* CONSTRUCTOR */
    public SevenDayMonitoringView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.MonitoringView);
        int count = typedArray.getInt(R.styleable.MonitoringView_mv_count,0);
        typedArray.recycle();
        float scale = getResources().getDisplayMetrics().density; // TODO
    }

    /* VIEW METHODS */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        this.width = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        this.height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(this.width, this.height);
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldw, int oldh) {
        this.width = width;
        this.height = height;
        updateSizeChange(width, height, backgrounds, sprites);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBackgrounds(canvas, backgrounds);
        drawSprites(canvas, sprites);
        // TODO
    }

    /* PUBLIC INTERFACE METHODS */
    /**
     * Add a background to the @SevenDayMonitoringView.
     * @param background A @GameBackgroundInterface object that is going to be added.
     */
    @Override
    public void addBackground(GameBackgroundInterface background) {
        background.onAttach(this.width, this.height);
        this.backgrounds.add(background);
    }

    /**
     * Remove a background from the @SevenDayMonitoringView.
     * @param background A @GameBackgroundInterface object that is going to be added.
     */
    @Override
    public void removeBackground(GameBackgroundInterface background) {
        this.backgrounds.remove(background);
    }

    /**
     * Get a list of background in @SevenDayMonitoringView.
     * @return The backgrounds.
     */
    @Override
    public List<GameBackgroundInterface> getListOfBackground() {
        return this.backgrounds;
    }

    /**
     * Add a sprite to the @SevenDayMonitoringView
     * @param sprite A @GameSpriteInterface object that is going to be added.
     */
    @Override
    public void addSprite(GameSpriteInterface sprite) {
        this.sprites.add(sprite);
    }

    /**
     * Remove a sprite from the @SevenDayMonitoringView
     * @param sprite A @GameBackgroundInterface object that is going to be added.
     */
    @Override
    public void removeSprite(GameSpriteInterface sprite) {
        this.sprites.remove(sprite);
    }

    /**
     * Get a list of sprites in @SevenDayMonitoringView.
     * @return The sprites.
     */
    @Override
    public List<GameSpriteInterface> getListOfSprite() {
        return this.sprites;
    }

    /**
     * Updates the contents of this @GameViewInterface
     */
    @Override
    public void update() {
        updateBackgrounds(this.backgrounds);
        updateSprites(this.sprites);
    }

    /* PRIVATE HELPER METHODS */
    private static void updateSizeChange(int width, int height,
                                         List<GameBackgroundInterface> backgrounds,
                                         List<GameSpriteInterface> sprites) {
        for (GameBackgroundInterface bg : backgrounds ) {
            bg.onSizeChanged(width, height);
        }
        for (GameSpriteInterface sprite : sprites ) {
            sprite.onSizeChanged(width, height);
        }
    }

    private static void drawBackgrounds(Canvas canvas, List<GameBackgroundInterface> backgrounds) {
        for (GameBackgroundInterface bg : backgrounds ) {
            bg.draw(canvas);
        }
    }

    private static void drawSprites(Canvas canvas, List<GameSpriteInterface> sprites) {
        for (GameSpriteInterface sprite : sprites ) {
            sprite.draw(canvas);
        }
    }

    private static void updateBackgrounds(List<GameBackgroundInterface> backgrounds) {
        for (GameBackgroundInterface bg : backgrounds ) {
            bg.update();
        }
    }

    private static void updateSprites(List<GameSpriteInterface> sprites) {
        for (GameSpriteInterface sprite : sprites ) {
            sprite.update();
        }
    }
}
