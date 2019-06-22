package edu.neu.ccs.wellness.storytelling;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import edu.neu.ccs.wellness.fitness.WellnessFitnessRepo;
import edu.neu.ccs.wellness.fitness.challenges.ChallengeManager;
import edu.neu.ccs.wellness.fitness.interfaces.ChallengeManagerInterface;
import edu.neu.ccs.wellness.fitness.interfaces.FitnessRepositoryInterface;
import edu.neu.ccs.wellness.people.Group;
import edu.neu.ccs.wellness.people.Person;
import edu.neu.ccs.wellness.people.PersonDoesNotExistException;
import edu.neu.ccs.wellness.server.FirebaseToken;
import edu.neu.ccs.wellness.server.FirebaseTokenRepository;
import edu.neu.ccs.wellness.server.OAuth2Exception;
import edu.neu.ccs.wellness.server.WellnessRestServer;
import edu.neu.ccs.wellness.server.WellnessUser;
import edu.neu.ccs.wellness.story.StoryManager;
import edu.neu.ccs.wellness.story.interfaces.StoryInterface;
import edu.neu.ccs.wellness.storytelling.settings.SynchronizedSetting;
import edu.neu.ccs.wellness.storytelling.settings.SynchronizedSetting.StoryListInfo;
import edu.neu.ccs.wellness.storytelling.settings.SynchronizedSettingRepository;
import edu.neu.ccs.wellness.utils.FirebaseUserManager;
import edu.neu.ccs.wellness.utils.WellnessIO;

/**
 * Created by hermansaksono on 10/24/17.
 */

public class Storywell {

    public static final String SERVER_URL = "https://wellness-test.ccs.neu.edu/storytelling_dev/";
    public static final String API_PATH = "api/";
    public static final String OAUTH_TOKEN_PATH = "oauth/token/";
    public static final String KEY_USER_DEF = "storywell_user";
    public static final int DEFAULT_PORT = 0;
    public static final boolean DEFAULT_IS_FIRST_RUN_COMPLETED = false;
    private static final String KEY_REFLECTION_ITERATION = "reflection_iteration";
    private static final int DEFAULT_KEY_REFLECTION_ITERATION = 1;

    private static final String clientId = BuildConfig.clientId;
    private static final String clientSecret = BuildConfig.clientSecret;

    private Context context;
    private SharedPreferences sharedPrefs;
    private WellnessUser user;
    private WellnessRestServer server;
    private StoryManager storyManager;
    private ChallengeManagerInterface challengeManager;
    private FitnessRepositoryInterface fitnessManager;

    /***
     * Constructor
     * @param context Application's context
     */
    public Storywell(Context context) {
        this.context = context.getApplicationContext();
    }


    /***
     * Check whether this is the app's first run
     * @return True if this is the first run. Otherwise return false.
     */
    public boolean isFirstRunCompleted() {
        //return this.getSharedPrefs().getBoolean(KEY_IS_FIRST_RUN_COMPLETED, DEFAULT_IS_FIRST_RUN_COMPLETED);
        return getSynchronizedSetting().isFirstRunCompleted();
    }

    /**
     * Save the status of  the app first run
     * @param isFirstRunCompleted True if the app has completed the first run. Otherwise give false.
     */
    public void setIsFirstRunCompleted(boolean isFirstRunCompleted) {

        SynchronizedSetting setting = this.getSynchronizedSetting();
        setting.setIsFirstRunCompleted(isFirstRunCompleted);
        SynchronizedSettingRepository.saveLocalAndRemoteInstance(setting, this.context);
        /*
        SharedPreferences.Editor editPref = this.getSharedPrefs().edit();
        editPref.putBoolean(KEY_IS_FIRST_RUN_COMPLETED, isFirstRunCompleted);
        editPref.apply();
        */
    }

    /***
     * Checks whether a user data is stored in phone's persistent storage
     * @return
     */
    public boolean userHasLoggedIn() {
        return WellnessUser.isInstanceSaved(KEY_USER_DEF, this.context);
    }

    /***
     * Login the user to the system if the user hasn't logged in yet.
     * @param username User's username
     * @param password User's password
     * @throws IOException
     */
    public void loginUser(String username, String password) throws OAuth2Exception, IOException {
        this.user = new WellnessUser(username, password, clientId, clientSecret, SERVER_URL, OAUTH_TOKEN_PATH);
        this.user.saveInstance(KEY_USER_DEF, context);
        this.server = new WellnessRestServer(SERVER_URL, DEFAULT_PORT, API_PATH, this.getUser());
    }

    /***
     * Logout the user from the system if the user has logged in.
     */
    public void logoutUser () {
        if (this.userHasLoggedIn()) {
            this.getUser().deleteSavedInstance(KEY_USER_DEF, this.context);
            Group.deleteInstance(this.context, this.getServer());
            ChallengeManager.deleteInstance(context, getServer());
            StoryManager.deleteStoryDefs(context, this.getServer());
            FirebaseUserManager.logout();
        }
    }

    public boolean isReloginNeeded() {
        if (this.getUser() != null) {
            return this.getUser().isTokenStalled();
        } else {
            return true;
        }
    }

    /***
     * INVARIANT: The user has been logged in to the Server.
     * @return User that has been verified
     */
    public WellnessUser getUser() {
        if (this.user == null) {
            this.user = WellnessUser.getSavedInstance(KEY_USER_DEF, this.context);
        }
        return this.user;
    }

