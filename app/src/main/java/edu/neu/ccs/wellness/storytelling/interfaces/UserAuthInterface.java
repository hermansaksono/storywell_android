package edu.neu.ccs.wellness.storytelling.interfaces;

/**
 * Created by hermansaksono on 6/14/17.
 */

public interface UserAuthInterface {
    public enum AuthType {BASIC, OAUTH2};

    public AuthType getType();
}
