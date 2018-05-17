package edu.neu.ccs.wellness.storywell.monitoringview;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import edu.neu.ccs.wellness.storytelling.R;
import edu.neu.ccs.wellness.utils.WellnessDate;

public class MonitoringDetailFragment extends DialogFragment {

    private int dayIndex;

    /* CONSTRUCTOR */
    public static MonitoringDetailFragment newInstance(int dayIndex) {
        MonitoringDetailFragment fragment = new MonitoringDetailFragment();
        fragment.dayIndex = dayIndex;
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // retrieve display dimensions
        Rect displayRectangle = new Rect();
        Window window = getActivity().getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_monitoring_detail, null);
        layout.setMinimumWidth((int) (displayRectangle.width() * 0.9f));
        layout.setMinimumHeight((int) (displayRectangle.height() * 0.9f));

        String text = String.format(getString(R.string.monitoring_detail_success_text),
                WellnessDate.getDayOfWeek(this.dayIndex));
        TextView textView = layout.findViewById(R.id.text);
        textView.setText(text);

        builder.setView(layout)
                .setPositiveButton(R.string.monitoring_detail_success_pos, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        return builder.create();
    }
}
