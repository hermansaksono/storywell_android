package edu.neu.ccs.wellness.storytelling;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;

import java.io.IOException;

import edu.neu.ccs.wellness.fitness.interfaces.ChallengeStatus;
import edu.neu.ccs.wellness.logging.WellnessUserLogging;
import edu.neu.ccs.wellness.notifications.RegularNotificationManager;
import edu.neu.ccs.wellness.people.Group;
import edu.neu.ccs.wellness.server.RestServer;
import edu.neu.ccs.wellness.server.RestServer.ResponseType;
import edu.neu.ccs.wellness.story.StoryManager;
import edu.neu.ccs.wellness.storytelling.firstrun.FirstRunActivity;
import edu.neu.ccs.wellness.storytelling.firstrun.HeroPickerFragment;
import edu.neu.ccs.wellness.storytelling.notifications.BatteryReminderReceiver;
import edu.neu.ccs.wellness.storytelling.notifications.RegularReminderReceiver;
import edu.neu.ccs.wellness.storytelling.settings.SynchronizedSetting;
import edu.neu.ccs.wellness.storytelling.settings.SynchronizedSettingRepository;
import edu.neu.ccs.wellness.storytelling.utils.UserLogging;
import edu.neu.ccs.wellness.utils.DeviceInfo;
import edu.neu.ccs.wellness.utils.WellnessBluetooth;
import edu.neu.ccs.wellness.utils.WellnessIO;
import io.fabric.sdk.android.Fabric;

import static edu.neu.ccs.wellness.utils.WellnessBluetooth.PERMISSION_REQUEST_COARSE_LOCATION;

public class SplashScreenActivity extends AppCompatActivity {
    private Storywell storywell;
    private SynchronizedSetting setting;
    private TextView statusTextView;
    private ProgressBar progressBar;
    private static final int PROGRESS_SETTING = 0;
    private static final int PROGRESS_STORIES = 1;
    private static final int PROGRESS_GROUP = 2;
    private static final int PROGRESS_CHALLENGES = 3;
    private static final int PROGRESS_COMPLETED = 4;
    private static final int[] PROGRESS_STRINGS = new int[]{
            R.string.splash_text_01,
            R.string.splash_download_stories,
            R.string.splash_download_group,
            R.string.splash_download_challenges};

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
        Fabric.with(this, new Crashlytics());

        if (!this.storywell.userHasLoggedIn()) {
            startLoginActivity();
            return;
        }

        if (this.storywell.isReloginNeeded()) {
            this.getLoginExpiredSnackbar(getString(R.string.error_relogin_required)).show();
            return;
        }

