package edu.neu.ccs.wellness.storytelling.storyview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import android.widget.TextView;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import edu.neu.ccs.wellness.storytelling.R;
import edu.neu.ccs.wellness.storytelling.models.StoryPage;

/**
 * A Fragment to show a simple view of one artwork and one text of the Story.
 */
public class StoryCoverFragment extends Fragment {
    private static final String STORY_TEXT_FACE = "fonts/pangolin_regular.ttf";
    private static TextView storyTitle;
    private static ImageView storyCover;
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


    public StoryCoverFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), STORY_TEXT_FACE);
        View rootview = inflater.inflate(R.layout.fragment_story_cover, container, false);
        setContentText(rootview, getString(R.string.reflection_text));
        String text = getArguments().getString(StoryContentAdapter.KEY_TEXT);
        this.storyTitle = (TextView) rootview.findViewById(R.id.text);
        this.storyTitle.setTypeface(tf);
        this.storyTitle.setText(text);

        this.storyCover = (ImageView) rootview.findViewById(R.id.storyImage);

        String imageUrl = getArguments().getString(StoryContentAdapter.KEY_IMG_URL);
        configureDefaultImageLoader(getContext());
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(imageUrl, this.storyCover, options);

        setContentText(rootview, text);

        return rootview;
    }

    /***
     * Set View to show the Story's content
     * @param view The View in which the content will be displayed
     * @param text The Story content's text
     */
    private void setContentText(View view, String text) {
    }

    private static void configureDefaultImageLoader(Context context) {
        ImageLoaderConfiguration defaultConfiguration = new ImageLoaderConfiguration.Builder(context)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .build();
        ImageLoader.getInstance().init(defaultConfiguration);
    }
}


