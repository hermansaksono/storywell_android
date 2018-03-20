package edu.neu.ccs.wellness.fitness;

import java.util.Date;

import edu.neu.ccs.wellness.fitness.interfaces.OneDayFitnessInterface;

/**
 * Created by hermansaksono on 3/20/18.
 */

public class OneDayFitness implements OneDayFitnessInterface {
    @Override
    public Date getDate() {
        return null;
    }

    @Override
    public int getSteps() {
        return 0;
    }

    @Override
    public float getCalories() {
        return 0;
    }

    @Override
    public float getDistance() {
        return 0;
    }

    @Override
    public float getActiveMinutes() {
        return 0;
    }
}
