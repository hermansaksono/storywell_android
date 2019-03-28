package edu.neu.ccs.wellness.server;

import android.content.Context;

import java.io.IOException;
import java.net.URL;

/**
 * Created by hermansaksono on 6/14/17.
 */

public interface RestServer {
    enum ResponseType {
        NO_INTERNET, SUCCESS_202, BAD_REQUEST_400, FORBIDDEN_403, NOT_FOUND_404, BAD_JSON,
        OTHER, UNINITIALIZED, FETCHING, LOGIN_EXPIRED
    }

    AuthUser getUser();

    boolean isOnline(Context context);

    boolean isFileExists(Context context, String filename);

    String doGetRequest(Context context, URL url) throws IOException;

    String doPostRequest(Context context, URL url, String data) throws IOException;

    String doGetRequestThenSave(Context context, String filename, URL url) throws IOException;

    String doGetRequestUsingSaved(Context context, String filename, URL url) throws IOException ;

    String doGetRequestFromAResource(Context context, String jsonFile, String resourcePath, boolean useSaved) throws IOException;

    String doSimpleGetRequestFromAResource(Context context, String urlString) throws IOException;

    String doPostRequestFromAResource(Context context, String data, String resourcePath) throws IOException;

    boolean resetSaved(Context context, String filename);
}
