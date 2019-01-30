package edu.neu.ccs.wellness.storytelling.settings;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.neu.ccs.wellness.utils.date.HourMinute;

/**
 * Created by hermansaksono on 1/23/19.
 */

@IgnoreExtraProperties
public class SynchronizedSetting {

    private static final String DEFAULT_CHALLENGE_ID = "";
    private static final String[] DEFAULT_UNLOCKED_STORIES = {"0"};
    private static final long DEFAULT_LAST_SYNC_TIME = 1546300800; // i.e., Jan 1, 2019 0:00 AM GMT
    private static final int DEFAULT_REFLECTION_ITERATION = 1;

    /**
     * Constructors
     */
    public SynchronizedSetting(String currentChallengeId,
                               List<String> unlockedStoryPages,
                               long caregiverLastSyncTime,
                               long childLastSyncTime) {
        this.currentChallengeId = currentChallengeId;
        this.unlockedStoryPages = unlockedStoryPages;
        this.caregiverLastSyncTime = caregiverLastSyncTime;
        this.childLastSyncTime = childLastSyncTime;
    }

    public SynchronizedSetting() {
        this.currentChallengeId = DEFAULT_CHALLENGE_ID;
        this.unlockedStories = new ArrayList<>(Arrays.asList(DEFAULT_UNLOCKED_STORIES));
        this.unlockedStoryPages = new ArrayList<>();
        this.caregiverLastSyncTime = DEFAULT_LAST_SYNC_TIME;
        this.childLastSyncTime = DEFAULT_LAST_SYNC_TIME;
        this.challengeEndTime = new HourMinute(19, 30);
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
     * Tells the local time when a fitness challenge ended in the family's side. A challenge will
     * always end at midnight, but on the family a challenge can end a little bit early.
     * Default: 7.30 PM.
     */
    private HourMinute challengeEndTime;

    public HourMinute getChallengeEndTime() {
        return challengeEndTime;
    }

    public void setChallengeEndTime(HourMinute challengeEndTime) {
        this.challengeEndTime = challengeEndTime;
    }

    /**
     *
     */
    private List<String> unlockedStories;

    public List<String> getUnlockedStories() {
        return this.unlockedStories;
    }

    public void setUnlockedStories(List<String> unlockedStories) {
        this.unlockedStories = unlockedStories;
    }

    /**
     *
     */
    private List<String> unlockedStoryPages;

    public List<String> getUnlockedStoryPages() {
        return this.unlockedStoryPages;
    }

    public void setUnlockedStoryPages(List<String> unlockedStoryPages) {
        this.unlockedStoryPages = unlockedStoryPages;
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
