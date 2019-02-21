package edu.neu.ccs.wellness.storytelling.utils;

import android.content.Context;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import edu.neu.ccs.wellness.story.Story;
import edu.neu.ccs.wellness.storytelling.R;
import edu.neu.ccs.wellness.story.interfaces.StoryInterface;
import edu.neu.ccs.wellness.story.interfaces.StoryType;
import edu.neu.ccs.wellness.storytelling.StoryViewActivity;
import edu.neu.ccs.wellness.storytelling.settings.SynchronizedSetting;

/**
 * Created by baharsheikhi on 6/22/17
 */

public class StoryCoverAdapter extends BaseAdapter {

    public static final String STATUS_DEFAULT = "DEFAULT";
    public static final String STATUS_UNREAD = "UNREAD";
    public static final String STATUS_LOCKED = "LOCKED";

    private Context context;
    private List<StoryInterface> stories;
    private SynchronizedSetting.StoryListInfo metadata;
    private final DisplayImageOptions options = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.img_placeholder)
            .showImageForEmptyUri(R.drawable.img_failure)
            .showImageOnFail(R.drawable.img_failure)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .considerExifParams(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .build();

    public StoryCoverAdapter(
            List<StoryInterface> stories, SynchronizedSetting.StoryListInfo storyListInfo,
            Context context) {
        this.context = context;
        this.stories = stories;
        this.metadata = storyListInfo;
    }

    public void setMetadata(SynchronizedSetting.StoryListInfo metadata) {
        this.metadata = metadata;
        notifyDataSetChanged();
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
            view = getInflater().inflate(R.layout.item_storybook_side, parent, false);
            ImageView imageView = view.findViewById(R.id.imageview_cover_art);
            gridViewImageHolder = new ViewHolder(view, imageView);
            view.setTag(gridViewImageHolder);
        }
        else {
            gridViewImageHolder = (ViewHolder) view.getTag();
        }

        view = getViewWithStatus(story, view);

        if (story.getStoryType() == StoryType.STORY) {
            ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.displayImage(story.getCoverUrl(), gridViewImageHolder.imageView, options);
        } else if (story.getStoryType() == StoryType.APP) {
            gridViewImageHolder.imageView.setImageResource(getDrawableResId(story.getCoverUrl()));
        }

        TextView textView = view.findViewById(R.id.textview_book_name);
        textView.setText(story.getTitle());
        setTextViewTypeface(textView, StoryViewActivity.STORY_TITLE_FACE);

        return view;
    }

    private View getViewWithStatus(StoryInterface story, View view) {
        /*
        if (metadata.getUnreadStories().contains(story.getId())) {
            view.findViewById(R.id.imageview_story_unlocked_icon).setVisibility(View.VISIBLE);
            return view;
        } else if (metadata.getUnlockedStories().contains(story.getId())) {
            return view;
        } else {
            view.findViewById(R.id.imageview_story_unlocked_icon).setVisibility(View.GONE);
            return view;
        }
        */
        String status = getStoryStatus(story);
        switch (status) {
            case STATUS_DEFAULT:
                view.findViewById(R.id.imageview_story_status_unread).setVisibility(View.GONE);
                //view.findViewById(R.id.imageview_story_status_locked).setVisibility(View.GONE);
                break;
            case STATUS_UNREAD:
                view.findViewById(R.id.imageview_story_status_unread).setVisibility(View.VISIBLE);
                //view.findViewById(R.id.imageview_story_status_locked).setVisibility(View.GONE);
                break;
            case STATUS_LOCKED:
                view.findViewById(R.id.imageview_story_status_unread).setVisibility(View.GONE);
                //view.findViewById(R.id.imageview_story_status_locked).setVisibility(View.VISIBLE);
                break;
        }
        return view;
    }

    private String getStoryStatus(StoryInterface story) {
        if (story.getStoryType() == StoryType.APP) {
            return STATUS_DEFAULT;
        }
        if (metadata.getUnreadStories().contains(story.getId())) {
            return STATUS_UNREAD;
        }
        if (metadata.getUnlockedStories().contains(story.getId())) {
            return STATUS_DEFAULT;
        } else {
            return STATUS_LOCKED;
        }
    }

    /**
     * Get the position of the Story in the grid given the {@link StoryInterface}'s id. If no story
     * found then return -1;
     * @param storyId
     * @return
     */
    public int getStoryPosition(String storyId) {
        int positionId = 0;
        for (StoryInterface story : stories) {
            if (story.getId().equals(storyId)) {
                return positionId;
            }
            positionId += 1;
        }
        return -1;
    }

    /** VIEWHOLDER CLASS */
    static class ViewHolder {
        View view;
        ImageView imageView;

        public ViewHolder (View view, ImageView imageView) {
            this.view = view;
            this.imageView = imageView;
        }
    }

    // PRIVATE METHODS
    private void setTextViewTypeface(TextView tv, int fontResId) {
        // Typeface tf = Typeface.createFromAsset(context.getAssets(), fontAsset);
        // Typeface tf = ResourcesCompat.getFont(this.context, fontResId);
        // tv.setTypeface(tf);
    }

    private LayoutInflater getInflater() {
        return (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    private int getDrawableResId(String resName) {
        return context.getResources().getIdentifier(resName, "drawable", context.getPackageName());
    }
}