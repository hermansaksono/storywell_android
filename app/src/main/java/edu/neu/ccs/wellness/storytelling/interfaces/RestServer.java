package edu.neu.ccs.wellness.storytelling.interfaces;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by hermansaksono on 6/14/17.
 */

public interface RestServer {
    public enum ResponseType {
        NO_INTERNET, SUCCESS_202, FORBIDDEN_403, NOT_FOUND_404, OTHER;
    }

    public AuthUser getUser();

    public boolean isOnline(Context context);

    public String makeGetRequest(String resourcePath);

    public String loadGetRequest(Context context, String jsonFile, String resourcePath);

    public String downloadToStorage(Context context, String filename, String Url);

    //TODO fix this so it doesn't take in the api path--should all be under static URL
    public void getImage(String resourcePath, View img, String filename, Context context,
                         int width, int height, String apiPath);

    public void getImage(String url, View img, String filename, Context context,
                         int width, int height);
}
