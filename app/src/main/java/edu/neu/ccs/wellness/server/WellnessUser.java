package edu.neu.ccs.wellness.server;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.Locale;

import ca.mimic.oauth2library.OAuth2Client;
import ca.mimic.oauth2library.OAuthError;
import ca.mimic.oauth2library.OAuthResponse;
import edu.neu.ccs.wellness.utils.WellnessIO;

/**
 * Created by hermansaksono on 6/14/17.
 * This model class handle's all of the User's aspect of Authentication
 */

public class WellnessUser implements AuthUser {

    public static final String ERROR_REFRESH_TOKEN_MISSING = "Refresh token is null.";
    public static final String TO_STRING = "{ accessToken: %s, refreshToken: %s, expiresAt: %d }";

    private AuthType type = AuthType.UNAUTHENTICATED;
    private String username;
    private String password;
    private String serverUrl;
    private String authPath;
    private String clientId;
    private String clientSecret;
    private String accessToken;
    private String refreshToken;
    private Long expiresAt = new Long(0);

    private static final String SHAREDPREF_NAME = "wellness_user";

    @Override
    public String toString() {
        return String.format(Locale.US,
                TO_STRING, this.accessToken, this.refreshToken, this.expiresAt);
    }

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
            throws OAuth2Exception, IOException {

        OAuth2Client client = new OAuth2Client.Builder(username, password,
                clientId, clientSecret, serverUrl + authPath)
                .build();
        OAuthResponse response = client.requestAccessToken();
       // Log.d("WELL OAuth2 successful", String.valueOf(response.isSuccessful()));

        if (response.isSuccessful()) {
            this.type = AuthType.OAUTH2;
            this.username = username;
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
            throw new OAuth2Exception(error.getError());
        }
    }

    // PUBLIC STATIC METHODS

    /***
     * Get an instance of Wellness User that was saved to persistent storage
     * @return
     */
    public static WellnessUser getSavedInstance(String name, Context context) {
        SharedPreferences sharedPref = WellnessIO.getSharedPref(context);
        String json = sharedPref.getString(SHAREDPREF_NAME, null);
        WellnessUser user = new Gson().fromJson(json, WellnessUser.class);
        return user;
    }

    public static boolean isInstanceSaved(String name, Context context) {
        SharedPreferences sharedPref = WellnessIO.getSharedPref(context);
        boolean isInstanceSaved = sharedPref.contains(SHAREDPREF_NAME);
        Log.d("WELL Saved user exists", String.valueOf(isInstanceSaved));
        return isInstanceSaved;
    }

    // PUBLIC METHODS

    /***
     * Get the authentication type of the AuthUser enstance
     * @return the AuthUser's authentication type
     */
    @Override
    public AuthType getType() {
        return this.type;
    }

    /***
     * Get the username
     * @return
     */
    public String getUsername () { return this.username; }

    /***
     * Get the authentication string for this user. For OAUTH2 user type,
     * this function may make a remote call to refresh the token.
     * @param context application's context
     * @return the authentication string
     */
    @Override
    public String getAuthenticationString(Context context) throws IOException {
        if (this.type == AuthType.BASIC) {
            return this.getBasicAuthenticationHeader();
        }
        else if (this.type == AuthType.OAUTH2) {
            return this.getOAuth2AuthenticationHeader(context);
        }
        else {
            return null;
        }
    }

    /***
     * Save this instance to persistent storage
     */
    public void saveInstance(String name, Context context) {
        SharedPreferences sharedPref = WellnessIO.getSharedPref(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        String json = new Gson().toJson(this);
        editor.putString(SHAREDPREF_NAME, json);
        editor.commit();
        Log.d("WELL User saved", this.getUsername());
    }

    /***
     * Delete saved login from persistent storage
     * @param name Name of the shared preferences
     * @param context Application's context
     */
    public void deleteSavedInstance(String name, Context context) {
        SharedPreferences sharedPref = WellnessIO.getSharedPref(context);
        sharedPref.edit().remove(SHAREDPREF_NAME).commit();
    }

    /**
     * Determines whether the OAuth2 token cannot be used anymore and the user needs to relogin.
     * @return
     */
    public boolean isTokenStalled() {
        return this.refreshToken == null;
    }

    /**
     * Refresh token and save locally.
     * @param context
     */
    private void refreshTokenAndSave(Context context) {
        try {
            this.refresh();
            this.saveInstance(SHAREDPREF_NAME, context);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /***
     * Refresh the token
     */
    private void refresh() throws IOException {
        if (this.refreshToken == null) {
            throw new IOException(ERROR_REFRESH_TOKEN_MISSING);
        }

        OAuth2Client client = new OAuth2Client.Builder(this.clientId, this.clientSecret,
                this.serverUrl + this.authPath).build();
        OAuthResponse response = client.refreshAccessToken(this.refreshToken);

        if (response.isSuccessful()) {
            this.accessToken = response.getAccessToken();
            this.refreshToken = response.getRefreshToken();
            this.expiresAt = response.getExpiresAt();
        } else {
            throw new IOException("OAuth2 token refresh failed. " + this.toString());
        }
    }

    // PRIVATE HELPER METHODS
    private String getBasicAuthenticationHeader() {
        byte[] loginBytes = (this.username + ":" + this.password).getBytes();
        StringBuilder loginBuilder = new StringBuilder()
                .append("Basic ")
                .append(Base64.encodeToString(loginBytes, Base64.DEFAULT));
        return loginBuilder.toString();
    }

    private String getOAuth2AuthenticationHeader(Context context) throws IOException {
        if (this.isTokenExpired()) {
            this.refresh();
            this.saveInstance(SHAREDPREF_NAME, context);
        }
        return "Bearer " + this.accessToken;
    }

    private boolean isTokenExpired() {
        if (this.getType() == AuthType.OAUTH2) {
            return this.expiresAt == null || System.currentTimeMillis() >= this.expiresAt;
        } else {
            return false;
        }

    }
}
