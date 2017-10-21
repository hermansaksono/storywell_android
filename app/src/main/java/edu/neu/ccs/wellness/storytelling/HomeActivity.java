package edu.neu.ccs.wellness.storytelling;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import static edu.neu.ccs.wellness.storytelling.storyview.ReflectionFragment.mReflectionsAudioFile;


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

    private static int NUMBER_OF_FRAGMENTS = 3;

    /**
     * Icons for the Title Strip
     */
    final int[] ICONS = new int[]{
            R.mipmap.ic_book_white_24dp,
            R.mipmap.ic_pages_white_24dp,
            R.mipmap.ic_directions_walk_white_24dp
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

        mStoryHomeViewPager = (ViewPager) findViewById(R.id.container);
        assert mStoryHomeViewPager != null;
        mStoryHomeViewPager.setAdapter(mScrolledTabsAdapter);


        /**
         * Set the icons for the title Strip
         * */
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        if (tabLayout != null) {
            tabLayout.setupWithViewPager(mStoryHomeViewPager);
            tabLayout.getTabAt(0).setIcon(ICONS[0]);
            tabLayout.getTabAt(1).setIcon(ICONS[1]);
            tabLayout.getTabAt(2).setIcon(ICONS[2]);
        }

        /**
         * TODO: Clear Cache Files after talking about strategy
         * */
        if (!TextUtils.isEmpty(mReflectionsAudioFile)) {
//            getApplicationContext().deleteFile();
//            Toast.makeText(this, "FILE DELETED", Toast.LENGTH_LONG).show();
        }

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
                case 0:
                    return StoryListFragment.newInstance();
                case 1:
                    return TreasureListFragment.newInstance();
                case 2:
                    return ActivitiesFragment.newInstance();

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
                case 0:
                    return getString(R.string.title_stories);
                case 1:
                    return getString(R.string.title_treasures);
                case 2:
                    return getString(R.string.title_activities);
                default:
                    return getString(R.string.title_stories);
            }
        }
    }
}
