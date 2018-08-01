package edu.neu.ccs.wellness.storytelling;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.neu.ccs.wellness.tracking.Event;
import edu.neu.ccs.wellness.tracking.Param;
import edu.neu.ccs.wellness.tracking.UserTrackDetails;
import edu.neu.ccs.wellness.tracking.WellnessUserTracking;
import edu.neu.ccs.wellness.utils.AnalyticsApplication;
import edu.neu.ccs.wellness.utils.WellnessIO;

/**
 * This Activity loads all the three Fragments
 * {@Link StoryListFragment}
 * The first Tab/Fragment visible to user which has the list of Stories
 * {@Link TreasureListFragment}
 * The second tab
 * {@Link ActivitiesFragment}
 * The Graph and charts
 */
public class HomeActivity extends AppCompatActivity {

    public static final String KEY_DEFAULT_TAB = "KEY_DEFAULT_TAB";
    public static final String KEY_TAB_INDEX = "HOME_TAB_INDEX";

    // TABS RELATED CONSTANTS
    public static final int NUMBER_OF_FRAGMENTS = 2;
    public static final int TAB_STORYBOOKS = 0;
    public static final int TAB_ADVENTURE = 1;
    public static final int TAB_TREASURES = 1; // This is currently not used

    // TABS RELATED VARIABLES
    private final int[] TAB_ICONS = new int[]{
            R.drawable.ic_book_white_24,
            // R.drawable.ic_gift_white_24,
            R.drawable.ic_run_fast_white_24
    };
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private HomePageFragmentsAdapter mScrolledTabsAdapter;
    private ViewPager mStoryHomeViewPager;

    private FirebaseAnalytics mFirebaseAnalytics;
    private Tracker mTracker;
    private Storywell storywell;
    private WellnessUserTracking wellnessUserTracking;
    private Bundle eventParams;
    private DatabaseReference databaseReference;


    // SUPERCLASS METHODS
    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mScrolledTabsAdapter = new HomePageFragmentsAdapter(getSupportFragmentManager());

        mStoryHomeViewPager = findViewById(R.id.container);
        mStoryHomeViewPager.setAdapter(mScrolledTabsAdapter);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mStoryHomeViewPager);
        tabLayout.getTabAt(TAB_STORYBOOKS).setIcon(TAB_ICONS[TAB_STORYBOOKS]);
        tabLayout.getTabAt(TAB_ADVENTURE).setIcon(TAB_ICONS[TAB_ADVENTURE]);

        //Firebase Track
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        storywell = new Storywell(getApplicationContext());

        //TODO userId in WellnessUser ??
        wellnessUserTracking = storywell.getUserTracker("108");
        eventParams = new Bundle();
        eventParams.putString(Param.ACTIVITY_NAME, "HOME_ACTIVITY");
        wellnessUserTracking.logEvent(Event.ACTIVITY_OPEN, eventParams);

        // new AsyncDownloadChallenges(getApplicationContext()).execute();
        // TODO this is handled by the AdventureFragment, but we can move it here
    }

    @Override
    protected void onResume() {
        super.onResume();
        doSetCurrentTabFromSharedPrefs();
        handleAnalytics();
    }

    private void handleAnalytics(){
        final String A = String.valueOf(new Date().getTime() - (1000*60*60*24*7));

        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("user_tracking").child("108").orderByChild("timestamp").startAt(A).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                HashMap<String, String> event = (HashMap<String, String>) dataSnapshot.getValue();
                String eventName = event.get("eventName");
                String B = String.valueOf(new Date().getTime());
                Date D1 = new Date(Long.parseLong(A));
                Date D2 = new Date(Long.parseLong(B));

                databaseReference.child("user_tracking").child("108").child("last_7_days").child(D1+" to "+D2).setValue(eventName);

                Log.d("ANALYTICS: ", dataSnapshot.toString());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        this.setTabToSharedPrefs(mStoryHomeViewPager.getCurrentItem());
    }

    /* PRIVATE METHODS */
    private void doSetCurrentTabFromSharedPrefs() {
        int tabPosition = WellnessIO.getSharedPref(this)
                .getInt(KEY_DEFAULT_TAB, TAB_STORYBOOKS);
        mStoryHomeViewPager.setCurrentItem(tabPosition);
        resetCurrentTab();
    }

    private void setTabToSharedPrefs(int position) {
        WellnessIO.getSharedPref(this).edit()
                .putInt(KEY_DEFAULT_TAB, position)
                .apply();
    }

    private void resetCurrentTab() {
        WellnessIO.getSharedPref(this).edit()
                .remove(KEY_DEFAULT_TAB)
                .apply();
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class HomePageFragmentsAdapter extends FragmentPagerAdapter {

        public HomePageFragmentsAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case TAB_STORYBOOKS:
                    return StoryListFragment.newInstance();
                // case TAB_TREASURES:
                //    return TreasureListFragment.newInstance();
                case TAB_ADVENTURE:
                    return AdventureFragment.newInstance();

                default:
                    return StoryListFragment.newInstance();
            }
        }

        @Override
        public int getCount() {
            return NUMBER_OF_FRAGMENTS;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case TAB_STORYBOOKS:
                    return getString(R.string.title_stories);
                // case TAB_TREASURES:
                //    return getString(R.string.title_treasures);
                case TAB_ADVENTURE:
                    return getString(R.string.title_activities);
                default:
                    return getString(R.string.title_stories);
            }
        }
    }

}
