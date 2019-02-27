package edu.neu.ccs.wellness.storytelling.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.neu.ccs.wellness.reflection.ResponsePile;
import edu.neu.ccs.wellness.storytelling.R;
import edu.neu.ccs.wellness.storytelling.StoryViewActivity;

/**
 * Created by hermansaksono on 1/17/19.
 */

public class TreasureItemAdapter extends BaseAdapter {

    private List<ResponsePile> treasures;

    public TreasureItemAdapter(Context context, List<ResponsePile> treasures) {
        this.treasures = treasures;
    }

    @Override
    public int getCount() {
        return this.treasures.size();
    }

    @Override
    public ResponsePile getItem(int position) {
        return this.treasures.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Context context = parent.getContext();
        View view = convertView;
        ImageView imageView;
        TextView titleTextView;
        TextView metaTextView;

        if (convertView == null) {
            view = getInflater(context)
                    .inflate(R.layout.item_treasure_generic, parent, false);
            imageView = view.findViewById(R.id.treasureIcon);
            titleTextView = view.findViewById(R.id.treasureTitle);
            metaTextView = view.findViewById(R.id.treasureMeta);

            ViewHolder treasureViewHolder = new ViewHolder(
                    view, imageView, titleTextView, metaTextView);
            view.setTag(treasureViewHolder);
        } else {
            ViewHolder treasureViewHolder = (ViewHolder) view.getTag();
            imageView = treasureViewHolder.imageView;
            titleTextView = treasureViewHolder.titleTextView;
            metaTextView = treasureViewHolder.metaTextView;
        }

        ResponsePile responsePile = getItem(position);
        titleTextView.setText(responsePile.getTitle());
        metaTextView.setText(getMeta(responsePile));
        //setTextViewTypeface(textView, StoryViewActivity.STORY_TITLE_FACE, context);

        return view;
    }

    /** VIEWHOLDER CLASS */
    static class ViewHolder {
        View view;
        ImageView imageView;
        TextView titleTextView;
        TextView metaTextView;

        public ViewHolder (View view,
                           ImageView imageView,
                           TextView titleTextView,
                           TextView metaTextView) {
            this.view = view;
            this.imageView = imageView;
            this.titleTextView = titleTextView;
            this.metaTextView = metaTextView;
        }
    }

    /* HELPER METHODS */
    private static LayoutInflater getInflater(Context context) {
        return (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    private static final String TREASURE_DATE_FORMAT = "EEE, MMM d, yyyy";
    private static final String EMPTY_DATE_STRING = "";

    private String getMeta(ResponsePile responsePile) {
        SimpleDateFormat sdf = new SimpleDateFormat(TREASURE_DATE_FORMAT);
        if (responsePile.getTimestamp() > 0) {
            Date date = new Date(responsePile.getTimestamp());
            return sdf.format(date);
        } else {
            return EMPTY_DATE_STRING;
        }
    }

    private static void setTextViewTypeface(TextView tv, int fontResId, Context context) {
        // Typeface tf = ResourcesCompat.getFont(context, fontResId);
        // tv.setTypeface(tf);
    }

}
