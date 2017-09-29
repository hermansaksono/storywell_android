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
        NO_INTERNET, SUCCESS_202, FORBIDDEN_403, NOT_FOUND_404, OTHER;
    }

    public AuthUser getUser();

    public boolean isOnline(Context context);

    public String makeGetRequest(URL url);

    public String loadHttpRequest(Context context, String filename, String Url);

    public String loadGetRequest(Context context, String jsonFile, String resourcePath);

    public void downloadToStorage(Context context, String filename, String Url);
}
