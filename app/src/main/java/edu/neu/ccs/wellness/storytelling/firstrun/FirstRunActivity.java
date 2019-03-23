package edu.neu.ccs.wellness.storytelling.firstrun;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import edu.neu.ccs.wellness.notifications.RegularNotificationManager;
import edu.neu.ccs.wellness.storytelling.R;
import edu.neu.ccs.wellness.storytelling.SplashScreenActivity;
import edu.neu.ccs.wellness.storytelling.Storywell;
import edu.neu.ccs.wellness.utils.WellnessBluetooth;

/**
 * Created by hermansaksono on 3/11/18.
 */

public class FirstRunActivity extends AppCompatActivity implements
        CompletedFirstRunFragment.OnFirstRunCompletedListener, OnPermissionChangeListener {

    private static final int INTRO_FRAGMENT = 0;
    private static final int DETAIL_FRAGMENT = 1;
    private static final int AUDIO_PERMISSION_FRAGMENT = 2;
    private static final int BLUETOOTH_PERMISSION_FRAGMENT = 3;
    private static final int COMPLETED_FRAGMENT = 4;
    private static final int GOOGLE_PLAY_FRAGMENT = 5;
    private static final int NUM_PAGES = 5;

    private ViewPager viewPagerFirstRun;
    private FirstRunFragmentManager firstRunFragmentManager;
    private int currentFragmentPos = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firstrun);

        this.firstRunFragmentManager = new FirstRunFragmentManager(getSupportFragmentManager());
        this.viewPagerFirstRun = findViewById(R.id.splashScreenViewPager);
        this.viewPagerFirstRun.setAdapter(this.firstRunFragmentManager);

        this.viewPagerFirstRun.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int pos, float offset, int positionOffsetPixels) { }

            @Override
            public void onPageSelected(int position) {
                /*
                currentFragmentPos = position;
                int gotoPosition = position;
                if (fragmentIsLockedAt + 1 == position) {
                    gotoPosition = fragmentIsLockedAt;
                }
                viewPagerFirstRun.setCurrentItem(gotoPosition);
                */
                if (canGoToThisNextFragment(position)) {
                    currentFragmentPos = position;
                }
                viewPagerFirstRun.setCurrentItem(currentFragmentPos);
            }

            @Override
            public void onPageScrollStateChanged(int state) { }
        });
    }

    @Override
    public void onPermissionGranted() {
        this.viewPagerFirstRun.setCurrentItem(this.currentFragmentPos + 1);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //Get the requestCode and check our case
        switch (requestCode) {
            case AskAudioRecordingPermissionsFragment.REQUEST_AUDIO_PERMISSIONS:
                //If Permission is Granted, change the boolean value
                if (AskAudioRecordingPermissionsFragment.isRecordingGranted(grantResults)) {
                    viewPagerFirstRun.setCurrentItem(this.currentFragmentPos + 1);
                } else {
                    //showSnackBar(getString(R.string.firstrun_snackbar_mustsetaudio));
                }
                break;
            case WellnessBluetooth.PERMISSION_REQUEST_COARSE_LOCATION:
                if (AskBluetoothPermissionsFragment.isPermissionGranted(grantResults)) {
                    viewPagerFirstRun.setCurrentItem(this.currentFragmentPos + 1);
                } else {
                    //
                }
        }
    }

    @Override
    public void onFirstRunCompleted() {
        Storywell storywell = new Storywell(getApplicationContext());
        storywell.setIsFirstRunCompleted(true);
        this.registerNotificationChannel();
        this.startUsingStorywell();
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

    private void startUsingStorywell() {
        Intent intent = new Intent(this, SplashScreenActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    /*
    @Override
    public void lockFragmentPager() {
        this.fragmentIsLockedAt = this.currentFragmentPos + 1;
        if (this.listOfLockedFragment.contains(this.currentFragmentPos + 1) == false)
            this.listOfLockedFragment.add(this.currentFragmentPos + 1);
    }

    @Override
    public void unlockFragmentPager() {
        this.fragmentIsLockedAt = -1;
        this.listOfLockedFragment.remove(this.currentFragmentPos);
    }

    @Override
    public boolean isFragmentLocked() {
        //return this.fragmentIsLockedAt != -1;
        return this.listOfLockedFragment.contains(-90);
    }
    */

    private boolean canGoToThisNextFragment(int position) {
        int currentPosition = position - 1;
        switch(currentPosition) {
            case AUDIO_PERMISSION_FRAGMENT:
                return AskAudioRecordingPermissionsFragment.isRecordingAllowed(getApplicationContext());
            case BLUETOOTH_PERMISSION_FRAGMENT:
                return WellnessBluetooth.isCoarseLocationAllowed(getApplicationContext());
            default:
                return true;
        }
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
                    return AskAudioRecordingPermissionsFragment.newInstance();
                case BLUETOOTH_PERMISSION_FRAGMENT:
                    return AskBluetoothPermissionsFragment.newInstance();
                case GOOGLE_PLAY_FRAGMENT:
                    return GooglePlayFragment.newInstance();
                case COMPLETED_FRAGMENT:
                    return CompletedFirstRunFragment.newInstance();
                default:
                    return AppIntroductionFragment.newInstance();
            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }
}
