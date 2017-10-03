package edu.neu.ccs.wellness.storytelling.interfaces;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import java.net.URL;

/**
 * Created by hermansaksono on 6/14/17.
 */

public interface RestServer {
    public enum ResponseType {
        NO_INTERNET, SUCCESS_202, BAD_REQUEST_400, FORBIDDEN_403, NOT_FOUND_404, OTHER;
    }

    public AuthUser getUser();

    public boolean isOnline(Context context);

    public String doGetRequest(URL url);

    public ResponseType doPostRequest(URL url, String data);

    public String saveGetResponse(Context context, String filename, String Url);

    public String getSavedGetResponse(Context context, String filename, String Url);

    public String getSavedGetRequest(Context context, String jsonFile, String resourcePath);

    public ResponseType postRequest(String data, String resourcePath);
}
