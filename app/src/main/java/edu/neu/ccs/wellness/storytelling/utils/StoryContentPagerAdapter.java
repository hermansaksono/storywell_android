package edu.neu.ccs.wellness.storytelling.utils;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

import edu.neu.ccs.wellness.story.interfaces.StoryContent;
import edu.neu.ccs.wellness.story.interfaces.StoryInterface;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 *
 * Created by hermansaksono on 1/25/19.
 */
public class StoryContentPagerAdapter extends FragmentPagerAdapter {

    private List<Fragment> fragments = new ArrayList<Fragment>();

    public StoryContentPagerAdapter(FragmentManager fm, StoryInterface story, Context context) {
        super(fm);
        for (StoryContent content : story.getContents()) {
            this.fragments.add(StoryContentAdapter.getFragment(content, context));
        }
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
