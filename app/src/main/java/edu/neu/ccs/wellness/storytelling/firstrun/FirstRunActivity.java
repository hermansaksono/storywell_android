package edu.neu.ccs.wellness.storytelling.firstrun;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import edu.neu.ccs.wellness.storytelling.R;

/**
 * Created by hermansaksono on 3/11/18.
 */

public class FirstRunActivity extends AppCompatActivity {

    private ViewPager viewPagerFirstRun;
    private FirstRunFragmentManager firstRunFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        this.viewPagerFirstRun = findViewById(R.id.splashScreenViewPager);
        this.firstRunFragmentManager = new FirstRunFragmentManager(getSupportFragmentManager());
        this.viewPagerFirstRun.setAdapter(this.firstRunFragmentManager);
    }

    /**
     * If this is the first run, show the first run fragments to educate the user
     */
    private class FirstRunFragmentManager extends FragmentPagerAdapter {

        FirstRunFragmentManager(FragmentManager fm) {
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
