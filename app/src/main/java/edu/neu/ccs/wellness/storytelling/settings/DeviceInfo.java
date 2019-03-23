package edu.neu.ccs.wellness.storytelling.settings;

import java.util.Date;

import edu.neu.ccs.wellness.utils.WellnessDate;

/**
 * Created by hermansaksono on 3/7/19.
 */

public class DeviceInfo {

    private static final long DEFAULT_TIME = 1546300800; // i.e., Jan 1, 2019 0:00 AM GMT
    private static final int DEFAULT_BATTERY_LEVEL = 50;

    public DeviceInfo() {

    }

    private long lastSyncTime = DEFAULT_TIME;
    private String btAddress = "";
    private int btBatteryLevel = DEFAULT_BATTERY_LEVEL;

    public long getLastSyncTime() {
        return lastSyncTime;
    }

    public void setLastSyncTime(long lastSyncTime) {
        this.lastSyncTime = lastSyncTime;
    }

    public String getLastSyncTimeStringReadOnly () {
        return WellnessDate.getDateStringRFC(lastSyncTime);
    }

    public String getBtAddress() {
        return btAddress;
    }

    public void setBtAddress(String btAddress) {
        this.btAddress = btAddress;
    }

    public int getBtBatteryLevel() {
        return btBatteryLevel;
    }

    public void setBtBatteryLevel(int btBatteryLevel) {
        this.btBatteryLevel = btBatteryLevel;
    }
}
