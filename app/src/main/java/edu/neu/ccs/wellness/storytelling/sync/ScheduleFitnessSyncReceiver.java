package edu.neu.ccs.wellness.storytelling.sync;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ScheduleFitnessSyncReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            FitnessSyncReceiver.scheduleFitnessSync(context, FitnessSyncReceiver.SYNC_INTERVAL);
        }
    }
}
