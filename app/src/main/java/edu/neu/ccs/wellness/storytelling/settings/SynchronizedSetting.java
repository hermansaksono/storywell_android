package edu.neu.ccs.wellness.storytelling.settings;

import java.util.List;

/**
 * Created by hermansaksono on 1/23/19.
 */

public class SynchronizedSetting {

    private static final long DEFAULT_LAST_SYNC_TIME = 1546300800; // i.e., Jan 1, 2019 0:00 AM GMT

    /**
     * Constructor
     */
    public SynchronizedSetting(String currentChallengeId,
                               long caregiverLastSyncTime,
                               long childLastSyncTime) {
        this.currentChallengeId = currentChallengeId;
        this.caregiverLastSyncTime = caregiverLastSyncTime;
        this.childLastSyncTime = childLastSyncTime;
    }

    public SynchronizedSetting() {
        this.currentChallengeId = null;
        this.caregiverLastSyncTime = DEFAULT_LAST_SYNC_TIME;
        this.childLastSyncTime = DEFAULT_LAST_SYNC_TIME;
    }


    /**
     * The id of the currently running challenge. Null if there is no running challenge
     */
    private String currentChallengeId;

    public String getCurrentChallengeId() {
        return currentChallengeId;
    }

    public void setCurrentChallengeId(String currentChallengeId) {
        this.currentChallengeId = currentChallengeId;
    }

    /**
     *
     */
    private List<String> completedChallenges;

    public List<String> getCompletedChallenges() {
        return completedChallenges;
    }

    public void setCompletedChallenges(List<String> completedChallenges) {
        this.completedChallenges = completedChallenges;
    }

    /**
     * The timestamp of the caregiver's last sync time (in GMT)
     */
    private long caregiverLastSyncTime;

    public long getLastSyncTime() {
        return caregiverLastSyncTime;
    }

    public void setLastSyncTime(long lastSyncTime) {
        this.caregiverLastSyncTime = lastSyncTime;
    }

    /**
     * The timestamp of the child's last sync time (in GMT)
     */
    private long childLastSyncTime;

    public long getChildLastSyncTime() {
        return childLastSyncTime;
    }

    public void setChildLastSyncTime(long lastSyncTime) {
        this.childLastSyncTime = lastSyncTime;
    }
}
