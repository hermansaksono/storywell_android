package com.hermansaksono.miband.model;

import com.hermansaksono.miband.utils.TypeConversionUtils;

/**
 * Created by hermansaksono on 6/20/18.
 */

public class FitnessSample {
    private int steps;
    private int dist;
    private int calories;

    public FitnessSample(byte[] data) {
        this.steps = subArrayToInt(data, 1);
        this.dist = subArrayToInt(data, 5);
        this.calories = subArrayToInt(data, 9);
    }

    public int getSteps() { return this.steps; }

    public int getDist() { return this.dist; }

    public int getCalories() { return this.calories; }

    /* HELPER METHODS */
    private int subArrayToInt(byte[] data, int start) {
        return TypeConversionUtils.unsignedShortToInt(data[start])
                + (TypeConversionUtils.unsignedShortToInt(data[start + 1]) * 256)
                + (TypeConversionUtils.unsignedShortToInt(data[start + 2]) * 65536);
    }
}
