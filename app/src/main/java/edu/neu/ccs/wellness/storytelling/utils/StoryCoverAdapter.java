package edu.neu.ccs.wellness.storytelling.utils;

import android.content.Context;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import java.util.ArrayList;
import java.util.List;

import edu.neu.ccs.wellness.storytelling.R;
import edu.neu.ccs.wellness.storytelling.StoryViewActivity;
import edu.neu.ccs.wellness.storytelling.interfaces.StoryInterface;

/**
 * Created by baharsheikhi on 6/22/17
 */

public class StoryCoverAdapter extends BaseAdapter {
    private Context mContext;
    private List<StoryInterface> stories;
    private static final String STORYLIST_FONT = "fonts/pangolin_regular.ttf";
    private final DisplayImageOptions options = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.place_holder)
            .showImageForEmptyUri(R.drawable.hand)
            .showImageOnFail(R.drawable.big_problem)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .considerExifParams(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .build();
    private static final int HEIGHT = 200;
    private static final int WIDTH = 200;

    public StoryCoverAdapter(Context c, List<StoryInterface> stories) {
        mContext = c;
        this.stories = stories;
    }

    public int getCount() {
        return stories.size();
    }

    //TODO
    public View getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return position;
    }


    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder gridViewImageHolder;
        View view = convertView;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.booklayout_storylist, parent, false);
            gridViewImageHolder = new ViewHolder();
            StoryInterface story = stories.get(position);
            ImageView imageView = (ImageView) view.findViewById(R.id.imageview_cover_art);
            ViewGroup.LayoutParams lp = imageView.getLayoutParams();
            lp.width = WIDTH;
            lp.height = HEIGHT;
            imageView.requestLayout();
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
            gridViewImageHolder.imageView = imageView;
            TextView textView = (TextView) view.findViewById(R.id.textview_book_name);
            textView.setText(story.getTitle());
            setTextViewTypeface(textView, STORYLIST_FONT);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(view.getContext(), StoryViewActivity.class);
                    mContext.startActivity(intent);
                }
            });
            gridViewImageHolder.view = view;
            view.setTag(gridViewImageHolder);
        }
        else {
            gridViewImageHolder = (ViewHolder) view.getTag();
        }
        configureDefaultImageLoader(mContext);
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage("http://wellness.ccs.neu.edu/story_static/temp/story0_pg0.png",
                gridViewImageHolder.imageView, options);
        return view;
    }


    static class ViewHolder {
        View view;
        ImageView imageView;
    }

    // PRIVATE METHODS
    private void setTextViewTypeface(TextView tv, String fontAsset) {
        Typeface tf = Typeface.createFromAsset(mContext.getAssets(), fontAsset);
        tv.setTypeface(tf);
    }


    public static void configureDefaultImageLoader(Context context) {
        ImageLoaderConfiguration defaultConfiguration = new ImageLoaderConfiguration.Builder(context)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .build();
        ImageLoader.getInstance().init(defaultConfiguration);
    }

}