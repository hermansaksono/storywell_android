package edu.neu.ccs.wellness.server;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

import edu.neu.ccs.wellness.utils.WellnessIO;

/**
 * Created by hermansaksono on 3/6/19.
 */

public class FirebaseTokenRepository {

    public static final String PATH_GET_TOKEN = "firebase/get_token";
    private static final String KEY_PATH = "group_firebase_token";

    public static FirebaseToken getToken(RestServer restServer, Context context) {
        if (isNewTokenRequired(context)) {
            updateLocalInstanceAsync(restServer, context);
        }
        return getLocalInstance(context);
    }

    public static FirebaseToken getLocalInstance(Context context) {
        SharedPreferences sharedPreferences = WellnessIO.getSharedPref(context);
        if (sharedPreferences.contains(KEY_PATH)) {
            String jsonString = sharedPreferences.getString(KEY_PATH, "");
            return new Gson().fromJson(jsonString, FirebaseToken.class);
        } else {
            return new FirebaseToken();
        }
    }

    public static boolean isNewTokenRequired(Context context) {
        FirebaseToken firebaseToken = getLocalInstance(context);
        Calendar now = Calendar.getInstance(Locale.US);
        Calendar tokenExpiration = firebaseToken.getExpiresAt();
        return now.after(tokenExpiration);
    }

    public static void updateLocalInstanceAsync(RestServer restServer, Context context) {
        try {
            String jsonString = restServer.doSimpleGetRequestFromAResource(PATH_GET_TOKEN);
            SharedPreferences sharedPreferences = WellnessIO.getSharedPref(context);
            sharedPreferences.edit().putString(KEY_PATH, jsonString).commit();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
