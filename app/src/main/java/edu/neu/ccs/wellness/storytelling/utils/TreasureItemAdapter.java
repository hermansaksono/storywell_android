package edu.neu.ccs.wellness.storytelling.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

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
        final ResponsePile responsePile = this.treasures.get(position);
        Context context = parent.getContext();
        View view = convertView;

        if (convertView == null) {
            view = getInflater(context)
                    .inflate(R.layout.item_treasure_generic, parent, false);
        }

        TextView textView = view.findViewById(R.id.treasureTitle);
        textView.setText(getItem(position).getTitle());
        setTextViewTypeface(textView, StoryViewActivity.STORY_TITLE_FACE, context);

        return view;
    }

    /* HELPER METHODS */
    private static LayoutInflater getInflater(Context context) {
        return (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    private static void setTextViewTypeface(TextView tv, int fontResId, Context context) {
        Typeface tf = ResourcesCompat.getFont(context, fontResId);
        tv.setTypeface(tf);
    }

}
