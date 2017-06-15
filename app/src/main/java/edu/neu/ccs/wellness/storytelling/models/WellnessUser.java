package edu.neu.ccs.wellness.storytelling.models;

import android.util.Base64;

import edu.neu.ccs.wellness.storytelling.interfaces.RestServerInterface;
import edu.neu.ccs.wellness.storytelling.interfaces.UserAuthInterface;

/**
 * Created by hermansaksono on 6/14/17.
 */

public class WellnessUser implements UserAuthInterface {

    private AuthType type;
    private WellnessRestServer server;
    private String username;
    private String password;
    private String accessToken;
    private String refreshToken;
    private String expiresAt;

    // PRIVATE CONSTRUCTORS

    /**
     * Constructor for BASIC Auth type
     * @param username
     * @param password
     */
    public WellnessUser (String username, String password) {
        this.type = AuthType.BASIC;
        this.server = server;
        this.username = username;
        this.password = password;
    }

    /**
     * Constructor for OAUTH2 type
     * @param accessToken
     * @param refreshToken
     * @param expiresAt
     */
    public WellnessUser (String accessToken, String refreshToken, String expiresAt) {
        this.type = AuthType.OAUTH2;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresAt = expiresAt;
    }

    // PUBLIC METHODS
    @Override
    public AuthType getType() {
        return this.type;
    }

    @Override
    public String getAuthenticationString() {
        if (this.type == AuthType.BASIC) {
            return this.getBasicAuthenticatioString();
        }
        else {
            return null;
        }
    }

    // PRIVATE HELPER METHODS
    private String getBasicAuthenticatioString(){
        byte[] loginBytes = (this.username + ":" + this.password).getBytes();
        StringBuilder loginBuilder = new StringBuilder()
                .append("Basic ")
                .append(Base64.encodeToString(loginBytes, Base64.DEFAULT));
        return loginBuilder.toString();
    }
}
