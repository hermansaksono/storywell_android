package edu.neu.ccs.wellness.sync;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import edu.neu.ccs.wellness.server.RestServer;
import edu.neu.ccs.wellness.sync.interfaces.SyncDataInterface;
import edu.neu.ccs.wellness.utils.WellnessIO;

/**
 * Created by RAJ on 3/24/2018.
 */

public class SyncData implements SyncDataInterface{

    private static final String CHALLENGE_MANAGER_REST_RESOURCE = "group/challenges2";
    private static final String CHALLENGE_MANAGER_FILENAME = "challengeManager.json";
    private RestServer server;
    private Context context;


    public SyncData(RestServer server, Context context){
        this.server = server;
        this.context = context;
    }

    @Override
    public String requestJsonString(Context context, Boolean useSaved) {
        try {
            return server.doGetRequestFromAResource(context, CHALLENGE_MANAGER_FILENAME, CHALLENGE_MANAGER_REST_RESOURCE, useSaved);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public JSONObject requestJson(Context context, Boolean useSaved) {
        try {
            String jsonString = requestJsonString(context, useSaved);
            return new JSONObject(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void writeFileToStorage(Context context, String jsonString) {
        WellnessIO.writeFileToStorage(this.context, CHALLENGE_MANAGER_FILENAME, jsonString);
    }

    @Override
    public String postRequest(String jsonString) {
        try {
           return server.doPostRequestFromAResource(jsonString, CHALLENGE_MANAGER_REST_RESOURCE);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
