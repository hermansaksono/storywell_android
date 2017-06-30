package edu.neu.ccs.wellness.storytelling.storyview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.support.v4.app.Fragment;
import android.os.Bundle;
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
public class StoryPageFragment extends Fragment {
    private static final String STORY_TEXT_FACE = "fonts/pangolin_regular.ttf";

    private final DisplayImageOptions options = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.place_holder)
            .showImageForEmptyUri(R.drawable.hand)
            .showImageOnFail(R.drawable.big_problem)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .considerExifParams(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .build();

    // CONSTRUCTORS
    /**
     * Constructor
     * @param page
     * @return
     */
    public static StoryPageFragment create(StoryPage page) {
        StoryPageFragment fragment = new StoryPageFragment();

        Bundle args = new Bundle();
        args.putString(StoryContentAdapter.KEY_TEXT, page.getText());
        fragment.setArguments(args);

        return fragment;
    }

    public static StoryPageFragment create(String text) {
        StoryPageFragment fragment = new StoryPageFragment();

        Bundle args = new Bundle();
        args.putString(StoryContentAdapter.KEY_TEXT, text);
        fragment.setArguments(args);

        return fragment;
    }

    // PUBLIC METHODS
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_story_view, container, false);
        ImageView imageView = (ImageView) view.findViewById(R.id.storyImage);

        String imageUrl = getArguments().getString(StoryContentAdapter.KEY_IMG_URL);
        String text = getArguments().getString(StoryContentAdapter.KEY_TEXT);
        setContentText(view, text);


        configureDefaultImageLoader(getContext());
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(imageUrl, imageView, options);

        return view;
    }

    /***
     * Set View to show the Story's content
     * @param view The View in which the content will be displayed
     * @param text The Story content's text
     */
    private void setContentText(View view, String text) {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), STORY_TEXT_FACE);
        TextView tv = (TextView) view.findViewById(R.id.storyText);
        tv.setTypeface(tf);
        tv.setText(text);
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
