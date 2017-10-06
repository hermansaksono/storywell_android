package edu.neu.ccs.wellness.storytelling.models;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import edu.neu.ccs.wellness.storytelling.interfaces.RestServer;

/**
 * This model class is the local "instance" of the remote Wellness REST API. This class handles all
 * of the communications to the Wellness REST API as well as the cache/local storage for the
 * request responses.
 * Created by hermansaksono on 6/19/17.
 */

public class WellnessRestServer implements RestServer {
    // PUBLIC CONSTANTS
    public static final String WELLNESS_SERVER_URL = "http://wellness.ccs.neu.edu/";
    public static final String STORY_API_PATH = "storytelling_dev/api/";
    public static final String DEFAULT_USER =  "family01";
    public static final String DEFAULT_PASS =  "tacos000";

    // CONSTANTS
    private String hostname;
    private int port;
    private String apiPath;
    private String baseUrl;
    private WellnessUser user;

    // CONSTRUCTORS
    public WellnessRestServer (String hostname, int port, String apiPath, WellnessUser user) {
        this.hostname = hostname;
        this.port = port;
        this.apiPath = apiPath;
        this.user = user;
        this.baseUrl = new StringBuilder(hostname).append(this.apiPath).toString();
    }

    // PUBLIC METHODS
    @Override
    public WellnessUser getUser() {
        return this.user;
    }

    /***
     * Return true if the Android device is online. Otherwise, return false
     * @param context
     * @return
     */
    @Override
    public boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        return (netInfo != null && netInfo.isConnectedOrConnecting());
    }

    /***
     * Do a HTTP GET Request to the @resourcePath in the server
     * @param url the url to a remote resource
     * @return The HTTP Response from the String
     * INVARIANT: This function assumes that internet connection is available,
     * the server is up, and the url is correct. TODO Make this more flexible
     */
    @Override
    public String doGetRequest(URL url) {
        String output = null;
        BufferedReader bufferedReader = null;
        HttpURLConnection connection = null;
        try {
            connection = this.getHttpConnectionToAResource(url, this.user.getAuthenticationString());
            String result;
            StringBuilder resultBuilder = new StringBuilder();

            bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            while ((result = bufferedReader.readLine()) != null) {
                resultBuilder.append(result);
            }
            bufferedReader.close();
            output = resultBuilder.toString();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            connection.disconnect();
        }

        return output;
    }

    /***
     * Do a HTTP POST request to the @resourcePath in the server with @data as input
     * @param url the url to a remote resource
     * @param data input data in key-value pairs (e.g., "name=Herman")
     * @return ResponseType.SUCCESS_202 if successful
     */
    @Override
    public ResponseType doPostRequest(URL url, String data) {
        ResponseType output = ResponseType.OTHER;
        try {
            HttpURLConnection connection = this.getHttpConnectionToAResource(url, this.user.getAuthenticationString());
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.getOutputStream().write(data.getBytes());
            output = ResponseType.SUCCESS_202;
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return output;
    }

    /***
     * If the filename does not exist in the external storage, then download file.
     * @param context
     * @param filename
     * @param url
     * @return String contents of the get response from the url
     */
    @Override
    public String saveGetResponse(Context context, String filename, String url) {
        String result = null;
        try {
            if (isFileExists(context, filename) == false) {
                result = this.doGetRequest(new URL(url));
                writeFileToStorage(context, filename, result);
            }
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return result;
    }

    /***
     * If the filename does not exist in the external storage, then download file.
     * @param context
     * @param filename
     * @param url
     * @return String contents of the stored file from the url
     */
    @Override
    public String getSavedGetResponse(Context context, String filename, String url) {
        if (isFileExists(context, filename)) {
            return readFileFromStorage(context, filename);
        }
        else {
            return this.saveGetResponse(context, filename, url);
        }
    }

    @Override
    /***
     * If the jsonFile does not exist in the internal storage, then do HTTP GET request to the
     * server and save it as jsonFile. Then return the contents of jsonFile.
     * @param context
     * @param jsonFile
     * @param resourcePath
     * @return
     */
    public String getSavedGetRequest(Context context, String jsonFile, String resourcePath) {
        String result = null;
        try {
            if (!isFileExists(context, jsonFile)) {
                URL url = this.getResourceURL(resourcePath);
                String jsonString = this.doGetRequest(url);
                writeFileToStorage(context, jsonFile, jsonString);
                result = jsonString;
            }
            else {
                result = readFileFromStorage(context, jsonFile);
            }
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public ResponseType postRequest (String data, String resourcePath) {
        ResponseType output = ResponseType.OTHER;
        try {
            URL url = this.getResourceURL(resourcePath);
            output = this.doPostRequest(url, data);
        }
        catch (MalformedURLException e) {
            output = ResponseType.BAD_REQUEST_400;
        }
        return output;
    }

    /**
     * Initialize the ImageLoader
     * @param context
     */
    public static void configureDefaultImageLoader(Context context) {
        ImageLoaderConfiguration defaultConfiguration = new ImageLoaderConfiguration.Builder(context)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .build();
        ImageLoader.getInstance().init(defaultConfiguration);
    }

    // PRIVATE METHODS
    /***
     * @param url the url to make the request
     * @param auth the basic authentication string
     * @return HttpURLConnection object for making requests to the REST server
     * @throws MalformedURLException
     * @throws IOException
     */
    private HttpURLConnection getHttpConnectionToAResource (URL url, String auth)
            throws MalformedURLException, IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.addRequestProperty("Authorization", auth);
        return connection;
    }

    /***
     * @param resourcePath
     * @return URL object that contains the URL to make the REST request
     * @throws MalformedURLException
     */
    private URL getResourceURL(String resourcePath) throws MalformedURLException {
        StringBuilder urlStringBuilder = new StringBuilder(this.baseUrl).append(resourcePath);
        return new URL(urlStringBuilder.toString());
    }

    // PRIVATE HELPER METHODS
    /***
     * Determines whether a file exists in the internal storage
     * @param context Android context
     * @param filename
     * @return true if the file exists in the internal storage. Otherwise return false;
     */
    private static boolean isFileExists(Context context, String filename) {
        File file = context.getFileStreamPath(filename);
        return file.exists();
    }

    private static void writeFileToStorage(Context context, String jsonFile, String jsonString) {
        try {
            FileOutputStream fos = context.openFileOutput(jsonFile, Context.MODE_PRIVATE);
            fos.write(jsonString.getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String readFileFromStorage(Context context, String jsonFilename) {
        StringBuffer sb = new StringBuffer("");
        try {
            FileInputStream fileInputStream = context.openFileInput(jsonFilename);
            InputStreamReader isReader = new InputStreamReader(fileInputStream);
            BufferedReader buffReader = new BufferedReader(isReader);
            String readString = buffReader.readLine () ;
            while (readString != null) {
                sb.append(readString);
                readString = buffReader.readLine () ;
            }
            isReader.close ();
            buffReader.close();
        } catch (IOException ioe) {
            ioe.printStackTrace () ;
        }
        return sb.toString();
    }
}
