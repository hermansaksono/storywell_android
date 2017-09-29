package edu.neu.ccs.wellness.storytelling.utils;

import android.content.Context;

import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import edu.neu.ccs.wellness.storytelling.R;
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
    private static final int HEIGHT = 250;
    private static final int WIDTH = 250;

    public StoryCoverAdapter(Context c, List<StoryInterface> stories) {
        mContext = c;
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
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.booklayout_storylist, parent, false);
            gridViewImageHolder = new ViewHolder();
            ImageView imageView = (ImageView) view.findViewById(R.id.imageview_cover_art);
            ViewGroup.LayoutParams lp = imageView.getLayoutParams();
            lp.width = getDPI(mContext, WIDTH);
            lp.height = getDPI(mContext, HEIGHT);
            imageView.requestLayout();
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
            gridViewImageHolder.imageView = imageView;
            TextView textView = (TextView) view.findViewById(R.id.textview_book_name);
            textView.setText(story.getTitle());
            setTextViewTypeface(textView, STORYLIST_FONT);

            gridViewImageHolder.view = view;
            view.setTag(gridViewImageHolder);
        }
        else {
            gridViewImageHolder = (ViewHolder) view.getTag();
        }


        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(story.getCoverUrl(), gridViewImageHolder.imageView, options);

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

    private int getDPI(Context context, int size) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size,
                context.getResources().getDisplayMetrics());
    }
}