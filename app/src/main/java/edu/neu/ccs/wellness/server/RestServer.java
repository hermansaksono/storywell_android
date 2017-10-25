package edu.neu.ccs.wellness.server;

import android.content.Context;

import java.io.IOException;
import java.net.URL;

/**
 * Created by hermansaksono on 6/14/17.
 */

public interface RestServer {
    enum ResponseType {
        NO_INTERNET, SUCCESS_202, BAD_REQUEST_400, FORBIDDEN_403, NOT_FOUND_404, OTHER
    }

    AuthUser getUser();

    boolean isOnline(Context context);

    String doGetRequest(URL url) throws IOException;

    String doPostRequest(URL url, String data) throws IOException;

    String doGetRequestThenSave(Context context, String filename, URL url) throws IOException;

    String doGetRequestUsingSaved(Context context, String filename, URL url) throws IOException ;

    String doGetRequestFromAResource(Context context, String jsonFile, String resourcePath, boolean useSaved) throws IOException;

    String doPostRequestFromAResource(String data, String resourcePath) throws IOException;
}
