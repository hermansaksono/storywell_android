package edu.neu.ccs.wellness.storytelling.settings;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import edu.neu.ccs.wellness.people.Group;
import edu.neu.ccs.wellness.setting.SyncableSetting;
import edu.neu.ccs.wellness.storytelling.resolutionview.BalloonRouletteState;
import edu.neu.ccs.wellness.storytelling.resolutionview.ResolutionStatus;
import edu.neu.ccs.wellness.utils.date.HourMinute;

/**
 * Created by hermansaksono on 1/23/19.
 */

@IgnoreExtraProperties
public class SynchronizedSetting implements SyncableSetting {

    public static final String KEY_FCM_TOKEN = "fcmToken";
    public static final String KEY_IS_DEMO_MODE = "isDemoMode";
    public static final String KEY_IS_REGULAR_REMINDER_SET = "isRegularReminderSet";
    public static final String KEY_IS_FIRST_RUN_COMPLETED = "isFirstRunCompleted";
    public static final String KEY_IS_STORYLIST_NEEDS_REFRESH = "isStoryListNeedsRefresh";
    public static final String KEY_IS_CHALLENGE_NEEDS_REFRESH = "isChallengeInfoNeedsRefresh";
    public static final String KEY_IS_GROUP_NEEDS_REFRESH = "isGroupInfoNeedsRefresh";

    private static final String DEFAULT_CHALLENGE_ID = "";
    private static final String[] DEFAULT_UNLOCKED_STORIES = {"0"};
    private static final String[] DEFAULT_UNREAD_STORIES = {"0"};
    private static final long DEFAULT_TIME = 1546300800; // i.e., Jan 1, 2019 0:00 AM GMT
    private static final int DEFAULT_REFLECTION_ITERATION = 1;
    private static final long DEFAULT_REFLECTION_MIN_DATE = 0L;

    /**
     * Constructor
     */
    public SynchronizedSetting() {
        this.challengeEndTime = new HourMinute(19, 30);
        this.appStartDate = getTodaysDate();
    }

    /**
     * The current login's information.
     */
    public static class SmartDeviceInfo {
        /**
         * Tells the maker of the login's phone.
         */
        private String deviceName = "";

        public String getDeviceName() {
            return deviceName;
        }

        public void setDeviceName(String deviceName) {
            this.deviceName = deviceName;
        }


        /**
         * Tells the Android release code
         */
        private String androidRelease = "";

        public String getAndroidRelease() {
            return androidRelease;
        }

        public void setAndroidRelease(String androidRelease) {
            this.androidRelease = androidRelease;
        }

        /**
         * Tells the Android version number
         */
        private int androidVersion = 0;

        public int getAndroidVersion() {
            return androidVersion;
        }

        public void setAndroidVersion(int androidVersion) {
            this.androidVersion = androidVersion;
        }

        /**
         * Tells which version the app is.
         */
        private int appVersionCode = 0;

        public int getAppVersionCode() {
            return appVersionCode;
        }

        public void setAppVersionCode(int appVersionCode) {
            this.appVersionCode = appVersionCode;
        }

        /**
         * Tells which version name the app is.
         */
        private String appVersionName = "0";

        public String getAppVersionName() {
            return appVersionName;
        }

        public void setAppVersionName(String appVersionName) {
            this.appVersionName = appVersionName;
        }
    }

    private SmartDeviceInfo deviceInfo = new SmartDeviceInfo();

