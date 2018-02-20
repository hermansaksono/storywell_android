package edu.neu.ccs.wellness.storywell.monitoringview;

import android.content.res.Resources;
import android.util.Pair;

import com.google.gson.Gson;

import edu.neu.ccs.wellness.storywell.interfaces.GameBackgroundInterface;
import edu.neu.ccs.wellness.storywell.interfaces.GameLevelInterface;
import edu.neu.ccs.wellness.storywell.interfaces.GameSpriteInterface;
import edu.neu.ccs.wellness.utils.WellnessDate;

/**
 * Created by hermansaksono on 2/13/18.
 */

public class GameLevel implements GameLevelInterface {

    /* PRIVATE VARIABLES */
    public int skyBgColor;

    public int fgDrawableId;

    public int islandDrawableId;


    public int cloudBg1DrawableId;
    public Pair<Float, Float> cloudBg1InitPos = new Pair<>(0.0f, 0.2f);
    public float cloudBg1SpeedX = -5f;

    public int cloudBg2DrawableId;
    public Pair<Float, Float> cloudBg2InitPos = new Pair<>(1f, 0.2f);
    public float cloudBg2SpeedX = -5f;

    public int cloudFg1DrawableId;
    public Pair<Float, Float> cloudFg1InitPos = new Pair<>(0.0f, 0.25f);
    public float cloudFg1SpeedX = -10f;

    public int cloudFg2DrawableId;
    public Pair<Float, Float> cloudFg2InitPos = new Pair<>(1f, 0.25f);
    public float cloudFg2SpeedX = -10f;

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
    public GameSpriteInterface getIsland(Resources res, int dayOfWeek,
                                         float posXRatio, float posYRatio, float scaleRatio) {
        return new IslandSprite(res, this.islandDrawableId,
                WellnessDate.getDayOfWeek(dayOfWeek), posXRatio, posYRatio, scaleRatio);
    }

    @Override
    public GameSpriteInterface getSeaFg(Resources res, float posXRatio, float posYRatio,
                                        float rangeXRatio, float rangeYRatio) {
        return new SeaSprite(res, this.fgDrawableId, posXRatio, posYRatio, rangeXRatio, rangeYRatio);
    }

    @Override
    public GameSpriteInterface getCloudBg1(Resources res) {
        return new CloudSprite(res, this.cloudBg1DrawableId,
                this.cloudBg1InitPos.first, this.cloudBg1InitPos.second, this.cloudBg1SpeedX);
    }

    @Override
    public GameSpriteInterface getCloudBg2(Resources res) {
        return new CloudSprite(res, this.cloudBg2DrawableId,
                this.cloudBg2InitPos.first, this.cloudBg2InitPos.second, this.cloudBg2SpeedX);
    }

    @Override
    public GameSpriteInterface getCloudFg1(Resources res) {
        return new CloudSprite(res, this.cloudFg1DrawableId,
                this.cloudFg1InitPos.first, this.cloudFg1InitPos.second, this.cloudFg1SpeedX);
    }

    @Override
    public GameSpriteInterface getCloudFg2(Resources res) {
        return new CloudSprite(res, this.cloudFg2DrawableId,
                this.cloudFg2InitPos.first, this.cloudFg2InitPos.second, this.cloudFg2SpeedX);
    }

    /* PUBLIC METHODS */
    public String getJson () {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
