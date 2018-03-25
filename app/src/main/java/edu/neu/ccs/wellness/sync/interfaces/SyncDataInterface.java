package edu.neu.ccs.wellness.sync.interfaces;

import android.content.Context;

import org.json.JSONObject;

/**
 * Created by RAJ on 3/24/2018.
 */

/*
This class should handle all the syncing with server, local storage and Firebase.
 */
public interface SyncDataInterface {

    String requestJsonString(Context context, Boolean useSaved, String fileName, String restResource);

    JSONObject requestJson(Context context, Boolean useSaved, String fileName, String restResource);

    void writeFileToStorage(Context context, String jsonString, String fileName);

    String postRequest(String jsonString, String restResource);
}
