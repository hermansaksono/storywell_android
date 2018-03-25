package edu.neu.ccs.wellness.fitness;

import android.content.Context;

import org.json.JSONObject;

import java.util.Date;

import edu.neu.ccs.wellness.fitness.interfaces.FitnessManagerInterface;
import edu.neu.ccs.wellness.fitness.interfaces.GroupFitnessInterface;
import edu.neu.ccs.wellness.server.RestServer;
import edu.neu.ccs.wellness.sync.SyncData;

/**
 * Created by hermansaksono on 6/21/17.
 */

public class FitnessManager implements FitnessManagerInterface {

    // PRIVATE VARIABLES
    private static final String REST_RESOURCE = "group/activities/7d/";
    private static final String FILENAME = "FitnessManager.json";
    private RestServer server;
    private Context context;
    private JSONObject jsonObject;
    private SyncData syncData;


    /* CONSTRUCTOR */
    public FitnessManager(RestServer server, Context context) {
        this.server = server;
        this.context = context;
        this.syncData = new SyncData(server, context);
    }

    /* INTERFACE METHODS */
    @Override
    public GroupFitnessInterface getMultiDayFitness(Date startDate, Date endDate, Date cacheExpiryDate) {
        //TODO call rest server
        Date date = new Date();
        if(date.after(cacheExpiryDate)){
           syncData.requestJson(this.context, true, FILENAME, REST_RESOURCE);
        }else{

        }

        return null;
    }
}
