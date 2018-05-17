package edu.neu.ccs.wellness.server;

import android.content.Context;

import org.json.JSONObject;

/**
 * Created by RAJ on 3/24/2018.
 * Objects implementing this interface should handle all the communications with the server,
 * local storage, Firebase, etc.
 */

public interface Repository {

    String requestJsonString(Context context, Boolean useSaved, String fileName, String restResource);

    JSONObject requestJson(Context context, Boolean useSaved, String fileName, String restResource);

    void writeFileToStorage(Context context, String jsonString, String fileName);

    String postRequest(String jsonString, String restResource);
}