    /***
     * INVARIANT: The user has been logged in to the Server.
     * @return Server that has been verified
     */
    public WellnessRestServer getServer() {
        if (this.server == null)
            this.server = new WellnessRestServer(SERVER_URL, DEFAULT_PORT, API_PATH, this.getUser());
        return this.server;
    }

    /***
     * INVARIANT: The user has been logged in to the Server.
     * @return True if server is online. Otherwise return false.
     */
    public boolean isServerOnline() {
        return WellnessRestServer.isServerOnline(this.context);
    }

    public boolean isFileExists(String filename) {
        return this.getServer().isFileExists(this.context, filename);
    }

    /**
     * INVARIANT: The user has been logged in to the Server.
     * @return Group of the logged user
     */
    public Group getGroup() {
        return Group.getInstance(this.context, this.getServer());
    }

    /**
     * INVARIANT: The user has been logged in to the Server.
     * @param isUseSaved Whether use the saved instance
     * @return Group of the logged user
     */
    public Group getGroup(boolean isUseSaved) {
        return Group.getInstance(this.context, isUseSaved, this.getServer());
    }

    public Person getCaregiver() {
        return getPersonByRole(Person.ROLE_PARENT);
    }

    public Person getChild() {
        return getPersonByRole(Person.ROLE_CHILD);
    }

    private Person getPersonByRole (String roleString) {
        try {
            return this.getGroup().getPersonByRole(roleString);
        } catch (PersonDoesNotExistException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public SynchronizedSetting getSynchronizedSetting() {
        return SynchronizedSettingRepository.getLocalInstance(context);
    }

    public int getReflectionIteration() {
        return this.getSynchronizedSetting().getReflectionIteration();
    }

    public void incrementReflectionIteration() {
        SynchronizedSetting setting = this.getSynchronizedSetting();
        int iteration = setting.getReflectionIteration();
        setting.setReflectionIteration(iteration + 1);
        setting.getReflectionIterationEpochs().add(Calendar.getInstance().getTimeInMillis());
        StoryListInfo newStoryListInfo = new SynchronizedSetting.StoryListInfo();
        StoryListInfo oldStoryListInfo = setting.getStoryListInfo();

        if (oldStoryListInfo.getUnlockedStories().size() >= 2) {
            String firstStoryId = oldStoryListInfo.getUnlockedStories().get(1);
            newStoryListInfo.getUnlockedStories().add(firstStoryId);
            newStoryListInfo.getUnreadStories().add(firstStoryId);
            newStoryListInfo.setHighlightedStoryId(firstStoryId);
        }
        if (oldStoryListInfo.getUnlockedStoryPages().size() >= 1) {
            String firstStoryPageId = oldStoryListInfo.getUnlockedStoryPages().get(0);
            newStoryListInfo.getUnlockedStoryPages().add(firstStoryPageId);
        }
        setting.setStoryListInfo(newStoryListInfo);

        SynchronizedSettingRepository.saveLocalAndRemoteInstance(setting, context);
    }

    public long getReflectionIterationMinEpoch() {
        List<Long> epochs = this.getSynchronizedSetting().getReflectionIterationEpochs();
        if (this.getReflectionIteration() == epochs.size()) {
            return epochs.get(this.getReflectionIteration() - 1);
        } else {
            return 0L;
        }
    }

    // STORY MANAGER
    public StoryManager getStoryManager() {
        if (this.storyManager == null)
            this.storyManager = StoryManager.create(this.getServer());
        return this.storyManager;
    }

    public boolean isStoryListCacheExists(Context context) {
        return this.getStoryManager().isStoryListCacheExists(context);
    }

    public void loadStoryList(boolean isUseSaved) {
        this.getStoryManager().loadStoryList(context, isUseSaved);
    }

    public void deleteStoryDefs() {
        StoryManager.deleteStoryDefs(context, this.getServer());
    }

    public List<StoryInterface> getStoryList() { return this.getStoryManager().getStoryList(); }

    public void resetStoryStatesAsync() {
        SynchronizedSetting setting = getSynchronizedSetting();
        setting.getStoryListInfo().setCurrentStoryPageId(new HashMap<String, Integer>());
        SynchronizedSettingRepository.saveLocalAndRemoteInstance(setting, context);
        /*
        loadStoryList(true);
        for (StoryInterface story: getStoryList()) {
            story.fetchStoryDef(context, getServer(), getGroup());
            StoryStateInterface state = story.getState();
            if (state != null) {
                state.setCurrentPage(0);
                state.save(context, getGroup());
            }
        }
        */
    }

    // CHALLENGE MANAGER
    public ChallengeManagerInterface getChallengeManager() {
        return this.getChallengeManager(true);
    }

    public ChallengeManagerInterface getChallengeManager(boolean useSaved) {
        this.challengeManager = ChallengeManager.getInstance(this.getServer(), useSaved, this.context);
        return this.challengeManager;
    }

    public boolean isChallengeInfoStored() {
        return this.getChallengeManager(true).isChallengeInfoStored();
    }

    // FITNESS MANAGER
    public FitnessRepositoryInterface getFitnessManager() {
        this.fitnessManager = WellnessFitnessRepo.newInstance(this.getServer(), this.context);
        return this.fitnessManager;
    }

    // FIREBASE
    public FirebaseToken getFirebaseTokenAsync() {
        return FirebaseTokenRepository.getToken(this.getServer(), this.context);
    }

    /* PRIVATE METHODS */
    public SharedPreferences getSharedPrefs() {
        if (this.sharedPrefs == null)
            this.sharedPrefs = WellnessIO.getSharedPref(this.context);
        return this.sharedPrefs;
    }

}
