package edu.neu.ccs.wellness.fitness;

import java.util.Date;
import java.util.List;

import edu.neu.ccs.wellness.fitness.interfaces.MultiDayFitnessInterface;
import edu.neu.ccs.wellness.fitness.interfaces.OneDayFitnessInterface;

/**
 * Created by hermansaksono on 3/20/18.
 */

public class MultiDayFitness implements MultiDayFitnessInterface {
    @Override
    public Date getStartDate() {
        return null;
    }

    @Override
    public Date getEndDate() {
        return null;
    }

    @Override
    public int getNumDays() {
        return 0;
    }

    @Override
    public int getElapsedDays() {
        return 0;
    }

    @Override
    public List<OneDayFitnessInterface> getDailyFitness() {
        return null;
    }
}
