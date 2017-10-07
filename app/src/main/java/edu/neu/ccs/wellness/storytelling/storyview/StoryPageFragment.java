package edu.neu.ccs.wellness.storytelling.storyview;

import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import edu.neu.ccs.wellness.storytelling.R;
import edu.neu.ccs.wellness.storytelling.StoryViewActivity;
import edu.neu.ccs.wellness.storytelling.models.WellnessRestServer;

/**
 * A Fragment to show a simple view of one artwork and one text of the Story.
 */
public class StoryPageFragment extends Fragment {

    private static String IMAGE_URL="";
    private static String TEXT="";


    private final DisplayImageOptions options = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.place_holder)
            .showImageForEmptyUri(R.drawable.hand)
            .showImageOnFail(R.drawable.big_problem)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .considerExifParams(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .build();


    public static StoryPageFragment newInstance(Bundle bundle){
        StoryPageFragment pageFragment = new StoryPageFragment();
        if (bundle != null) {
            Bundle args = new Bundle();
            args.putString("KEY_TEXT", bundle.getString("KEY_TEXT"));
            args.putString("KEY_IMG_URL", bundle.getString("KEY_IMG_URL"));
            pageFragment.setArguments(args);
            TEXT = bundle.getString("KEY_TEXT");
            IMAGE_URL = bundle.getString("KEY_IMG_URL");
        }

        return pageFragment;
    }


    // PUBLIC METHODS
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_story_view, container, false);
        ImageView imageView = (ImageView) view.findViewById(R.id.storyImage);

        setContentText(view, TEXT);

        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(IMAGE_URL, imageView, options);

        return view;
    }

    /***
     * Set View to show the Story's content
     * @param view The View in which the content will be displayed
     * @param text The Page's text contents
     */
    private void setContentText(View view, String text) {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(),
                StoryViewActivity.STORY_TEXT_FACE);
        TextView tv = (TextView) view.findViewById(R.id.storyText);
        tv.setTypeface(tf);
        tv.setText(text);
    }
}
