package edu.neu.ccs.wellness.storytelling.interfaces;

/**
 * Created by hermansaksono on 6/14/17.
 */

public interface AuthUser {
    public enum AuthType {BASIC, OAUTH2};

    public AuthType getType();

    public String getAuthenticationString();
}
