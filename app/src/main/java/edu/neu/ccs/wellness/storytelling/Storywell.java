package edu.neu.ccs.wellness.storytelling;

import android.content.Context;

import java.io.IOException;
import java.util.List;

import edu.neu.ccs.wellness.server.OAuth2Exception;
import edu.neu.ccs.wellness.server.WellnessRestServer;
import edu.neu.ccs.wellness.server.WellnessUser;
import edu.neu.ccs.wellness.storytelling.interfaces.StoryInterface;
import edu.neu.ccs.wellness.storytelling.models.StoryManager;

/**
 * Created by hermansaksono on 10/24/17.
 */

public class Storywell {

    public static final String SERVER_URL = "http://wellness.ccs.neu.edu/storytelling_dev/";
    public static final String API_PATH = "api/";
    public static final String OAUTH_TOKEN_PATH = "oauth/token/";
    public static final String KEY_USER_DEF = "storywell_user";
    public static final String DEFAULT_USER =  "family01";
    public static final String DEFAULT_PASS =  "tacos000";

    private static final String clientId = "8QPgBwRdt2uHrYZvQCK60FV6AMxDOFKm19Dqzwrz";
    private static final String clientSecret = "7qaXVwM4vYIjtrUrodM1FFUyDHSTL6xCumN2JX54v58MWuyBG80OIQaZdUpWuJpDaTL9nNkx84F7Hi5zCGsVSqNsOdatDogVrHfyiYufbo1ysuKg9tfPeRwkgHLSI6bX";

    private Context context;
    private WellnessUser user;
    private WellnessRestServer server;
    private StoryManager storyManager;
    private String message;

    /***
     * Constructor
     * @param context Application's context
     */
    public Storywell(Context context) {
        this.context = context;
    }

    /***
     * Checks whether a user data is stored in phone's persistent storage
     * @param context Application's context
     * @return
     */
    public static boolean userHasLoggedIn(Context context) {
        return WellnessUser.isInstanceSaved(KEY_USER_DEF, context);
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
    }

    /***
     * Logout the user from the system if the user has logged in.
     */
    public void logoutUser () {
        if (this.userHasLoggedIn(this.context)) {
            this.user.deleteSavedInstance(KEY_USER_DEF, this.context);
        }
    }

    /***
     * INVARIANT: The user has been logged in to the Server.
     * @return User that has been verified
     */
    public WellnessUser getUser() {
        if (this.user == null)
            this.user = WellnessUser.getSavedInstance(KEY_USER_DEF, this.context);
        return this.user;
    }

    /***
     * INVARIANT: The user has been logged in to the Server.
     * @return Server that has been verified
     */
    public WellnessRestServer getServer() {
        if (this.server == null)
            this.server = new WellnessRestServer(SERVER_URL, 0, API_PATH, this.getUser());

        return this.server;
    }

    public StoryManager getStoryManager() {
        if (this.storyManager == null)
            this.storyManager = StoryManager.create(server);
        return this.storyManager;
    }

    public List<StoryInterface> getStoryList() {
        return this.getStoryManager().getStoryList();
    }

    public boolean isServerOnline() {
        return this.getServer().isOnline(this.context);
    }
}
