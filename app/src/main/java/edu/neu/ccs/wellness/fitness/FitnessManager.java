package edu.neu.ccs.wellness.fitness;

import android.content.Context;

import org.json.JSONObject;

import java.util.Date;

import edu.neu.ccs.wellness.fitness.interfaces.FitnessManagerInterface;
import edu.neu.ccs.wellness.fitness.interfaces.GroupFitnessInterface;
import edu.neu.ccs.wellness.server.RestServer;

/**
 * Created by hermansaksono on 6/21/17.
 */

public class FitnessManager implements FitnessManagerInterface {


    // PRIVATE VARIABLES
    private RestServer server;
    private Context context;
    private JSONObject jsonObject;


    /* CONSTRUCTOR */
    public FitnessManager(RestServer server, Context context) {
        this.server = server;
        this.context = context;
    }

    /* INTERFACE METHODS */
    @Override
    public GroupFitnessInterface getMultiDayFitness(Date startDate, Date endDate, Date cacheExpiryDate) {
        //TODO call rest server


        return null;
    }
}
