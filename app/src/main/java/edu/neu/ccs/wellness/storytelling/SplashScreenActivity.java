package edu.neu.ccs.wellness.storytelling;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import edu.neu.ccs.wellness.server.RestServer;
import edu.neu.ccs.wellness.storytelling.firstrun.FirstRunActivity;

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
        //startGameActivity();

        if (this.storywell.isFirstRunCompleted() == false) {
            starFirstRun();
        } else if (this.storywell.userHasLoggedIn() == true) {
            initApp();
        } else {
            startLoginActivity();
        }
    }

    private void initApp() {
        this.preloadResources();
    }

    private void starFirstRun() {
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
                statusTextView.setText(R.string.error_no_internet);
            } else if (result == RestServer.ResponseType.SUCCESS_202) {
                //new DownloadChallengeAsync().execute();
                startHomeActivity();
            }
        }
    }

    /* ASYNCTASK To get UnitChallenge info */
    private class DownloadChallengeAsync extends AsyncTask<Void, Integer, RestServer.ResponseType> {

        protected RestServer.ResponseType doInBackground(Void... voids) {
            if (!storywell.isServerOnline()) {
                return RestServer.ResponseType.NO_INTERNET;
            } else {
                //storywell.getChallengeManager().download(getApplicationContext()); // TODO uncomment below later (added by Herman)
                return RestServer.ResponseType.SUCCESS_202;
            }
        }

        protected void onPostExecute(RestServer.ResponseType result) {
            if (result == RestServer.ResponseType.NO_INTERNET) {
                Log.e("WELL challenge d/l", result.toString());
                statusTextView.setText(R.string.error_no_internet);
            } else if (result == RestServer.ResponseType.SUCCESS_202) {
                Log.i("WELL challenge d/l", "Downloaded");
                /*

                //TODO RK testing Fitness Manager
                GroupFitness groupFitness = (GroupFitness) storywell.getFitnessManager().getMultiDayFitness(new Date(2017, 06, 01), new Date(), new Date(new Date().getTime()-9000));

                //TODO RK testing ChallengeProgressCalculator
                RunningChallenge runningChallenge = storywell.getChallengeManager().getRunningChallenge();
                ChallengeProgressCalculator challengeProgressCalculator = new ChallengeProgressCalculator(runningChallenge, groupFitness);
                Map.Entry<Person, MultiDayFitness> entry = groupFitness.getPersonMultiDayFitnessMap().entrySet().iterator().next();
                Person person = entry.getKey();
                Map<Date,Float> progressMap = null;
                try {
                    //TODO method 1
                    progressMap = challengeProgressCalculator.getPersonProgress(person);
                } catch (PersonDoesNotExistException e) {
                    e.printStackTrace();
                }
                //TODO method 2
                float overallGroupProgress = challengeProgressCalculator.getGroupProgress();

                //Specific date to search
                String dateStr = "Sat Jan 07 00:06:00 GMT 2017";
                DateFormat formatter = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy");
                Date date = null;
                try {
                    date = (Date)formatter.parse(dateStr);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                //TODO method 3
                try {
                    float overallGroupProgressByDate = challengeProgressCalculator.getOverallGroupProgressByDate(date);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                */
                startHomeActivity();
            }
        }
    }
}
