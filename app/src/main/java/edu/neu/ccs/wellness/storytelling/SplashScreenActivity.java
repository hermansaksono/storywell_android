package edu.neu.ccs.wellness.storytelling;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import edu.neu.ccs.wellness.server.RestServer;
import edu.neu.ccs.wellness.storytelling.models.challenges.GroupChallenge;

public class SplashScreenActivity extends AppCompatActivity {
    private Storywell storywell;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen2);
        this.storywell = new Storywell(this);

        if (Storywell.userHasLoggedIn(getApplicationContext())) {
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
                new DownloadChallengesAsync().execute();
                startHomeActivity();
            }
        }

    }

    private class DownloadChallengesAsync extends AsyncTask<Void, Integer, RestServer.ResponseType> {

        protected RestServer.ResponseType doInBackground(Void... voids) {
            if (!storywell.isServerOnline()) {
                return RestServer.ResponseType.NO_INTERNET;
            } else {
                Log.i("WELL Challenges d/;", "Start");
                return GroupChallenge.downloadChallenges(getApplicationContext(), storywell.getServer());
            }
        }

        protected void onPostExecute(RestServer.ResponseType result) {
            Log.d("WELL Challenges d/l", result.toString());
            if (result == RestServer.ResponseType.NO_INTERNET) {
                // TODO
            } else if (result == RestServer.ResponseType.NOT_FOUND_404) {
                // TODO
            } else if (result == RestServer.ResponseType.SUCCESS_202) {
                // TODO
            }
        }
    }
}
