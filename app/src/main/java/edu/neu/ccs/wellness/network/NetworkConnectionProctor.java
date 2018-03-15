package edu.neu.ccs.wellness.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by RAJ on 3/15/2018.
 */

public class NetworkConnectionProctor  extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {

            if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)) {
                Toast.makeText(context, "Network lost", Toast.LENGTH_LONG).show();
            } else{
                Toast.makeText(context, "Network found", Toast.LENGTH_LONG).show();
            }
            }
    }
}
