package edu.neu.ccs.wellness.storytelling;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import edu.neu.ccs.wellness.people.Group;
import edu.neu.ccs.wellness.server.RestServer;

public class SplashScreenActivity extends AppCompatActivity {
    private Storywell storywell;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);
        this.storywell = new Storywell(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Storywell.userHasLoggedIn(this)) {
            initApp();
        } else {
            startLoginActivity();
        }
    }

    private void initApp() {
        new DownloadStoryListAsync().execute();
    }

    private void startHomeActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void startLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

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
            Log.d("WELL Story list d/l", result.toString());
            if (result == RestServer.ResponseType.NO_INTERNET) {
                // TODO
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
            Log.d("WELL Group download d/l", result.toString());
            if (result == RestServer.ResponseType.NO_INTERNET) {
                // TODO
            } else if (result == RestServer.ResponseType.SUCCESS_202) {
                startHomeActivity();
            }
        }
    }
}
