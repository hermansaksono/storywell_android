package edu.neu.ccs.wellness.server;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import edu.neu.ccs.wellness.utils.WellnessIO;

/**
 * Created by RAJ on 3/24/2018.
 */

public class WellnessRepository implements Repository {
    private RestServer server;
    private Context context;

    public WellnessRepository(RestServer server, Context context) {
        this.server = server;
        this.context = context.getApplicationContext();
    }

    @Override
    public String requestJsonString(Context context, Boolean useSaved, String fileName, String restResource) {
        try {
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
        WellnessIO.writeFileToStorage(context, fileName, jsonString);
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
