package edu.neu.ccs.wellness.server;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import com.google.gson.Gson;

import java.io.IOException;

import ca.mimic.oauth2library.OAuth2Client;
import ca.mimic.oauth2library.OAuthError;
import ca.mimic.oauth2library.OAuthResponse;
import edu.neu.ccs.wellness.storytelling.interfaces.StorytellingException;

/**
 * Created by hermansaksono on 6/14/17.
 * This model class handle's all of the User's aspect of Authentication
 */

public class WellnessUser implements AuthUser {

    private AuthType type = AuthType.UNAUTHENTICATED;
    private String username;
    private String password;
    private String serverUrl;
    private String authPath;
    private String clientId;
    private String clientSecret;
    private String accessToken;
    private String refreshToken;
    private Long expiresAt;

    private static final String SHAREDPREF_NAME = "wellness_user";

    // PUBLIC CONSTRUCTORS

    /**
     * Constructor for BASIC Auth type
     * @param username
     * @param password
     */
    public WellnessUser (String username, String password) {
        this.type = AuthType.BASIC;
        this.username = username;
        this.password = password;
    }

    /**
     * Constructor for OAuth2 type authentication. May make remote calls to get the tokens.
     * @param username
     * @param password
     * @param clientId
     * @param clientSecret
     */
    public WellnessUser (String username, String password,
                         String clientId, String clientSecret,
                         String serverUrl, String authPath)
            throws StorytellingException, IOException {

        OAuth2Client client = new OAuth2Client.Builder(username, password,
                clientId, clientSecret, serverUrl + authPath)
                .build();
        OAuthResponse response = client.requestAccessToken();

        if (response.isSuccessful()) {
            this.type = AuthType.OAUTH2;
            this.clientId = clientId;
            this.clientSecret = clientSecret;
            this.serverUrl = serverUrl;
            this.authPath = authPath;
            this.accessToken = response.getAccessToken();
            this.refreshToken = response.getRefreshToken();
            this.expiresAt = response.getExpiresAt();
        } else {
            this.type = AuthType.AUTH_FAILED;
            OAuthError error = response.getOAuthError();
            throw new StorytellingException(error.getError());
        }
    }

    // PUBLIC STATIC METHODS

    /***
     * Get an instance of Wellness User that was saved to persistent storage
     * @return
     */
    public static WellnessUser getSavedInstance(String name, Context context) {
        SharedPreferences sharedPref = getSharedPref(name, context);
        String json = sharedPref.getString(SHAREDPREF_NAME, null);
        return new Gson().fromJson(json, WellnessUser.class);
    }

    public static boolean isInstanceSaved(String name, Context context) {
        SharedPreferences sharedPref = getSharedPref(name, context);
        return sharedPref.contains(SHAREDPREF_NAME);
    }

    // PUBLIC METHODS

    /**
     * Get the authentication type of the AuthUser enstance
     * @return the AuthUser's authentication type
     */
    @Override
    public AuthType getType() {
        return this.type;
    }

    /***
     * Get the authentication string for this user. For OAUTH2 user type,
     * this function may make a remote call to refresh the token.
     * @return the authentication string
     */
    @Override
    public String getAuthenticationString() throws IOException {
        if (this.type == AuthType.BASIC) {
            return this.getBasicAuthenticationHeader();
        }
        else if (this.type == AuthType.OAUTH2) {
            return this.getOAuth2AuthenticationHeader();
        }
        else {
            return null;
        }
    }

    /***
     * Save this instance to persistent storage
     */
    public void saveInstance(String name, Context context) {
        SharedPreferences sharedPref = getSharedPref(name, context);
        SharedPreferences.Editor editor = sharedPref.edit();
        String json = new Gson().toJson(this);
        editor.putString(SHAREDPREF_NAME, json);
        editor.commit();
    }

    /***
     * Delete saved login from persistent storage
     * @param name Name of the shared preferences
     * @param context Application's context
     */
    public void deleteSavedInstance(String name, Context context) {
        SharedPreferences sharedPref = getSharedPref(name, context);
        sharedPref.edit().remove(SHAREDPREF_NAME).commit();
    }

    /***
     * Refresh the token
     */
    private void refresh() throws IOException {
        OAuth2Client client = new OAuth2Client.Builder(this.clientId, this.clientSecret, this.serverUrl).build();
        OAuthResponse response = client.refreshAccessToken(this.refreshToken);
        this.accessToken = response.getAccessToken();
        this.refreshToken = response.getRefreshToken();
        this.expiresAt = response.getExpiresAt();
    }

    // PRIVATE HELPER METHODS
    private String getBasicAuthenticationHeader() {
        byte[] loginBytes = (this.username + ":" + this.password).getBytes();
        StringBuilder loginBuilder = new StringBuilder()
                .append("Basic ")
                .append(Base64.encodeToString(loginBytes, Base64.DEFAULT));
        return loginBuilder.toString();
    }

    private String getOAuth2AuthenticationHeader() throws IOException {
        if (this.isTokenExpired()) {
            this.refresh();
        }
        return "Bearer " + this.accessToken;
    }

    private static SharedPreferences getSharedPref (String username, Context context) {
        String name = getSharedPrefFileName(username);
        return context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    private static String getSharedPrefFileName (String username) {
        StringBuilder sb = new StringBuilder()
                .append(WellnessUser.class.getSimpleName())
                .append(".")
                .append(username);
        return sb.toString();
    }

    private boolean isTokenExpired() {
        return System.currentTimeMillis() >= this.expiresAt;
    }
}
