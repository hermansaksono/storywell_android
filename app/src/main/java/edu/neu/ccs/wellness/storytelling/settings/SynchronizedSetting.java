package edu.neu.ccs.wellness.storytelling.settings;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hermansaksono on 1/23/19.
 */

@IgnoreExtraProperties
public class SynchronizedSetting {

    private static final String DEFAULT_CHALLENGE_ID = "";
    private static final long DEFAULT_LAST_SYNC_TIME = 1546300800; // i.e., Jan 1, 2019 0:00 AM GMT
    private static final int DEFAULT_REFLECTION_ITERATION = 1;

    /**
     * Constructors
     */
    public SynchronizedSetting(String currentChallengeId,
                               List<String> completedChallenges,
                               long caregiverLastSyncTime,
                               long childLastSyncTime) {
        this.currentChallengeId = currentChallengeId;
        this.completedChallenges = completedChallenges;
        this.caregiverLastSyncTime = caregiverLastSyncTime;
        this.childLastSyncTime = childLastSyncTime;
    }

    public SynchronizedSetting() {
        this.currentChallengeId = DEFAULT_CHALLENGE_ID;
        this.completedChallenges = new ArrayList<>();
        this.caregiverLastSyncTime = DEFAULT_LAST_SYNC_TIME;
        this.childLastSyncTime = DEFAULT_LAST_SYNC_TIME;
    }


    /**
     * The id of the currently running challenge. Null if there is no running challenge
     */
    private String currentChallengeId;

    public String getRunningChallengeId() {
        return currentChallengeId;
    }

    public void setCurrentChallengeId(String currentChallengeId) {
        this.currentChallengeId = currentChallengeId;
    }

    @Exclude
    public boolean isChallengeIdExists() {
        return !DEFAULT_CHALLENGE_ID.equals(this.currentChallengeId);
    }

    /**
     *
     */
    private List<String> completedChallenges;

    public List<String> getCompletedChallenges() {
        return this.completedChallenges;
    }

    public void setCompletedChallenges(List<String> completedChallenges) {
        this.completedChallenges = completedChallenges;
    }

    /**
     * The timestamp of the caregiver's last sync time (in GMT)
     */
    private long caregiverLastSyncTime;

    public long getCaregiverLastSyncTime() {
        return caregiverLastSyncTime;
    }

    public void setCaregiverLastSyncTime(long lastSyncTime) {
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

    /**
     * The iteration tells the number of times the user has responded to the reflection.
     */
    private int reflectionIteration = DEFAULT_REFLECTION_ITERATION;

    public int getReflectionIteration() {
        return reflectionIteration;
    }

    public void setReflectionIteration(int reflectionIteration) {
        this.reflectionIteration = reflectionIteration;
    }
}
