package edu.neu.ccs.wellness.storytelling.storyview;

import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import edu.neu.ccs.wellness.storytelling.R;
import edu.neu.ccs.wellness.storytelling.StoryViewActivity;
import edu.neu.ccs.wellness.storytelling.models.Story;

/**
 * A Fragment to show a simple view of one artwork and one text of the Story.
 */
public class StoryCoverFragment extends Fragment {
    private final DisplayImageOptions options = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.place_holder)
            .showImageForEmptyUri(R.drawable.hand)
            .showImageOnFail(R.drawable.big_problem)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .considerExifParams(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .build();

    public StoryCoverFragment() {}

    public static StoryCoverFragment newInstance(Bundle bundle){
        return new StoryCoverFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_story_cover, container, false);
        ImageView imageView = (ImageView) view.findViewById(R.id.storyImage);

        String text = getArguments().getString(StoryContentAdapter.KEY_TEXT);
        setContentText(view, text);

        String imageUrl = getArguments().getString(StoryContentAdapter.KEY_IMG_URL);
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(imageUrl, imageView, options);

        return view;
    }

    /***
     * Set View to show the Story's content
     * @param view The View in which the content will be displayed
     * @param text The Storybook's title
     */
    private void setContentText(View view, String text) {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(),
                StoryViewActivity.STORY_TEXT_FACE);
        TextView tv = (TextView) view.findViewById(R.id.storyText);
        tv.setTypeface(tf);
        tv.setText(text);
    }
}


