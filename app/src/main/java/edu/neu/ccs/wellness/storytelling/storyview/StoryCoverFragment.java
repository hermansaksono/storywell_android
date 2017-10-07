package edu.neu.ccs.wellness.storytelling.storyview;

import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import edu.neu.ccs.wellness.storytelling.R;
import edu.neu.ccs.wellness.storytelling.StoryViewActivity;
import edu.neu.ccs.wellness.storytelling.models.Story;

/**
 * A Fragment to show a simple view of one artwork and one text of the Story.
 */
public class StoryCoverFragment extends Fragment {

    /**
     * */
    private static String KEY_TEXT = "";
    private static String KEY_IMG_URL = "";


    private final DisplayImageOptions options = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.place_holder)
            .showImageForEmptyUri(R.drawable.hand)
            .showImageOnFail(R.drawable.big_problem)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .considerExifParams(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .build();

    public StoryCoverFragment() {
    }


    public static StoryCoverFragment newInstance(Bundle bundle) {
        StoryCoverFragment coverFragment = new StoryCoverFragment();
        if (bundle != null) {
            Bundle b = new Bundle();
            //Pass the Arguments to be initialized in onCreate of the FRAGMENT
            b.putString("KEY_TEXT", bundle.getString("KEY_TEXT"));
            b.putString("KEY_IMG_URL", bundle.getString("KEY_IMG_URL"));
            coverFragment.setArguments(b);
        }
        return coverFragment;
    }


    /**The system calls this when creating the fragment.
     *  Within your implementation, you should initialize essential components of the fragment
     *  that you want to retain when the fragment is paused or stopped,
     *  then resumed.*/
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            KEY_TEXT = savedInstanceState.getString("KEY_TEXT");
            KEY_IMG_URL = savedInstanceState.getString("KEY_IMG_URL");
        }
    }


    /**
     * The system calls onCreateView when it's time for the fragment to draw its user interface
     * for the first time.
     * So the view gets inflated which is considered one of the most heavy tasks in Android
     * Do all essential initializations in onCreate of Fragments above
     * */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_story_cover, container, false);
        ImageView imageView = (ImageView) view.findViewById(R.id.storyImage);
        ImageLoader imageLoader = ImageLoader.getInstance();

        // If the values don't reach due to some error
        // Do it in a try-catch block so that app doesn't crash
        try {
            setContentText(view, KEY_TEXT);
            imageLoader.displayImage(KEY_IMG_URL, imageView, options);
        } catch (Exception e) {
            e.printStackTrace();
        }
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