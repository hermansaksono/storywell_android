package edu.neu.ccs.wellness.storytelling;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import edu.neu.ccs.wellness.storytelling.utils.AsyncDownloadChallenges;

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

    public static final String KEY_TAB_INDEX = "HOME_TAB_INDEX";
    public static final int NUMBER_OF_FRAGMENTS = 3;
    public static final int TAB_STORYBOOKS = 0;
    public static final int TAB_TREASURES = 1;
    public static final int TAB_ADVENTURE = 2;


    /**
     * Icons for the Title Strip
     */
    final int[] ICONS = new int[]{
            R.drawable.ic_book_white_24,
            R.drawable.ic_gift_white_24,
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

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mStoryHomeViewPager;

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        /**
         *  Create the adapter that will return a fragment for each of the three
         *  primary sections of the activity on the HomePage
         *  */
        mScrolledTabsAdapter = new HomePageFragmentsAdapter(getSupportFragmentManager());

        /**
         *  Set up the ViewPager with the sections adapter.
         *  Similar to ListView and ArrayAdapter
         *  */

        mStoryHomeViewPager = findViewById(R.id.container);
        assert mStoryHomeViewPager != null;
        mStoryHomeViewPager.setAdapter(mScrolledTabsAdapter);


        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mStoryHomeViewPager);

        /**
         * Set the icons for the title Strip
         */
        try {
            tabLayout.getTabAt(TAB_STORYBOOKS).setIcon(ICONS[TAB_STORYBOOKS]);
            tabLayout.getTabAt(TAB_TREASURES).setIcon(ICONS[TAB_TREASURES]);
            tabLayout.getTabAt(TAB_ADVENTURE).setIcon(ICONS[TAB_ADVENTURE]);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        new AsyncDownloadChallenges(getApplicationContext()).execute();
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class HomePageFragmentsAdapter extends FragmentPagerAdapter {

        public HomePageFragmentsAdapter(FragmentManager fm) {
            super(fm);
        }

        /**
         * Retrieves each of the fragment and sets them via position
         */
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case TAB_STORYBOOKS:
                    return StoryListFragment.newInstance();
                case TAB_TREASURES:
                    return TreasureListFragment.newInstance();
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

        /**
         * Set the Title Text for the pager title Strip
         */
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case TAB_STORYBOOKS:
                    return getString(R.string.title_stories);
                case TAB_TREASURES:
                    return getString(R.string.title_treasures);
                case TAB_ADVENTURE:
                    return getString(R.string.title_activities);
                default:
                    return getString(R.string.title_stories);
            }
        }
    }

}
