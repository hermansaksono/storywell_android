package edu.neu.ccs.wellness.storytelling;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;

import java.io.IOException;

import edu.neu.ccs.wellness.server.RestServer;
import edu.neu.ccs.wellness.storytelling.firstrun.FirstRunActivity;

/**
 *
 * TODO HS: Please explain why do we need an application state singleton?
 */
public class SplashScreenActivity extends AppCompatActivity {
    private Storywell storywell;
    private TextView statusTextView;
    private ProgressBar progressBar;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);
        this.statusTextView = findViewById(R.id.text);
        this.progressBar = findViewById(R.id.fetchingProgressBar);
        this.context = getApplicationContext();
        this.storywell = new Storywell(context);

    }

    @Override
    protected void onStart() {
        super.onStart();
        //startGameActivity();

        if (this.storywell.isFirstRunCompleted() == false) {
            startFirstRun();
        } else if (this.storywell.userHasLoggedIn() == true) {
            initApp();
        } else {
            startLoginActivity();
        }
    }

    private void initApp() {
        this.preloadResources();
    }

    private void startFirstRun() {
        Intent intent = new Intent(this, FirstRunActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

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
        statusTextView.setText(R.string.splash_download_stories);
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
                progressBar.setProgress(33);
                statusTextView.setText(R.string.splash_download_group);
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
                statusTextView.setText(R.string.error_no_internet);
            } else if (result == RestServer.ResponseType.SUCCESS_202) {
                statusTextView.setText(R.string.splash_download_challenges);
                progressBar.setProgress(66);
                new DownloadChallengeAsync().execute();
                //startHomeActivity();
            }
        }
    }

    /* ASYNCTASK To get UnitChallenge info */
    private class DownloadChallengeAsync extends AsyncTask<Void, Integer, RestServer.ResponseType> {

        protected RestServer.ResponseType doInBackground(Void... voids) {
            if (!storywell.isServerOnline()) {
                return RestServer.ResponseType.NO_INTERNET;
            }


            try {
                storywell.getChallengeManager().getStatus();
                return RestServer.ResponseType.SUCCESS_202;
            } catch (IOException e) {
                e.printStackTrace();
                return RestServer.ResponseType.NO_INTERNET;
            } catch (JSONException e) {
                e.printStackTrace();
                return RestServer.ResponseType.BAD_JSON;
            }
        }

        protected void onPostExecute(RestServer.ResponseType result) {
            if (result == RestServer.ResponseType.NO_INTERNET) {
                Log.e("WELL challenge d/l", result.toString());
                statusTextView.setText(R.string.error_no_internet);
            } else if (result == RestServer.ResponseType.BAD_JSON) {
                Log.i("WELL challenge d/l", "Bad JSON");
            } else if (result == RestServer.ResponseType.SUCCESS_202) {
                Log.i("WELL challenge d/l", "Downloaded");
                progressBar.setProgress(100);
                startHomeActivity();
            }
        }
    }
}
