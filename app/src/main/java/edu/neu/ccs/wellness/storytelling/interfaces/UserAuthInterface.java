package edu.neu.ccs.wellness.storytelling.interfaces;

/**
 * Created by hermansaksono on 6/14/17.
 */

public interface UserAuthInterface {

    public UserAuthInterface create(RestServerInterface server, String username, String password);

    public UserAuthInterface create(RestServerInterface server,
                                    String accessToken, String refreshToken, String expiresAt);

}
