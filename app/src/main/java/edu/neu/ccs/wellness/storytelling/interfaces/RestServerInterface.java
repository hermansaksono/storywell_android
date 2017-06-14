package edu.neu.ccs.wellness.storytelling.interfaces;

/**
 * Created by hermansaksono on 6/14/17.
 */

public interface RestServerInterface {
    public void connect(String resourcePath, String params);

    public UserAuthInterface getUser();
}
