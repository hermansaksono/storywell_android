package edu.neu.ccs.wellness.storytelling.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import edu.neu.ccs.wellness.storytelling.notifications.RegularReminderReceiver;

/**
 * Created by hermansaksono on 2/5/19.
 */

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            // TODO this may crash if the user logged out from Storywell
            RegularReminderReceiver.scheduleRegularReminders(context);
        }
    }

}
