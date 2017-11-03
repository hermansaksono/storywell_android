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
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import edu.neu.ccs.wellness.storytelling.R;
import edu.neu.ccs.wellness.storytelling.StoryViewActivity;

import static edu.neu.ccs.wellness.utils.StreamReflectionsFirebase.reflectionsUrlHashMap;


/**
 * A Fragment to show a simple view of one artwork and one text of the Story.
 */
public class StoryPageFragment extends Fragment {


    private final DisplayImageOptions options = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.img_placeholder)
            .showImageForEmptyUri(R.drawable.img_failure)
            .showImageOnFail(R.drawable.img_failure)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .considerExifParams(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .build();


    // PUBLIC METHODS
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_story_view, container, false);
        ImageView imageView = (ImageView) view.findViewById(R.id.storyImage);
        ImageLoader imageLoader = ImageLoader.getInstance();


        try {
            setContentText(view, getArguments().getString("KEY_TEXT"));
            imageLoader.displayImage(getArguments().getString("KEY_IMG_URL"), imageView, options);
        } catch (Exception e) {
            e.printStackTrace();
        }

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
