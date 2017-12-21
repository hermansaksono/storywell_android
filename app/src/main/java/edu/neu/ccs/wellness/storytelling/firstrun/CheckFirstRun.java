package edu.neu.ccs.wellness.storytelling.firstrun;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import edu.neu.ccs.wellness.storytelling.LoginActivity;
import edu.neu.ccs.wellness.storytelling.R;
import edu.neu.ccs.wellness.storytelling.SplashScreenActivity;
import edu.neu.ccs.wellness.storytelling.Storywell;

public class CheckFirstRun extends AppCompatActivity {

    public static boolean checkFirstRunBoolean;
    public static ViewPager mViewPagerFirstRun;
    private SharedPreferences sharedPreferencesInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        mViewPagerFirstRun = (ViewPager) findViewById(R.id.splashScreenViewPager);

        /**Get the default Shared Preferences*/
        sharedPreferencesInstance = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        /**Set a variable an a default value*/
        checkFirstRunBoolean = sharedPreferencesInstance.getBoolean("isFirstRun", true);

        if (checkFirstRunBoolean) {

            /**Update First Run variable in Shared Preference*/
            editPreferenceManager();

            /**
             * Educate the user about the app
             * Ask for Permissions during first Run
             * Do any other first run task
             * */
            try {
                firstRunFragmentManager firstRunFragments = new firstRunFragmentManager(getSupportFragmentManager());
                mViewPagerFirstRun.setAdapter(firstRunFragments);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }//check for First Run Boolean
        else {

            /**
             * This is not a first run.
             * Check for login now
             * */
            if (Storywell.userHasLoggedIn(this)) {
                sendToSplashScreen();
            } else {
                sendToLoginScreen();
            }
        }

    }//End of onCreate

    /**
     * If user has logged in, simply send user to splash screen
     * Show splash screen to user and in the background, run the AsyncTasks
     */
    private void sendToSplashScreen() {
        Intent sendToSplashScreenIntent = new Intent(this, SplashScreenActivity.class);
        startActivity(sendToSplashScreenIntent);
        finish();
    }

    /**
     * If user has not logged in and this is not the first run as well-
     * Simply show user login screen
     */
    private void sendToLoginScreen() {
        Intent sendToLoginScreenIntent = new Intent(this, LoginActivity.class);
        sendToLoginScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(sendToLoginScreenIntent);
        finish();
    }

    /**
     * Update shared Preference for next run
     */
    private void editPreferenceManager() {
        SharedPreferences.Editor editPref = sharedPreferencesInstance.edit();
        editPref.putBoolean("isFirstRun", false);
        editPref.apply();
    }


    /**
     * If this is the first run, show the first run fragments to educate the user
     */
    private class firstRunFragmentManager extends FragmentPagerAdapter {

        firstRunFragmentManager(FragmentManager fm) {
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return EducateBasicsApp.newInstance();
                case 1:
                    return AskPermissionsFragment.newInstance();
                case 2:
                    return EducateChallengesApp.newInstance();
                default:
                    return EducateBasicsApp.newInstance();
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    }

}