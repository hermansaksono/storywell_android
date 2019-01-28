package edu.neu.ccs.wellness.storytelling;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;

import java.io.IOException;

import edu.neu.ccs.wellness.fitness.interfaces.ChallengeStatus;
import edu.neu.ccs.wellness.logging.WellnessUserLogging;
import edu.neu.ccs.wellness.people.Group;
import edu.neu.ccs.wellness.server.RestServer;
import edu.neu.ccs.wellness.server.RestServer.ResponseType;
import edu.neu.ccs.wellness.storytelling.firstrun.FirstRunActivity;
import edu.neu.ccs.wellness.storytelling.settings.SynchronizedSettingRepository;

public class SplashScreenActivity extends AppCompatActivity {
    private Storywell storywell;
    private TextView statusTextView;
    private ProgressBar progressBar;
    private static final int PROGRESS_STORIES = 0;
    private static final int PROGRESS_GROUP = 1;
    private static final int PROGRESS_CHALLENGES = 2;
    private static final int PROGRESS_SETTING = 3;
    private static final int PROGRESS_COMPLETED = 4;
    private static final int[] PROGRESS_STRINGS = new int[]{
            R.string.splash_download_stories,
            R.string.splash_download_group,
            R.string.splash_download_challenges,
            R.string.splash_text_01};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);
        this.statusTextView = findViewById(R.id.text);
        this.progressBar = findViewById(R.id.progressBar);
        this.storywell = new Storywell(getApplicationContext());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!this.storywell.isFirstRunCompleted()) {
            startFirstRun();
        } else if (!this.storywell.userHasLoggedIn()) {
            startLoginActivity();
        } else {
            preloadDataThenStartHomeActivity();
        }
    }

    private void startFirstRun() {
        Intent intent = new Intent(this, FirstRunActivity.class);
        startIntent(intent);
    }

    private void startLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startIntent(intent);
    }

    private void preloadDataThenStartHomeActivity() {
        this.resetProgressIndicators();
        // this.updateLocalSynchronizedSettingThenFetchEverything();
        new FetchEverythingAsync().execute();
    }

    private void startHomeActivity() {
        WellnessUserLogging userLogging = new WellnessUserLogging(storywell.getGroup().getName());
        userLogging.logEvent("APP_STARTUP", null);

        //FitnessSyncReceiver.scheduleFitnessSync(this, FitnessSyncReceiver.SYNC_INTERVAL);
        Intent intent = new Intent(this, HomeActivity.class);
        startIntent(intent);
    }

    private void startGameActivity() {
        Intent intent = new Intent(this, MonitoringActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
    /*
    private void updateLocalSynchronizedSettingThenFetchEverything() {
        this.setProgressStatus(PROGRESS_SETTING);

        if (!SynchronizedSettingRepository.isLocalExists(this)) {
            SynchronizedSettingRepository.initialize(this);
        } else {
            ValueEventListener listener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    new FetchEverythingAsync().execute();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    getTryAgainSnackbar("We have a problem connecting to the server").show();
                }
            };
            SynchronizedSettingRepository.updateLocalInstance(listener, getApplicationContext());
        }
    }
    */
    /* AsyncTask to initialize the data */
    private class FetchEverythingAsync extends AsyncTask<Void, Integer, ResponseType> {
        protected RestServer.ResponseType doInBackground(Void... voids) {
            if (!storywell.isServerOnline()) {
                return RestServer.ResponseType.NO_INTERNET;
            }

            try {
                publishProgress(PROGRESS_STORIES);
                storywell.getStoryManager().loadStoryList(getApplicationContext());

                publishProgress(PROGRESS_GROUP);
                Group group = storywell.getGroup();
                Log.d("SWELL", "Group: " + group.getName());

                publishProgress(PROGRESS_CHALLENGES);
                ChallengeStatus status = storywell.getChallengeManager().getStatus();
                Log.d("SWELL", "Challenge status: " + status);

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
            doHandleServerResponse(response);
        }
    }

    /* PRIVATE HELPER METHODS */
    private void startIntent(Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void setProgressStatus(Integer progressId) {
        int stringResourcesId;
        int progressPercent;
        switch(progressId) {
            case PROGRESS_STORIES:
                stringResourcesId = PROGRESS_STRINGS[PROGRESS_STORIES];
                progressPercent = 0;
                break;
            case PROGRESS_GROUP:
                stringResourcesId = PROGRESS_STRINGS[PROGRESS_GROUP];
                progressPercent = 25;
                break;
            case PROGRESS_CHALLENGES:
                stringResourcesId = PROGRESS_STRINGS[PROGRESS_CHALLENGES];
                progressPercent = 50;
                break;
            case PROGRESS_SETTING:
                stringResourcesId = PROGRESS_STRINGS[PROGRESS_SETTING];
                progressPercent = 75;
                break;
            case PROGRESS_COMPLETED:
                stringResourcesId = PROGRESS_STRINGS[PROGRESS_SETTING];
                progressPercent = 100;
                break;
            default:
                stringResourcesId = 0;
                progressPercent = 0;
                break;
        }

        statusTextView.setText(stringResourcesId);
        progressBar.setProgress(progressPercent);
    }

    private void doHandleServerResponse(ResponseType response) {
        switch (response) {
            case SUCCESS_202:
                //startHomeActivity();
                updateLocalSynchronizedSetting();
                break;
            case NO_INTERNET:
                getTryAgainSnackbar(getString(R.string.error_no_internet)).show();
                break;
            case BAD_JSON:
                getTryAgainSnackbar(getString(R.string.error_json_error)).show();
                break;
            case BAD_REQUEST_400:
                getTryAgainSnackbar(getString(R.string.error_json_error)).show();
                break;
            default:
                statusTextView.setText("");
                break;
        }
    }

    private void updateLocalSynchronizedSetting() {
        this.setProgressStatus(PROGRESS_SETTING);

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                setProgressStatus(PROGRESS_COMPLETED);
                startHomeActivity();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                getTryAgainSnackbar("We have a problem connecting to the server").show();
            }
        };
        SynchronizedSettingRepository.updateLocalInstance(listener, getApplicationContext());
    }

    private void resetProgressIndicators() {
        statusTextView.setText(R.string.empty);
        progressBar.setProgress(0);
    }

    private Snackbar getTryAgainSnackbar(String text) {
        Snackbar snackbar = getSnackbar(text, this);
        snackbar.setAction(R.string.button_try_again, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preloadDataThenStartHomeActivity();
            }
        });
        return snackbar;
    }

    private static Snackbar getSnackbar(String text, Activity activity) {
        View gameView = activity.findViewById(R.id.splashscreenView);
        return Snackbar.make(gameView, text, Snackbar.LENGTH_INDEFINITE);
    }
}
