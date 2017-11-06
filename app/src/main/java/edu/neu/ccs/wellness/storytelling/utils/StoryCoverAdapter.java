package edu.neu.ccs.wellness.storytelling.utils;

import android.content.Context;

import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import edu.neu.ccs.wellness.storytelling.R;
import edu.neu.ccs.wellness.story.interfaces.StoryInterface;
import edu.neu.ccs.wellness.story.interfaces.StoryType;

/**
 * Created by baharsheikhi on 6/22/17
 */

public class StoryCoverAdapter extends BaseAdapter {
    private Context context;
    private List<StoryInterface> stories;
    private static final String STORYLIST_FONT = "fonts/pangolin_regular.ttf";
    private final DisplayImageOptions options = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.img_placeholder)
            .showImageForEmptyUri(R.drawable.img_failure)
            .showImageOnFail(R.drawable.img_failure)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .considerExifParams(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .build();

    public StoryCoverAdapter(Context context, List<StoryInterface> stories) {
        this.context = context;
        this.stories = stories;
    }

    public int getCount() {
        return stories.size();
    }

    public View getItem(int position) { return null; }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final StoryInterface story = stories.get(position);
        ViewHolder gridViewImageHolder;
        View view = convertView;

        if (convertView == null) {
            view = getInflater().inflate(R.layout.item_storybook, parent, false);
            ImageView imageView = view.findViewById(R.id.imageview_cover_art);
            gridViewImageHolder = new ViewHolder(view, imageView);
            view.setTag(gridViewImageHolder);
        }
        else {
            gridViewImageHolder = (ViewHolder) view.getTag();
        }

        if (story.getStoryType() == StoryType.STORY) {
            ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.displayImage(story.getCoverUrl(), gridViewImageHolder.imageView, options);
        } else if (story.getStoryType() == StoryType.APP) {
            gridViewImageHolder.imageView.setImageResource(getDrawableResId(story.getCoverUrl()));
        }

        TextView textView = view.findViewById(R.id.textview_book_name);
        textView.setText(story.getTitle());
        setTextViewTypeface(textView, STORYLIST_FONT);

        return view;
    }

    static class ViewHolder {
        View view;
        ImageView imageView;

        public ViewHolder (View view, ImageView imageView) {
            this.view = view;
            this.imageView = imageView;
        }
    }

    // PRIVATE METHODS
    private void setTextViewTypeface(TextView tv, String fontAsset) {
        Typeface tf = Typeface.createFromAsset(context.getAssets(), fontAsset);
        tv.setTypeface(tf);
    }

    private LayoutInflater getInflater() {
        return (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    private int getDrawableResId(String resName) {
        return context.getResources().getIdentifier(resName, "drawable", context.getPackageName());
    }
}