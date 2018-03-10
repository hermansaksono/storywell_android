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

import edu.neu.ccs.wellness.storytelling.R;

public class MonitoringDetailFragment extends DialogFragment {

    /* CONSTRUCTOR */
    public static MonitoringDetailFragment newInstance() {
        return new MonitoringDetailFragment();
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

        builder.setView(layout)
                .setPositiveButton(R.string.monitoring_detail_success_pos, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                /*
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });*/

        return builder.create();
    }
}
