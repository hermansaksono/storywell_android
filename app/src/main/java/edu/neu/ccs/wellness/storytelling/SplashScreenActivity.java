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
import edu.neu.ccs.wellness.server.RestServer.ResponseType;
import edu.neu.ccs.wellness.storytelling.firstrun.FirstRunActivity;

public class SplashScreenActivity extends AppCompatActivity {
    private Storywell storywell;
    private TextView statusTextView;
    private ProgressBar progressBar;
    private Context context;
    private static final int PROGRESS_STORIES = 0;
    private static final int PROGRESS_GROUP = 1;
    private static final int PROGRESS_CHALLENGES = 2;
    private static final int PROGRESS_COMPLETED = 3;
    private static final int[] PROGRESS_STRINGS = new int[]{
            R.string.splash_download_stories,
            R.string.splash_download_group,
            R.string.splash_download_challenges};

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
        this.setProgressStatus(PROGRESS_STORIES);
        //new DownloadStoryListAsync().execute();
        new FetchEverythingAsync().execute();
    }

    /* AsyncTask to initialize the data */
    private class FetchEverythingAsync extends AsyncTask<Void, Integer, ResponseType> {
        protected RestServer.ResponseType doInBackground(Void... voids) {
            if (storywell.isServerOnline() == false) {
                return RestServer.ResponseType.NO_INTERNET;
            }

            try {
                storywell.getStoryManager().loadStoryList(getApplicationContext());

                publishProgress(PROGRESS_GROUP);
                storywell.getGroup();

                publishProgress(PROGRESS_CHALLENGES);
                storywell.getChallengeManager().getStatus();

                publishProgress(PROGRESS_COMPLETED);
                return RestServer.ResponseType.SUCCESS_202;
            } catch (JSONException e) {
                e.printStackTrace();
                Log.d("SWELL", "Bad JSON");
                return ResponseType.BAD_JSON;
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("SWELL", "Can't connect to server.");
                return ResponseType.BAD_REQUEST_400;
            }
        }

        protected void onProgressUpdate(Integer... progressId) {
            setProgressStatus(progressId[0]);
        }

        protected void onPostExecute(ResponseType response) {
            switch (response) {
                case SUCCESS_202:
                    startHomeActivity();
                    break;
                case NO_INTERNET:
                    statusTextView.setText(R.string.error_no_internet);
                    break;
                case BAD_JSON:
                    statusTextView.setText(R.string.error_json_error);
                    break;
                case BAD_REQUEST_400:
                    statusTextView.setText(R.string.error_bad_request);
                    break;
                default:
                    statusTextView.setText("");
                    break;
            }
        }
    }

    private void setProgressStatus(Integer progressId) {
        int stringResourcesId = 0;
        int progressPercent = 0;
        switch(progressId) {
            case PROGRESS_STORIES:
                stringResourcesId = PROGRESS_STRINGS[PROGRESS_STORIES];
                progressPercent = 0;
                break;
            case PROGRESS_GROUP:
                stringResourcesId = PROGRESS_STRINGS[PROGRESS_GROUP];
                progressPercent = 33;
                break;
            case PROGRESS_CHALLENGES:
                stringResourcesId = PROGRESS_STRINGS[PROGRESS_CHALLENGES];
                progressPercent = 66;
                break;
            case PROGRESS_COMPLETED:
                stringResourcesId = PROGRESS_STRINGS[PROGRESS_CHALLENGES];
                progressPercent = 100;
                break;
            default:
                stringResourcesId = 0;
                break;
        }

        statusTextView.setText(stringResourcesId);
        progressBar.setProgress(progressPercent);
    }
}
