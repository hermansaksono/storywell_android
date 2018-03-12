package edu.neu.ccs.wellness.storytelling.firstrun;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

    private static final int INTRO_FRAGMENT = 0;
    private static final int DETAIL_FRAGMENT = 1;
    private static final int AUDIO_PERMISSION_FRAGMENT = 2;
    private static final int COMPLETED_FRAGMENT = 3;

    private ViewPager viewPagerFirstRun;
    private FirstRunFragmentManager firstRunFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firstrun);

        this.firstRunFragmentManager = new FirstRunFragmentManager(getSupportFragmentManager());
        this.viewPagerFirstRun = findViewById(R.id.splashScreenViewPager);
        this.viewPagerFirstRun.setAdapter(this.firstRunFragmentManager);
    }

    @Override
    public void onAudioPermissionGranted() {
        this.viewPagerFirstRun.setCurrentItem(3);
    }

    @Override
    public void onFirstRunCompleted() {
        Storywell storywell = new Storywell(getApplicationContext());
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
        public Fragment getItem(int position) {
            switch (position) {
                case INTRO_FRAGMENT:
                    return AppIntroductionFragment.newInstance();
                case DETAIL_FRAGMENT:
                    return AppDetailFragment.newInstance();
                case AUDIO_PERMISSION_FRAGMENT:
                    return AskPermissionsFragment.newInstance();
                case COMPLETED_FRAGMENT:
                    return FirstRunCompletedFragment.newInstance();
                default:
                    return AppIntroductionFragment.newInstance();
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    }
}
