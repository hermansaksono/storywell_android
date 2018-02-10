package edu.neu.ccs.wellness.storytelling;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import edu.neu.ccs.wellness.server.RestServer;
import edu.neu.ccs.wellness.storywell.monitoringview.SevenDayMonitoringView;

public class SplashScreenActivity extends AppCompatActivity {
    private Storywell storywell;
    private TextView statusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);
        this.statusTextView = findViewById(R.id.text);
        this.storywell = new Storywell(getApplicationContext());
    }

    @Override
    protected void onStart() {
        super.onStart();
        startGameActivity();
        /*
        if (Storywell.userHasLoggedIn(getApplicationContext())) {
            initApp();
        } else {
            startLoginActivity();
        }
        */
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

    private void startGameActivity() {
        Intent intent = new Intent(this, MonitoringActivity.class);
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
                storywell.getChallengeManager().download(getApplicationContext());
                return RestServer.ResponseType.SUCCESS_202;
            }
        }

        protected void onPostExecute(RestServer.ResponseType result) {
            if (result == RestServer.ResponseType.NO_INTERNET) {
                Log.e("WELL challenge d/l", result.toString());
                statusTextView.setText(R.string.error_no_internet);
            } else if (result == RestServer.ResponseType.SUCCESS_202) {
                Log.i("WELL challenge d/l", "Downloaded");
                startHomeActivity();
            }
        }
    }
}
