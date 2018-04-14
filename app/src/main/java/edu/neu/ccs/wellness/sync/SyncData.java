package edu.neu.ccs.wellness.sync;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import edu.neu.ccs.wellness.server.RestServer;
import edu.neu.ccs.wellness.sync.interfaces.SyncDataInterface;
import edu.neu.ccs.wellness.utils.WellnessIO;

/**
 * Created by RAJ on 3/24/2018.
 */

public class SyncData implements SyncDataInterface {

    private static final String CHALLENGE_MANAGER_REST_RESOURCE = "group/challenges2";
    private static final String FITNESS_MANAGER_REST_RESOURCE = "group/activities/7d/";

    private static final String CHALLENGE_MANAGER_FILENAME = "challengeManager.json";
    private static final String FITNESS_MANAGER_FILENAME = "FitnessManager.json";
    private RestServer server;
    private Context context;


    public SyncData(RestServer server, Context context) {
        this.server = server;
        this.context = context;
    }

    @Override
    public String requestJsonString(Context context, Boolean useSaved, String fileName, String restResource) {
        try {
            //TODO Remove context from the parameter
                return server.doGetRequestFromAResource(context, fileName, restResource, useSaved);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }



    @Override
    public JSONObject requestJson(Context context, Boolean useSaved, String fileName, String restResource) {
        try {
            String jsonString = requestJsonString(context, useSaved, fileName, restResource);
            return new JSONObject(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void writeFileToStorage(Context context, String jsonString, String fileName) {
        WellnessIO.writeFileToStorage(this.context, fileName, jsonString);
    }

    @Override
    public String postRequest(String jsonString, String restResource) {
        try {
            return server.doPostRequestFromAResource(jsonString, restResource);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