    public SmartDeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(SmartDeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    /**
     * The current user's {@link Group} data.
     */
    private Group group;

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
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
     * The iteration tells the number of times the user has responded to the reflection.
     */
    private int reflectionIteration = DEFAULT_REFLECTION_ITERATION;

    public int getReflectionIteration() {
        return reflectionIteration;
    }

    public void setReflectionIteration(int reflectionIteration) {
        this.reflectionIteration = reflectionIteration;
    }

    /**
     * The iteration epochs tells the times when a new iteration was chosen.
     */
    private List<Long> reflectionIterationEpochs;

    public List<Long> getReflectionIterationEpochs() {
        if (this.reflectionIterationEpochs == null) {
            this.reflectionIterationEpochs = new ArrayList<>();
            this.reflectionIterationEpochs.add(DEFAULT_REFLECTION_MIN_DATE);
        }
        return this.reflectionIterationEpochs;
    }

    public void setReflectionIterationEpochs(List<Long> epochsList) {
        this.reflectionIterationEpochs = epochsList;
    }

    /**
     * The start date of when the user starts using the app
     */
    private long appStartDate;

    public long getAppStartDate() {
        return this.appStartDate;
    }

    public void setAppStartDate(long timestamp) {
        this.appStartDate = Math.max(timestamp, DEFAULT_TIME);
    }

    public void resetAppStartDate() {
        this.appStartDate = getTodaysDate();
    }

    private static long getTodaysDate() {
        Calendar calendar = Calendar.getInstance(Locale.US);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        return calendar.getTimeInMillis();
    }

    /**
     * Whether a regular reminder has been set
     */
    private boolean isRegularReminderSet = false;

    @PropertyName(KEY_IS_REGULAR_REMINDER_SET)
    public boolean isRegularReminderSet() {
        return this.isRegularReminderSet;
    }

    public void setRegularReminderSet(boolean regularReminderSet) {
        this.isRegularReminderSet = regularReminderSet;
    }

    /**
     * First run
     */
    private boolean isFirstRunCompleted = false;

    @PropertyName(KEY_IS_FIRST_RUN_COMPLETED)
    public boolean isFirstRunCompleted() {
        return isFirstRunCompleted;
    }

    public void setIsFirstRunCompleted(boolean firstRunCompleted) {
        this.isFirstRunCompleted = firstRunCompleted;
    }

    /**
     * Firebase Cloud Messaging (FCM) registration token
     */
    private String fcmToken;

    @PropertyName(KEY_FCM_TOKEN)
    public String getFcmToken() {
        return this.fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    /**
     * Whether the Group info need to be refreshed
     */
    private boolean isGroupInfoNeedsRefresh = true;

    @PropertyName(KEY_IS_GROUP_NEEDS_REFRESH)
    public boolean isGroupInfoNeedsRefresh() {
        return isGroupInfoNeedsRefresh;
    }

    public void setIsGroupInfoNeedsRefresh(boolean isGroupInfoNeedsRefresh) {
        this.isGroupInfoNeedsRefresh = isGroupInfoNeedsRefresh;
    }

    /**
     * Whether the Challenge need to be refreshed
     */
    private boolean isChallengeInfoNeedsRefresh = true;

    @PropertyName(KEY_IS_CHALLENGE_NEEDS_REFRESH)
    public boolean isChallengeInfoNeedsRefresh() {
        return isChallengeInfoNeedsRefresh;
    }

    public void setIsChallengeInfoNeedsRefresh(boolean isChallengeInfoNeedsRefresh) {
        this.isChallengeInfoNeedsRefresh = isChallengeInfoNeedsRefresh;
    }

    /**
     * Whether the storylist need to be refreshed
     */
    private boolean isStoryListNeedsRefresh = true;

    @PropertyName(KEY_IS_STORYLIST_NEEDS_REFRESH)
    public boolean isStoryListNeedsRefresh() {
        return isStoryListNeedsRefresh;
    }

    public void setIsStoryListNeedsRefresh(boolean isStoryListNeedsRefresh) {
        this.isStoryListNeedsRefresh = isStoryListNeedsRefresh;
    }

    /**
     * Whether the user is in the Demo mode
     */
    private boolean isDemoMode = false;

    @PropertyName(KEY_IS_DEMO_MODE)
    public boolean isDemoMode() {
        return isDemoMode;
    }

    public void setDemoMode(boolean demoMode) {
        isDemoMode = demoMode;
    }

    /**
     * Determines which hero is being shown in the game. 0 = DEFAULT_GIRL, 1 = DEFAULT_BOY
     */
    private int heroCharacterId = 0;

    public int getHeroCharacterId() {
        return heroCharacterId;
    }

    public void setHeroCharacterId(int heroCharacterId) {
        this.heroCharacterId = heroCharacterId;
    }

    /**
     * Determines the current challenge
     */
    public static class ChallengeInfo {

        public ChallengeInfo() {

        }

        /**
         * The id of the currently running challenge. Null if there is no running challenge
         */
        private String currentChallengeId = DEFAULT_CHALLENGE_ID;

        public String getCurrentChallengeId() {
            return currentChallengeId;
        }

        public void setCurrentChallengeId(String currentChallengeId) {
            this.currentChallengeId = currentChallengeId;
        }

        @Exclude
        public boolean isChallengeIdExists() {
            return !DEFAULT_CHALLENGE_ID.equals(this.currentChallengeId);
        }
    }

    private ChallengeInfo challengeInfo = new ChallengeInfo();

    public ChallengeInfo getChallengeInfo() {
        return challengeInfo;
    }

    public void setCChallengeInfo(ChallengeInfo challengeInfo) {
        this.challengeInfo = challengeInfo;
    }

    /**
     *
     */
    public static class StoryChallengeInfo {

        public StoryChallengeInfo() {

        }

        private boolean isSet = false;

        public boolean getIsSet() {
            return isSet;
        }

        public void setIsSet(boolean set) {
            isSet = set;
        }

        /**
         * The story and content related to the challenge.
         */
        private String storyId;

        public String getStoryId() {
            return storyId;
        }

        public void setStoryId(String storyId) {
            this.storyId = storyId;
        }


        private String storyTitle;

        public String getStoryTitle() {
            return storyTitle;
        }

        public void setStoryTitle(String storyTitle) {
            this.storyTitle = storyTitle;
        }


        private String storyCoverImageUri;

        public String getStoryCoverImageUri() {
            return storyCoverImageUri;
        }

        public void setStoryCoverImageUri(String storyCoverImageUri) {
            this.storyCoverImageUri = storyCoverImageUri;
        }

        private String chapterIdToBeUnlocked;

        public String getChapterIdToBeUnlocked() {
            return chapterIdToBeUnlocked;
        }

        public void setChapterIdToBeUnlocked(String chapterIdToBeUnlocked) {
            this.chapterIdToBeUnlocked = chapterIdToBeUnlocked;
        }
    }

    private StoryChallengeInfo storyChallengeInfo = new StoryChallengeInfo();

    public StoryChallengeInfo getStoryChallengeInfo() {
        return storyChallengeInfo;
    }

    public void setStoryChallengeInfo(StoryChallengeInfo storyChallengeInfo) {
        this.storyChallengeInfo = storyChallengeInfo;
    }

    @Exclude
    public void resetStoryChallengeInfo() {
        this.storyChallengeInfo = new StoryChallengeInfo();
    }

    /**
     * StoryListInfo
     */
    public static class StoryListInfo {

        public StoryListInfo() {

        }

        private String highlightedStoryId = "";

        public String getHighlightedStoryId() {
            return highlightedStoryId;
        }

        public void setHighlightedStoryId(String highlightedStoryId) {
            this.highlightedStoryId = highlightedStoryId;
        }


        private List<String> unreadStories = new ArrayList<>(
                Arrays.asList(DEFAULT_UNREAD_STORIES));

        public List<String> getUnreadStories() {
            return unreadStories;
        }

        public void setUnreadStories(List<String> unreadStories) {
            this.unreadStories = unreadStories;
        }


        private List<String> unlockedStories = new ArrayList<>(
                Arrays.asList(DEFAULT_UNLOCKED_STORIES));

        public List<String> getUnlockedStories() {
            return unlockedStories;
        }

        public void setUnlockedStories(List<String> unlockedStories) {
            this.unlockedStories = unlockedStories;
        }

        /**
         * List of a all story pages that has been unlocked
         */
        private List<String> unlockedStoryPages = new ArrayList<>();

        public List<String> getUnlockedStoryPages() {
            return this.unlockedStoryPages;
        }

        public void setUnlockedStoryPages(List<String> unlockedStoryPages) {
            this.unlockedStoryPages = unlockedStoryPages;
        }


        /**
         * Map of story ids and their current page ids.
         */
        private Map<String, Integer> currentStoryPageId = new HashMap<>();

        public Map<String, Integer> getCurrentStoryPageId() {
            return currentStoryPageId;
        }

        public void setCurrentStoryPageId(Map<String, Integer> currentStoryPageId) {
            this.currentStoryPageId = currentStoryPageId;
        }
    }

    private StoryListInfo storyListInfo = new StoryListInfo();

    public StoryListInfo getStoryListInfo() {
        return storyListInfo;
    }

    public void setStoryListInfo(StoryListInfo storyListInfo) {
        this.storyListInfo = storyListInfo;
    }

    /**
     * Resolution info
     */
    public static class ResolutionInfo {

        public ResolutionInfo() {

        }

        private int resolutionStatus = ResolutionStatus.UNSTARTED;

        public int getResolutionStatus() {
            return resolutionStatus;
        }

        public void setResolutionStatus(int resolutionStatus) {
            this.resolutionStatus = resolutionStatus;
        }


        private BalloonRouletteState gameState;

        public BalloonRouletteState getGameState() {
            return gameState;
        }

        public void setGameState(BalloonRouletteState gameState) {
            this.gameState = gameState;
        }


        private String lastCalmingPromptSetId = "1";

        public String getLastCalmingPromptSetId() {
            return lastCalmingPromptSetId;
        }

        public void setLastCalmingPromptSetId(String lastCalmingPromptSetId) {
            this.lastCalmingPromptSetId = lastCalmingPromptSetId;
        }


        private int lastCalmingPromptId = 0;

        public int getLastCalmingPromptId() {
            return lastCalmingPromptId;
        }

        public void setLastCalmingPromptId(int lastCalmingPromptId) {
            this.lastCalmingPromptId = lastCalmingPromptId;
        }
    }

    private ResolutionInfo resolutionInfo = new ResolutionInfo();

    public ResolutionInfo getResolutionInfo() {
        return resolutionInfo;
    }

    public void setResolutionInfo(ResolutionInfo resolutionInfo) {
        this.resolutionInfo = resolutionInfo;
    }

    /**
     * Provide information about synchronization.
     */
    public static class FitnessSyncInfo {

        public FitnessSyncInfo() {

        }

        /**
         * Information about caregiver's bluetooh device.
         */
        private DeviceInfo caregiverDeviceInfo = new DeviceInfo();

        public DeviceInfo getCaregiverDeviceInfo() {
            return caregiverDeviceInfo;
        }

        public void setCaregiverDeviceInfo(DeviceInfo caregiverDeviceInfo) {
            this.caregiverDeviceInfo = caregiverDeviceInfo;
        }

        /**
         * Caregiver's biographical information
         */
        private UserBioInfo caregiverBio = new UserBioInfo();

        public UserBioInfo getCaregiverBio() {
            return caregiverBio;
        }

        public void setCaregiverBio(UserBioInfo caregiverBio) {
            this.caregiverBio = caregiverBio;
        }

        /**
         * Information about the child's bluetooh device.
         */
        private DeviceInfo childDeviceInfo = new DeviceInfo();

        public DeviceInfo getChildDeviceInfo() {
            return childDeviceInfo;
        }

        public void setChildDeviceInfo(DeviceInfo childDeviceInfo) {
            this.childDeviceInfo = childDeviceInfo;
        }

        /**
         * Child's biographical information
         */
        private UserBioInfo childBio = new UserBioInfo();

        public UserBioInfo getChildBio() {
            return childBio;
        }

        public void setChildBio(UserBioInfo childBio) {
            this.childBio = childBio;
        }
    }

    private FitnessSyncInfo fitnessSyncInfo = new FitnessSyncInfo();

    public FitnessSyncInfo getFitnessSyncInfo() {
        return fitnessSyncInfo;
    }


}
