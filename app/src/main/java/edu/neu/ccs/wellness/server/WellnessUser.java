package edu.neu.ccs.wellness.server;

import android.util.Base64;
import org.sdf.danielsz.OAuth2Client;
import org.sdf.danielsz.Token;

/**
 * Created by hermansaksono on 6/14/17.
 * This model class handle's all of the User's aspect of Authentication
 */

public class WellnessUser implements AuthUser {

    private AuthType type;
    private String username;
    private String password;
    private Token token;
    private String clientId;
    private String clientSecret;

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
                         String clientId, String clientSecret) {
        this.type = AuthType.OAUTH2;
        this.username = username;
        this.password = password;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.token = getToken(username, password, clientId, clientSecret);
    }

    // PUBLIC FACTORY METHOD(S)

    /***
     * Get an instance of Wellness User that was saved to persistent storage
     * @return
     */
    public static WellnessUser getSavedInstance() {
        // TODO
        return null;
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
    public String getAuthenticationString() {
        if (this.type == AuthType.BASIC) {
            return this.getBasicAuthenticationString();
        }
        else {
            return this.getOAuth2AuthenticationString();
        }
    }

    /***
     * Refresh the token
     */
    private void refresh() {
        OAuth2Client client = new OAuth2Client(this.username, this.password,
                this.clientId, this.clientSecret, WellnessRestServer.WELLNESS_SERVER_URL);
        this.token.refresh(client);
        this.saveInstance();
    }

    /***
     * Save this instance to persistent storage
     */
    private void saveInstance () {
        // TODO
    }

    // PRIVATE HELPER METHODS
    private String getBasicAuthenticationString() {
        byte[] loginBytes = (this.username + ":" + this.password).getBytes();
        StringBuilder loginBuilder = new StringBuilder()
                .append("Basic ")
                .append(Base64.encodeToString(loginBytes, Base64.DEFAULT));
        return loginBuilder.toString();
    }

    private String getOAuth2AuthenticationString() {
        if (this.token.isExpired()) {
            this.refresh();
        }
        byte[] loginBytes = (this.token.getAccessToken()).getBytes();
        StringBuilder loginBuilder = new StringBuilder()
                .append("Bearer ")
                .append(Base64.encodeToString(loginBytes, Base64.DEFAULT));
        return loginBuilder.toString();
    }

    private static Token getToken(String username, String password,
                                  String clientId, String clientSecret) {
        OAuth2Client client = new OAuth2Client(username, password,
                clientId, clientSecret, WellnessRestServer.WELLNESS_SERVER_URL);
        return client.getAccessToken();
    }
}
