package edu.neu.ccs.wellness.storytelling.interfaces;

import android.content.Context;

/**
 * Created by hermansaksono on 6/14/17.
 */

public interface RestServerInterface {
    public UserAuthInterface getUser();

    public String makeGetRequest(String resourcePath);

    public String loadGetRequest(Context context, String jsonFile, String resourcePath);
}
