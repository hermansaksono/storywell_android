package edu.neu.ccs.wellness.storytelling.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import edu.neu.ccs.wellness.storytelling.Storywell;
import edu.neu.ccs.wellness.storytelling.notifications.BatteryReminderReceiver;
import edu.neu.ccs.wellness.storytelling.notifications.RegularReminderReceiver;
import edu.neu.ccs.wellness.storytelling.settings.SynchronizedSetting;
import edu.neu.ccs.wellness.storytelling.settings.SynchronizedSettingRepository;

/**
 * Created by hermansaksono on 2/5/19.
 */

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("SWELL", "Staring BootReceiver");
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            scheduleRegularReminders(context);
        }
    }

    private static void scheduleRegularReminders(Context context) {
        Storywell storywell = new Storywell(context);
        if (storywell.userHasLoggedIn()) {
            RegularReminderReceiver.scheduleRegularReminders(context);
            BatteryReminderReceiver.scheduleBatteryReminders(context);

            SynchronizedSetting setting = storywell.getSynchronizedSetting();
            setting.setRegularReminderSet(true);
            SynchronizedSettingRepository.saveLocalAndRemoteInstance(setting, context);
        }
    }

}
