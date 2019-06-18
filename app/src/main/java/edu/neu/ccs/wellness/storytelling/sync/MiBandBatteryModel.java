package edu.neu.ccs.wellness.storytelling.sync;

import android.content.Context;

import edu.neu.ccs.wellness.storytelling.Storywell;
import edu.neu.ccs.wellness.storytelling.settings.SynchronizedSetting;

public class MiBandBatteryModel {
    public static final int MIN_BATTERY_LEVEL = 20;

    private Storywell storywell;

    public MiBandBatteryModel(Context context) {
        this.storywell = new Storywell(context);
    }

    public String getCaregiverName() {
        return this.storywell.getCaregiver().getName();
    }

    public String getChildName() {
        return this.storywell.getChild().getName();
    }

    public boolean isCaregiverBatteryLevelLow() {
        SynchronizedSetting.FitnessSyncInfo fitnessSyncInfo = storywell
                .getSynchronizedSetting().getFitnessSyncInfo();
        return fitnessSyncInfo.getCaregiverDeviceInfo().getBtBatteryLevel() <= MIN_BATTERY_LEVEL;
    }

    public boolean isChildBatteryLevelLow() {
        SynchronizedSetting.FitnessSyncInfo fitnessSyncInfo = storywell
                .getSynchronizedSetting().getFitnessSyncInfo();
        return fitnessSyncInfo.getChildDeviceInfo().getBtBatteryLevel() <= MIN_BATTERY_LEVEL;
    }
}
