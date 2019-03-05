package edu.neu.ccs.wellness.storytelling.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import edu.neu.ccs.wellness.reflection.TreasureItem;
import edu.neu.ccs.wellness.reflection.TreasureItemType;
import edu.neu.ccs.wellness.storytelling.R;

/**
 * Created by hermansaksono on 1/17/19.
 */

public class TreasureItemAdapter extends BaseAdapter {

    private List<TreasureItem> treasures;

    public TreasureItemAdapter(Context context, List<TreasureItem> treasures) {
        this.treasures = treasures;
    }

    @Override
    public int getCount() {
        return this.treasures.size();
    }

    @Override
    public TreasureItem getItem(int position) {
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

        TreasureItem treasureItem = getItem(position);
        titleTextView.setText(treasureItem.getTitle());
        metaTextView.setText(getMeta(treasureItem));

        imageView.setImageResource(getImageResource(getItem(position).getType()));
        //setTextViewTypeface(textView, StoryViewActivity.STORY_TITLE_FACE, context);

        return view;
    }

    private int getImageResource(int type) {
        switch (type) {
            case TreasureItemType.STORY_REFLECTION:
                return R.drawable.ic_microphone_48;
            case TreasureItemType.CALMING_PROMPT:
                return R.drawable.art_roulette_baloon_answer;
            default:
                return R.drawable.ic_gift_48;
        }
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

    private String getMeta(TreasureItem treasureItem) {
        SimpleDateFormat sdf = new SimpleDateFormat(TREASURE_DATE_FORMAT);
        if (treasureItem.getLastUpdateTimestamp() > 0) {
            Date date = new Date(treasureItem.getLastUpdateTimestamp());
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
