package edu.neu.ccs.wellness.storywell.monitoringview;

import android.content.res.Resources;
import android.util.Pair;

import edu.neu.ccs.wellness.storywell.interfaces.GameBackgroundInterface;
import edu.neu.ccs.wellness.storywell.interfaces.GameLevelInterface;
import edu.neu.ccs.wellness.storywell.interfaces.GameSpriteInterface;

/**
 * Created by hermansaksono on 2/13/18.
 */

public class GameLevel implements GameLevelInterface {
    /* STATIC VARIABLES */
    public static final String[] DAYS_OF_WEEK = {"SUN", "MON", "TUE", "WED", "THUR", "FRI", "SAT"};

    /* PRIVATE VARIABLES */
    public HeroSprite hero;

    public int skyBgColor;

    public int fgDrawableId;
    public Pair<Integer, Integer> fgRange = new Pair<>(50, 10);

    public int islandDrawableId;

    public int cloudBg2DrawableId;
    public Pair<Integer, Integer> cloudBg2InitPos = new Pair<>(200, 200);
    public float cloudBg2Scale = 0.75f;
    public float cloudBg2SpeedX = -25;

    public int cloudBg1DrawableId;
    public Pair<Integer, Integer> cloudBg1InitPos = new Pair<>(800, 200);
    public float cloudBg1Scale = 0.75f;
    public float cloudBg1SpeedX = -30;

    public int cloudFg1DrawableId;
    public Pair<Integer, Integer> cloudFg1InitPos = new Pair<>(-1000, 200);
    public float cloudFg1Scale = 0.65f;
    public float cloudFg1SpeedX = -50;

    public int cloudFg2DrawableId;
    public Pair<Integer, Integer> cloudFg2InitPos = new Pair<>(200, 600);
    public float cloudFg2Scale = 0.65f;
    public float cloudFg2SpeedX = -50;

    /* CONSTRUCTOR */
    public GameLevel(int skyBgColor, int fgDrawableId, int islandDrawableId,
                     int cloudFg1DrawableId, int cloudBg1DrawableId,
                     int cloudFg2DrawableId, int cloudBg2DrawableId) {
        this.skyBgColor = skyBgColor;
        this.fgDrawableId = fgDrawableId;
        this.islandDrawableId = islandDrawableId;
        this.cloudFg1DrawableId = cloudFg1DrawableId;
        this.cloudBg1DrawableId = cloudBg1DrawableId;
        this.cloudFg2DrawableId = cloudFg2DrawableId;
        this.cloudBg2DrawableId = cloudBg2DrawableId;
    }

    /* PUBLIC INTERFACE METHODS */
    @Override
    public GameBackgroundInterface getBaseBackground(Resources res) {
        return new SolidBackground(res.getColor(this.skyBgColor));
    }

    @Override
    public GameSpriteInterface getIsland(Resources res, int dayOfWeek) {
        return new IslandSprite(res, this.islandDrawableId, DAYS_OF_WEEK[dayOfWeek]);
    }

    @Override
    public GameSpriteInterface getSeaFg(Resources res) {
        return new SeaSprite(res, this.fgDrawableId, fgRange.first, fgRange.second);
    }

    @Override
    public GameSpriteInterface getCloudBg1(Resources res) {
        return new CloudSprite(res, this.cloudBg1DrawableId,
                this.cloudBg1InitPos.first, this.cloudBg1InitPos.second,
                this.cloudBg1Scale, this.cloudBg1SpeedX);
    }

    @Override
    public GameSpriteInterface getCloudBg2(Resources res) {
        return new CloudSprite(res, this.cloudBg2DrawableId,
                this.cloudBg2InitPos.first, this.cloudBg2InitPos.second,
                this.cloudBg2Scale, this.cloudBg2SpeedX);
    }

    @Override
    public GameSpriteInterface getCloudFg1(Resources res) {
        return new CloudSprite(res, this.cloudFg1DrawableId,
                this.cloudFg1InitPos.first, this.cloudFg1InitPos.second,
                this.cloudFg1Scale, this.cloudFg1SpeedX);
    }

    @Override
    public GameSpriteInterface getCloudFg2(Resources res) {
        return new CloudSprite(res, this.cloudFg2DrawableId,
                this.cloudFg2InitPos.first, this.cloudFg2InitPos.second,
                this.cloudFg2Scale, this.cloudFg2SpeedX);
    }

    @Override
    public void setHero(HeroSprite hero) {
        this.hero = hero;
    }

    @Override
    public HeroSprite getHero() {
        return this.hero;
    }
}
