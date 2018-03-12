package edu.neu.ccs.wellness.storytelling.firstrun;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import edu.neu.ccs.wellness.storytelling.R;
import edu.neu.ccs.wellness.storytelling.SplashScreenActivity;
import edu.neu.ccs.wellness.storytelling.Storywell;

/**
 * Created by hermansaksono on 3/11/18.
 */

public class FirstRunActivity extends AppCompatActivity implements
        AskPermissionsFragment.AudioPermissionListener,
        FirstRunCompletedFragment.FirstRunCompletedListener {

    private ViewPager viewPagerFirstRun;
    private FirstRunFragmentManager firstRunFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firstrun);

        this.viewPagerFirstRun = findViewById(R.id.splashScreenViewPager);
        this.firstRunFragmentManager = new FirstRunFragmentManager(getSupportFragmentManager());
        this.viewPagerFirstRun.setAdapter(this.firstRunFragmentManager);
    }

    @Override
    public void onAudioPermissionGranted() {
        this.viewPagerFirstRun.setCurrentItem(3);
    }

    @Override
    public void onFirstRunCompleted() {
        Storywell storywell = new Storywell(this);
        storywell.setIsFirstRunCompleted(true);
        this.startUsingStorywell();
    }

    private void startUsingStorywell() {
        Intent intent = new Intent(this, SplashScreenActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
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
                    return AppBasicsInfoFragment.newInstance();
                case 1:
                    return AppDetailsFragment.newInstance();
                case 2:
                    return AskPermissionsFragment.newInstance();
                case 3:
                    return FirstRunCompletedFragment.newInstance();
                default:
                    return AppBasicsInfoFragment.newInstance();
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    }
}
