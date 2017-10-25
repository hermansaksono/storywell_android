package edu.neu.ccs.wellness.storytelling;

import android.content.Context;

import java.io.IOException;

import edu.neu.ccs.wellness.storytelling.interfaces.StorytellingException;
import edu.neu.ccs.wellness.server.WellnessRestServer;
import edu.neu.ccs.wellness.server.WellnessUser;

/**
 * Created by hermansaksono on 10/24/17.
 */

public class Storywell {

    public static final String SERVER_URL = "http://wellness.ccs.neu.edu/storytelling_dev/";
    public static final String API_PATH = "api/";
    public static final String KEY_USER_DEF = "storywell_user";
    public static final String DEFAULT_USER =  "family01";
    public static final String DEFAULT_PASS =  "tacos000";

    private static final String clientId = "8QPgBwRdt2uHrYZvQCK60FV6AMxDOFKm19Dqzwrz";
    private static final String clientSecret = "7qaXVwM4vYIjtrUrodM1FFUyDHSTL6xCumN2JX54v58MWuyBG80OIQaZdUpWuJpDaTL9nNkx84F7Hi5zCGsVSqNsOdatDogVrHfyiYufbo1ysuKg9tfPeRwkgHLSI6bX";

    private Context context;
    private WellnessUser user;
    private WellnessRestServer server;
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
     * @param
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
    public void loginUser(String username, String password) {
        try {
            this.user = new WellnessUser(username, password, clientId, clientSecret, SERVER_URL);
            this.user.saveInstance(KEY_USER_DEF, context);
        } catch (StorytellingException e) {
            this.message = e.getMessage();
        } catch (IOException e) {
            this.message = e.getMessage();
        }
    }

    /***
     * Logout the user from the system if the user has logged in.
     */
    public void logoutUser () {
        if (this.userHasLoggedIn()) {
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
}
