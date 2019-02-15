package edu.neu.ccs.wellness.storytelling.homeview;

import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import edu.neu.ccs.wellness.storytelling.R;

/**
 * Created by hermansaksono on 2/15/19.
 */

public class ChallengeCompletedDialog extends DialogFragment {
    /* FACTORY METHODS */
    public static AlertDialog newInstance(
            String storyTitle, String coverImageUri,
            Context context,
            final DialogInterface.OnClickListener onPositiveButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                context, R.style.AppTheme_Dialog);
        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(R.layout.dialog_challenge_completion, null);

        ImageView imageView = layout.findViewById(R.id.story_cover);
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(coverImageUri, imageView, getLoaderOptions());

        TextView textView = layout.findViewById(R.id.text_story_title);
        textView.setText(storyTitle);

        builder.setView(layout)
                .setPositiveButton(
                        R.string.adventure_dialog_completed_unlock,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                onPositiveButtonClickListener.onClick(dialog, id);
                                dialog.dismiss();
                            }
                        })
                .setNegativeButton(
                        R.string.adventure_dialog_completed_later,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });

        return builder.create();
    }

    private static DisplayImageOptions getLoaderOptions() {
        return new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.img_placeholder)
                .showImageForEmptyUri(R.drawable.img_failure)
                .showImageOnFail(R.drawable.img_failure)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();}
}
