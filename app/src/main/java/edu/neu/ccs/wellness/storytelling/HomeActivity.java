package edu.neu.ccs.wellness.storytelling;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import edu.neu.ccs.wellness.storytelling.homeview.AdventurePresenter;
import edu.neu.ccs.wellness.utils.WellnessIO;

public class HomeActivity extends AppCompatActivity
        implements AdventurePresenter.AdventurePresenterListener {

    public static final String KEY_DEFAULT_TAB = "KEY_DEFAULT_TAB";
    public static final String KEY_TAB_INDEX = "HOME_TAB_INDEX";
    public static final int CODE_REFRESH_CHALLENGE_INFO = 123;

    // TABS RELATED CONSTANTS
    public static final int NUMBER_OF_FRAGMENTS = 3;
    public static final int TAB_STORYBOOKS = 0;
    public static final int TAB_ADVENTURE = 1;
    public static final int TAB_TREASURES = 2;

    // TABS RELATED VARIABLES
    private final int[] TAB_ICONS = new int[]{
            R.drawable.ic_book_white_24,
            R.drawable.ic_round_baloons_24px,
            R.drawable.ic_gift_white_24
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
        tabLayout.getTabAt(TAB_TREASURES).setIcon(TAB_ICONS[TAB_TREASURES]);
    }

    @Override
    protected void onResume() {
        super.onResume();
        doSetCurrentTabFromSharedPrefs();
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.setTabToSharedPrefs(mStoryHomeViewPager.getCurrentItem());
    }

    @Override
    public void goToStoriesTab(String highlightedStoryId) {
        this.goToThisTab(TAB_STORYBOOKS);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == CODE_REFRESH_CHALLENGE_INFO && resultCode == Activity.RESULT_OK) {
            AdventureFragment fragment = (AdventureFragment) getSupportFragmentManager()
                    .findFragmentByTag("android:switcher:" + R.id.container + ":" + TAB_ADVENTURE);
            fragment.updateChallengeAndFitnessData();
        }
    }

    /* PRIVATE METHODS */
    private void goToThisTab(int tabPosition) {
        if (tabPosition >= 0 && tabPosition < NUMBER_OF_FRAGMENTS) {
            mStoryHomeViewPager.setCurrentItem(tabPosition);
        }
    }

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
                case TAB_ADVENTURE:
                    return AdventureFragment.newInstance();
                case TAB_TREASURES:
                    return TreasureListFragment.newInstance();
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
                case TAB_ADVENTURE:
                    return getString(R.string.title_activities);
                case TAB_TREASURES:
                    return getString(R.string.title_treasures);
                default:
                    return getString(R.string.title_stories);
            }
        }
    }

}