        if (!this.storywell.isFirstRunCompleted()) {
            startFirstRun();
        } else {
            refreshSettingsThenContinue();
        }
    }

    private void startLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startIntent(intent);
    }

    private void startFirstRun() {
        Intent intent = new Intent(this, FirstRunActivity.class);
        startIntent(intent);
    }

    private void refreshSettingsThenContinue() {
        if (!storywell.isServerOnline()) {
            getTryAgainSnackbar(getString(R.string.error_no_internet)).show();
        }

        SynchronizedSettingRepository.updateLocalInstance(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                setting = SynchronizedSettingRepository.getLocalInstance(getApplicationContext());
                setProgressStatus(PROGRESS_SETTING);
                setActiveHomeTab(getIntent());
                prepareFCM();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                getTryAgainSnackbar("We have a problem connecting to the server").show();
            }
        }, getApplicationContext());
    }

    private void setActiveHomeTab(Intent intent) {
        if (intent.getExtras() != null && intent.getExtras()
                .containsKey(HomeActivity.KEY_DEFAULT_TAB)) {
            WellnessIO.getSharedPref(this).edit()
                    .putInt(HomeActivity.KEY_DEFAULT_TAB, HomeActivity.TAB_ADVENTURE)
                    .apply();
        }
    }

    private void prepareFCM() {
        // Register notification channels
        registerNotificationChannel();

        // Schedule Regular Reminders
        if (!RegularReminderReceiver.isScheduled(this)) {
            RegularReminderReceiver.scheduleRegularReminders(this);
            setting.setRegularReminderSet(true);
            Log.d("SWELL", "Regular reminders set");
        }

        if (!BatteryReminderReceiver.isScheduled(this)) {
            BatteryReminderReceiver.scheduleBatteryReminders(this);
            setting.setRegularReminderSet(true);
            Log.d("SWELL", "Battery reminders set");
        }

        /*
        if (!this.setting.isRegularReminderSet()) {
            RegularReminderReceiver.scheduleRegularReminders(this);
            BatteryReminderReceiver.scheduleBatteryReminders(this);
            setting.setRegularReminderSet(true);
        }
        */

        // Initialize FCM
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.e("SWELL", "FCM getInstanceId failed", task.getException());
                            return;
                        } else {
                            String token = task.getResult().getToken();
                            setting.setFcmToken(token);
                            preloadDataThenStartHomeActivity();
                        }
                    }
                });

        // Subscribe to Announcements
        FirebaseMessaging.getInstance()
                .subscribeToTopic(getString(R.string.notification_announcements_channel_name));
    }

    private void registerNotificationChannel() {
        RegularNotificationManager.createNotificationChannel(
                getString(R.string.notification_default_channel_id),
                getString(R.string.notification_default_channel_name),
                getString(R.string.notification_default_channel_desc),
                this);

        RegularNotificationManager.createNotificationChannel(
                getString(R.string.notification_announcements_channel_id),
                getString(R.string.notification_announcements_channel_name),
                getString(R.string.notification_announcements_channel_desc),
                this);
    }

    private void preloadDataThenStartHomeActivity() {
        this.resetProgressIndicators();
        new FetchEverythingAsync().execute();
    }

    private void startHomeActivity() {
        UserLogging.logStartup();
        Intent intent = new Intent(this, HomeActivity.class);
        startIntent(intent);
    }

    /**
     *  AsyncTask to initialize the data
     */
    private class FetchEverythingAsync extends AsyncTask<Void, Integer, ResponseType> {
        protected RestServer.ResponseType doInBackground(Void... voids) {
            if (!storywell.isServerOnline()) {
                return RestServer.ResponseType.NO_INTERNET;
            }

            try {
                Context context = getApplicationContext();

                // Download stories
                publishProgress(PROGRESS_STORIES);
                if (setting.isStoryListNeedsRefresh()
                        || !storywell.isStoryListCacheExists(context)) {
                    storywell.deleteStoryDefs();
                    storywell.loadStoryList(true);
                    setting.setIsStoryListNeedsRefresh(false);
                } else {
                    storywell.loadStoryList(true);
                }

                // Download group info
                publishProgress(PROGRESS_GROUP);
                if (setting.isGroupInfoNeedsRefresh()) {
                    Group group = storywell.getGroup(false);
                    setting.setGroup(group);
                    setting.setIsGroupInfoNeedsRefresh(false);
                } else {
                    Group group = storywell.getGroup(true);
                    setting.setGroup(group);
                }


                // Download Challenge info
                publishProgress(PROGRESS_CHALLENGES);
                ChallengeStatus status;
                if (setting.isChallengeInfoNeedsRefresh() || !storywell.isChallengeInfoStored()) {
                    status = storywell.getChallengeManager(false).getStatus();
                    setting.setIsChallengeInfoNeedsRefresh(false);
                } else {
                    status = storywell.getChallengeManager(true).getStatus();
                }
                Log.d("SWELL", "Challenge status: " + status);

                // Complete
                return RestServer.ResponseType.SUCCESS_202;
            } catch (JSONException e) {
                Log.e("SWELL", "Storywell startup failed: Bad JSON.");
                e.printStackTrace();
                return ResponseType.BAD_JSON;
            } catch (IOException e) {
                Log.e("SWELL", "Storywell startup failed: Can't connect to server.");
                e.printStackTrace();
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

    private void setProgressStatus(Integer progressId) {
        int stringResourcesId;
        int progressPercent;
        switch(progressId) {
            case PROGRESS_SETTING:
                stringResourcesId = PROGRESS_STRINGS[PROGRESS_SETTING];
                progressPercent = 10;
                break;
            case PROGRESS_STORIES:
                stringResourcesId = PROGRESS_STRINGS[PROGRESS_STORIES];
                progressPercent = 25;
                break;
            case PROGRESS_GROUP:
                stringResourcesId = PROGRESS_STRINGS[PROGRESS_GROUP];
                progressPercent = 50;
                break;
            case PROGRESS_CHALLENGES:
                stringResourcesId = PROGRESS_STRINGS[PROGRESS_CHALLENGES];
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
                setProgressStatus(PROGRESS_COMPLETED);
                setDeviceName();
                setCrashlyticsUid();
                putIntentExtrasIntoSetting();
                saveSynchronizedSetting();
                doBluetoothCheck();
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

    private void setDeviceName() {
        String deviceName = DeviceInfo.getDeviceName();
        if (!setting.getDeviceInfo().getDeviceName().equals(deviceName)) {
            setting.getDeviceInfo().setDeviceName(deviceName);
            setting.getDeviceInfo().setAndroidVersion(Build.VERSION.SDK_INT);
            setting.getDeviceInfo().setAndroidRelease(Build.VERSION.RELEASE);
        }

        try {
            String versionName = getPackageManager()
                    .getPackageInfo(getPackageName(), 0).versionName;
            int versionCode = getPackageManager()
                    .getPackageInfo(getPackageName(), 0).versionCode;


            setting.getDeviceInfo().setAppVersionCode(versionCode);
            setting.getDeviceInfo().setAppVersionName(versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void setCrashlyticsUid() {
        if (setting != null && setting.getGroup() != null) {
            String uid = setting.getGroup().getName();
            Crashlytics.setUserIdentifier(uid);
        }
    }

    private void putIntentExtrasIntoSetting() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            setting.setHeroCharacterId(extras.getInt(HeroPickerFragment.KEY_HERO_ID, 0));
        }
    }

    private void saveSynchronizedSetting() {
        SynchronizedSettingRepository.saveLocalAndRemoteInstance(setting, this);
    }

    private void doBluetoothCheck() {
        if (WellnessBluetooth.isCoarseLocationAllowed(this)) {
            startHomeActivity();
        } else {
            WellnessBluetooth.tryRequestCoarsePermission(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startHomeActivity();
                } else {
                    getBluetoothErrorSnackbar().show();
                }
                return;
            }
        }
    }

    /* PRIVATE HELPER METHODS */
    private void resetProgressIndicators() {
        statusTextView.setText(R.string.empty);
        progressBar.setProgress(0);
    }

    private Snackbar getTryAgainSnackbar(String text) {
        Snackbar snackbar = getSnackbar(text, this);
        snackbar.setAction(R.string.button_try_again, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshSettingsThenContinue();
            }
        });
        return snackbar;
    }

    private Snackbar getLoginExpiredSnackbar(String text) {
        Snackbar snackbar = getSnackbar(text, this);
        snackbar.setAction(R.string.button_error_relogin_required, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storywell.logoutUser();
                startLoginActivity();
            }
        });
        return snackbar;
    }

    private Snackbar getBluetoothErrorSnackbar() {
        String text = getResources().getString(R.string.splash_error_bluetooth_permission);
        Snackbar snackbar = getSnackbar(text, this);
        snackbar.setAction(R.string.splash_error_bluetooth_permission_try_again,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        doBluetoothCheck();
                    }
                });
        return snackbar;
    }

    private static Snackbar getSnackbar(String text, Activity activity) {
        View gameView = activity.findViewById(R.id.splashscreenView);
        return Snackbar.make(gameView, text, Snackbar.LENGTH_INDEFINITE);
    }

    private void startIntent(Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
