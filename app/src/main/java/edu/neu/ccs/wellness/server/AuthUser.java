package edu.neu.ccs.wellness.server;

import java.io.IOException;

/**
 * Created by hermansaksono on 6/14/17.
 */

public interface AuthUser {
    enum AuthType {BASIC, OAUTH2, UNAUTHENTICATED, AUTH_FAILED}

    AuthType getType();

    String getAuthenticationString() throws IOException;
}
