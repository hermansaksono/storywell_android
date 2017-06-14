package edu.neu.ccs.wellness.storytelling.models;

import edu.neu.ccs.wellness.storytelling.interfaces.RestServerInterface;
import edu.neu.ccs.wellness.storytelling.interfaces.StoryContentInterface;
import edu.neu.ccs.wellness.storytelling.interfaces.UserAuthInterface;
import edu.neu.ccs.wellness.storytelling.interfaces.UserAuthInterface.AuthType;

/**
 * Created by hermansaksono on 6/14/17.
 */

public class WellnessRestServer implements RestServerInterface {
    private String hostname;
    private int port;
    private String apiPath;
    private WellnessUser user;

    // CONSTRUCTORS

    public WellnessRestServer (String hostname, int port, String apiPath, WellnessUser user) {
        this.hostname = hostname;
        this.port = port;
        this.apiPath = apiPath;
        this.user = user;
    }

    // PUBLIC METHODS
    @Override
    public void connect(String resourcePath, String params) {
        if (this.user.getType() == AuthType.BASIC) {
            this.connectUsingBasic();
        }
        else if (this.user.getType() == AuthType.OAUTH2) {
            this.connectUsingOauth2();
        }
    }

    @Override
    public WellnessUser getUser() {
        return this.user;
    }

    // PRIVATE METHODS
    private void connectUsingBasic() {

    }

    private void connectUsingOauth2() {

    }
}
