package edu.neu.ccs.wellness.server;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by RAJ on 3/24/2018.
 * Objects implementing this interface should handle all the communications with the server,
 * local storage, Firebase, etc.
 */

public interface Repository {

    String requestJsonString(Context context, Boolean useSaved, String fileName, String restResource)
            throws IOException;

    JSONObject requestJson(Context context, Boolean useSaved, String fileName, String restResource) throws IOException, JSONException;

    void writeFileToStorage(Context context, String jsonString, String fileName);

    String postRequest(String jsonString, String restResource);

    String getRequest(String restResource);
}
