package edu.neu.ccs.wellness.storytelling.utils;

import android.content.Context;

import android.view.View;
import android.view.ViewGroup;

import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.List;

import edu.neu.ccs.wellness.storytelling.interfaces.StoryInterface;

/**
 * Created by baharsheikhi on 6/22/17
 */

public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    private List<View> stories;

    // Constructor
    public ImageAdapter(Context c, List<View> stories) {
        mContext = c;
        this.stories = stories;
    }

    public int getCount() {
        return stories.size();
    }

    public View getItem(int position) {
        return stories.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = stories.get(position);
        return view;
    }

}