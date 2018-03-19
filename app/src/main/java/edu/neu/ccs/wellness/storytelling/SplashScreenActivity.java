package edu.neu.ccs.wellness.storytelling;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import edu.neu.ccs.wellness.fitness.interfaces.ChallengeStatus;
import edu.neu.ccs.wellness.server.RestServer;

/**
 *
 * TODO HS: Please explain why do we need an application state singleton?
 */
public class SplashScreenActivity extends AppCompatActivity {
    private Storywell storywell;
    private TextView statusTextView;
    private SharedPreferences myPreferences;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);
        this.statusTextView = findViewById(R.id.text);
        this.context = getApplicationContext();
         this.storywell = new Storywell(context);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //TODO if it is first run then do the following:


//        if(isFirstRun()){
//            storywell.getChallengeManager().setStatus("AVAILABLE");
//            Log.d("status changed to: ", "available");
//            updateFirstRun();
//        }

        if (Storywell.userHasLoggedIn(getApplicationContext())) {
            initApp();
        } else {
            startLoginActivity();
        }

    }

    private boolean isFirstRun(){
        myPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        Boolean isFirstRun = myPreferences.getBoolean("isFirstRun", true);
        return isFirstRun;
    }

    private void updateFirstRun(){
        SharedPreferences.Editor editPref = myPreferences.edit();
        editPref.putBoolean("isFirstRun", false);
        editPref.apply();
    }

    private void initApp() { this.preloadResources(); }

    private void startHomeActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void startLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    /* PRIVATE METHODS */
    private void preloadResources () {
        new DownloadStoryListAsync().execute();
    }

    /* ASYNCTASK CLASSES */
    /* ASYNCTASK To Initialize Story */
    private class DownloadStoryListAsync extends AsyncTask<Void, Integer, RestServer.ResponseType> {

        protected RestServer.ResponseType doInBackground(Void... voids) {
            if (!storywell.isServerOnline()) {
                return RestServer.ResponseType.NO_INTERNET;
            } else {
                storywell.getStoryManager().loadStoryList(getApplicationContext());
                return RestServer.ResponseType.SUCCESS_202;
            }
        }

        protected void onPostExecute(RestServer.ResponseType result) {
            if (result == RestServer.ResponseType.NO_INTERNET) {
                statusTextView.setText(R.string.error_no_internet);
            } else if (result == RestServer.ResponseType.SUCCESS_202) {
                new DownloadGroupAsync().execute();
            }
        }
    }

    /* ASYNCTASK To initialize Group info */
    private class DownloadGroupAsync extends AsyncTask<Void, Integer, RestServer.ResponseType> {

        protected RestServer.ResponseType doInBackground(Void... voids) {
            if (!storywell.isServerOnline()) {
                return RestServer.ResponseType.NO_INTERNET;
            } else {
                storywell.getGroup();
                return RestServer.ResponseType.SUCCESS_202;
            }
        }

        protected void onPostExecute(RestServer.ResponseType result) {
            if (result == RestServer.ResponseType.NO_INTERNET) {
               Log.e("WELL Group download d/l", result.toString());
                statusTextView.setText(R.string.error_no_internet);
            } else if (result == RestServer.ResponseType.SUCCESS_202) {
               Log.i("WELL Group download d/l", storywell.getGroup().getName());
                new DownloadChallengeAsync().execute();
            }
        }
    }

    /* ASYNCTASK To get Challenge info */
    private class DownloadChallengeAsync extends AsyncTask<Void, Integer, RestServer.ResponseType> {

        protected RestServer.ResponseType doInBackground(Void... voids) {
            if (!storywell.isServerOnline()) {
                return RestServer.ResponseType.NO_INTERNET;
            } else {
                if(isFirstRun()){
                    try {
                        storywell.getChallengeManager().changeChallengeStatus(0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    updateFirstRun();
                }
              storywell.getChallengeManager().manageChallenge();

                return RestServer.ResponseType.SUCCESS_202;
            }
        }

        protected void onPostExecute(RestServer.ResponseType result) {
            if (result == RestServer.ResponseType.NO_INTERNET) {
             //   Log.e("WELL challenge d/l", result.toString());
                statusTextView.setText(R.string.error_no_internet);
            } else if (result == RestServer.ResponseType.SUCCESS_202) {
              //  Log.i("WELL challenge d/l", "Downloaded");
                startHomeActivity();
            }
        }
    }
}
