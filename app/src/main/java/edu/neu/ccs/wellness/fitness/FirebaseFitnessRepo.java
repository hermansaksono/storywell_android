package edu.neu.ccs.wellness.fitness;

import java.util.Date;

import edu.neu.ccs.wellness.fitness.interfaces.FitnessException;
import edu.neu.ccs.wellness.fitness.interfaces.FitnessRepositoryInterface;
import edu.neu.ccs.wellness.fitness.interfaces.GroupFitnessInterface;

/**
 * Created by hermansaksono on 7/12/18.
 */

public class FirebaseFitnessRepo implements FitnessRepositoryInterface {

    @Override
    public GroupFitnessInterface getMultiDayFitness(Date startDate, Date endDate, Date cacheExpiry)
            throws FitnessException {
        return null;
    }

    @Override
    public GroupFitnessInterface getMultiDayFitness(Date startDate, Date endDate)
            throws FitnessException {
        return null;
    }
}
