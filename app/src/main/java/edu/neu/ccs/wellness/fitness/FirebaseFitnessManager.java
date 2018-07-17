package edu.neu.ccs.wellness.fitness;

import org.json.JSONException;

import java.io.IOException;
import java.util.Date;

import edu.neu.ccs.wellness.fitness.interfaces.FitnessManagerInterface;
import edu.neu.ccs.wellness.fitness.interfaces.GroupFitnessInterface;
import edu.neu.ccs.wellness.fitness.storage.FitnessRepository;

/**
 * Created by hermansaksono on 7/17/18.
 */

public class FirebaseFitnessManager implements FitnessManagerInterface {

    private FitnessRepository repo;

    public FirebaseFitnessManager() {
        this.repo = new FitnessRepository();
    }

    @Override
    public GroupFitnessInterface getMultiDayFitness(Date startDate, Date endDate, Date cacheExpiry) throws IOException, JSONException {
        return null;
    }

    @Override
    public GroupFitnessInterface getMultiDayFitness(Date startDate, Date endDate) throws IOException, JSONException {
        return null;
    }
}
