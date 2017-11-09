package edu.neu.ccs.wellness.storytelling.firstrun;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import edu.neu.ccs.wellness.storytelling.HomeActivity;
import edu.neu.ccs.wellness.storytelling.R;

import static edu.neu.ccs.wellness.storytelling.storyview.ReflectionFragment.isPermissionGranted;

public class SplashScreen extends AppCompatActivity {

    private SharedPreferences myPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        firstRunFragmentManager firstRunFragments = new firstRunFragmentManager(getSupportFragmentManager());

        ViewPager mViewPager = (ViewPager) findViewById(R.id.splashScreenViewPager);
        myPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        Boolean isFirstRun = myPreferences.getBoolean("isFirstRun", true);

        if (isFirstRun) {
            //Ask for Permissions during first Run
            try {
                mViewPager.setAdapter(firstRunFragments);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (isPermissionGranted) {
                editPreferenceManager();
            }
        } else {

                findViewById(R.id.splashScreenViewPager).setVisibility(View.GONE);
                //Simply show a splash screen and load resources in background
                new CountDownTimer(1000, 1000) {
                    @Override
                    public void onTick(long l) {
                        //Call AsyncTasks Here
                    }

                    @Override
                    public void onFinish() {
                        Intent startApp = new Intent(SplashScreen.this, HomeActivity.class);
                        startActivity(startApp);
                        finish();
                    }

                }.start();
            }
        }

    private void editPreferenceManager() {
        SharedPreferences.Editor editPref = myPreferences.edit();
        editPref.putBoolean("isFirstRun", false);
        editPref.apply();
    }



    private class firstRunFragmentManager extends FragmentPagerAdapter {

        public firstRunFragmentManager(FragmentManager fm) {
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return FirstRunTutorial1.newInstance();
                case 1:
                    return FirstRunTutorial2.newInstance();
                case 2:
                    return FirstRunTutorial3.newInstance();
                default:
                    return FirstRunTutorial1.newInstance();
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    }

}
