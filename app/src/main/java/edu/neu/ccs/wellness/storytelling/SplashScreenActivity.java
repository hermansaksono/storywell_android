package edu.neu.ccs.wellness.storytelling;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;

import java.io.IOException;

import edu.neu.ccs.wellness.fitness.interfaces.ChallengeStatus;
import edu.neu.ccs.wellness.logging.WellnessUserLogging;
import edu.neu.ccs.wellness.notifications.RegularNotificationManager;
import edu.neu.ccs.wellness.people.Group;
import edu.neu.ccs.wellness.server.FirebaseToken;
import edu.neu.ccs.wellness.server.RestServer;
import edu.neu.ccs.wellness.server.RestServer.ResponseType;
import edu.neu.ccs.wellness.storytelling.firstrun.FirstRunActivity;
import edu.neu.ccs.wellness.storytelling.notifications.FcmNotificationService;
import edu.neu.ccs.wellness.storytelling.notifications.RegularReminderReceiver;
import edu.neu.ccs.wellness.storytelling.settings.SynchronizedSetting;
import edu.neu.ccs.wellness.storytelling.settings.SynchronizedSettingRepository;
import edu.neu.ccs.wellness.utils.FirebaseUserManager;
import edu.neu.ccs.wellness.utils.WellnessIO;

public class SplashScreenActivity extends AppCompatActivity {
    private Storywell storywell;
    private TextView statusTextView;
    private ProgressBar progressBar;
    private FirebaseToken firebaseToken;
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
            setActiveHomeTab(getIntent());
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

    private void setActiveHomeTab(Intent intent) {
        if (intent.getExtras() != null && intent.getExtras()
                .containsKey(HomeActivity.KEY_DEFAULT_TAB)) {
            WellnessIO.getSharedPref(this).edit()
                    .putInt(HomeActivity.KEY_DEFAULT_TAB, HomeActivity.TAB_ADVENTURE)
                    .apply();
        }
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

    /* AsyncTask to initialize the data */
    private class FetchEverythingAsync extends AsyncTask<Void, Integer, ResponseType> {
        protected RestServer.ResponseType doInBackground(Void... voids) {
            if (!storywell.isServerOnline()) {
                return RestServer.ResponseType.NO_INTERNET;
            }

            try {
                firebaseToken = storywell.getFirebaseTokenAsync();

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
                doAuthThenSync();
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

    private void doAuthThenSync() {
        if (FirebaseUserManager.isLoggedIn()) {
            updateLocalSynchronizedSetting();
        } else {
            doLoginToFirebaseThenUpdateLocalSynchronizedSetting();
        }
    }

    private void updateLocalSynchronizedSetting() {
        this.setProgressStatus(PROGRESS_SETTING);
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                setSyncedSettingWithUserData();
                scheduleRegularReminders();
                initializeFCM();
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

    private void doLoginToFirebaseThenUpdateLocalSynchronizedSetting() {
        //FirebaseUserManager.authenticate(this, new OnCompleteListener<AuthResult>(){
        FirebaseUserManager.authenticateWithCustomToken(this, this.firebaseToken,
                new OnCompleteListener<AuthResult>(){
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    updateLocalSynchronizedSetting();
                } else {
                    getTryAgainSnackbar(getString(R.string.error_firebase_db)).show();
                }
            }
        });
    }

    private void setSyncedSettingWithUserData() {
        SynchronizedSetting synchronizedSetting =
                SynchronizedSettingRepository.getLocalInstance(getApplicationContext());
        synchronizedSetting.setGroup(this.storywell.getGroup());
        SynchronizedSettingRepository.saveLocalAndRemoteInstance(
                synchronizedSetting, getApplicationContext());
    }

    private void scheduleRegularReminders() {
        SynchronizedSetting setting = storywell.getSynchronizedSetting();

        if (setting.isRegularReminderSet()) {
            // Do not do anything
        } else {
            registerNotificationChannel();
            RegularReminderReceiver.scheduleRegularReminders(this);
            setting.setRegularReminderSet(true);
            SynchronizedSettingRepository.saveLocalAndRemoteInstance(setting, this);
        }
    }

    private void registerNotificationChannel() {
        RegularNotificationManager.createNotificationChannel(
                getString(R.string.notification_default_channel_id),
                getString(R.string.notification_default_channel_name),
                getString(R.string.notification_default_chennel_desc),
                this);
    }

    private void initializeFCM() {
        FcmNotificationService.initializeFCM(this);
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
