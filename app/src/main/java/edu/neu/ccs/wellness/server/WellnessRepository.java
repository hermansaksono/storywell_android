package edu.neu.ccs.wellness.server;

import android.content.Context;
import android.util.Log;

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
    public String requestJsonString(Context context, Boolean useSaved,
                                    String fileName, String restResource) throws IOException {
        return server.doGetRequestFromAResource(context, fileName, restResource, useSaved);
    }

    @Override
    public JSONObject requestJson(Context context, Boolean useSaved, String fileName,
                                  String restResource) throws IOException, JSONException {
        String jsonString = requestJsonString(context, useSaved, fileName, restResource);
        return new JSONObject(jsonString);
    }

    @Override
    public void writeFileToStorage(Context context, String jsonString, String fileName) {
        WellnessIO.writeFileToStorage(context, fileName, jsonString);
    }

    @Override
    public String postRequest(String jsonString, String restResource) {
        try {
            return server.doPostRequestFromAResource(context, jsonString, restResource);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getRequest(String restResource) {
        try {
            return server.doSimpleGetRequestFromAResource(context, restResource);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean isSavedExist(Context context, String filename) {
        return server.isFileExists(context, filename);
    }


}
