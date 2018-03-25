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

    String requestJsonString(Context context, Boolean useSaved);

    JSONObject requestJson(Context context, Boolean useSaved);

    void writeFileToStorage(Context context, String jsonString);

    String postRequest(String jsonString);
}
