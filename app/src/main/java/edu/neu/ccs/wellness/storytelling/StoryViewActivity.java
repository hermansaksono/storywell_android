package edu.neu.ccs.wellness.storytelling;

import android.support.v7.app.AppCompatActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import edu.neu.ccs.wellness.storytelling.storyview.StoryCoverFragment;
import edu.neu.ccs.wellness.storytelling.storyview.StoryPageFragment;
import edu.neu.ccs.wellness.storytelling.storyview.ReflectionStartFragment;
import edu.neu.ccs.wellness.storytelling.storyview.ReflectionFragment;
import edu.neu.ccs.wellness.storytelling.storyview.StatementFragment;
import edu.neu.ccs.wellness.storytelling.storyview.ChallengeInfoFragment;
import edu.neu.ccs.wellness.storytelling.storyview.ChallengePickerFragment;
import edu.neu.ccs.wellness.utils.CardStackPageTransformer;

public class StoryViewActivity extends AppCompatActivity {
    public static final float PAGE_MIN_SCALE = 0.75f;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private StoryContentPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_content_view);

        mSectionsPagerAdapter = new StoryContentPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setPageTransformer(true, new CardStackPageTransformer(PAGE_MIN_SCALE));

    }

    @Override
    public void onStart() {
        super.onStart();
        showNavigationInstruction();
    }

    // PRIVATE METHODS
    private void showNavigationInstruction() {
        String navigationInfo = getString(R.string.tooltip_storycontent_navigation);
        Toast toast = Toast.makeText(getApplicationContext(), navigationInfo, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP|Gravity.CENTER, 0, 0);
        toast.show();
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class StoryContentPagerAdapter extends FragmentPagerAdapter {

        private List<Fragment> fragments = new ArrayList<Fragment>();

        public StoryContentPagerAdapter(FragmentManager fm) {
            super(fm);
            this.fragments.add(new StoryCoverFragment());
            this.fragments.add(StoryPageFragment.create(getString(R.string.story_default_text)));
            this.fragments.add(StoryPageFragment.create("2"));
            this.fragments.add(StoryPageFragment.create("3"));
            this.fragments.add(new ReflectionStartFragment());
            this.fragments.add(ReflectionFragment.create(getString(R.string.reflection_text)));
            this.fragments.add(ReflectionFragment.create("What do you like when you were physically active with your %s?"));
            this.fragments.add(new StatementFragment());
            this.fragments.add(new ChallengeInfoFragment());
            this.fragments.add(new ChallengePickerFragment());
        }

        @Override
        public Fragment getItem(int position) {
            return this.fragments.get(position);
        }

        @Override
        public int getCount() {
            return this.fragments.size();
        }
    }
}
