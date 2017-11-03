package edu.neu.ccs.wellness.storytelling;

import android.*;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import edu.neu.ccs.wellness.server.RestServer;

import static edu.neu.ccs.wellness.storytelling.storyview.ReflectionFragment.isPermissionGranted;

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
                startHomeActivity();
            }
        }
    }
}
