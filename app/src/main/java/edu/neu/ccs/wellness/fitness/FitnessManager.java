package edu.neu.ccs.wellness.fitness;

import android.content.Context;

import java.util.Date;

import edu.neu.ccs.wellness.fitness.interfaces.FitnessManagerInterface;
import edu.neu.ccs.wellness.fitness.interfaces.GroupFitnessInterface;
import edu.neu.ccs.wellness.server.RestServer;

/**
 * Created by hermansaksono on 6/21/17.
 */

public class FitnessManager implements FitnessManagerInterface {

    /* CONSTRUCTOR */
    public FitnessManager(RestServer server, Context context) {
        // TODO
    }

    /* INTERFACE METHODS */
    @Override
    public GroupFitnessInterface getMultiDayFitness(Date startDate, Date endDate, Date cacheExpiryDate) {
        return null;
    }
}
