package edu.neu.ccs.wellness.server;

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
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * This model class is the local "instance" of the remote Wellness REST API. This class handles all
 * of the communications to the Wellness REST API as well as the cache/local storage for the
 * request responses.
 * Created by hermansaksono on 6/19/17.
 */

public class WellnessRestServer implements RestServer {
    // PUBLIC CONSTANTS
    public static final boolean USE_SAVED = true;
    public static final boolean DONT_USE_SAVED = false;

    // CONSTANTS
    private String hostname;
    private int port;
    private String apiPath;
    private String baseUrl;
    private WellnessUser user;

    // CONSTRUCTORS
    public WellnessRestServer(String hostname, int port, String apiPath, WellnessUser user) {
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
        return isServerOnline(context);
    }

    public static boolean isServerOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        return (netInfo != null && netInfo.isConnectedOrConnecting());
    }

    /***
     * Determines whether a file exists in the internal storage
     * @param context Android context
     * @param filename
     * @return true if the file exists in the internal storage. Otherwise return false;
     */
    @Override
    public boolean isFileExists(Context context, String filename) {
        File file = context.getFileStreamPath(filename);
        return file.exists();
    }

    /***
     * Do a HTTP GET Request to the @resourcePath in the server
     * @param context application's context
     * @param url the url to a remote resource
     * @return The HTTP Response from the String
     * INVARIANT: This function assumes that internet connection is available,
     * the server is up, and the url is correct.
     */
    @Override
    public String doGetRequest(Context context, URL url) throws IOException {
        String output = null;
        BufferedReader bufferedReader = null;
        HttpURLConnection connection = null;
        try {
            connection = this.getHttpConnectionToAResource(
                    url, this.user.getAuthenticationString(context));
            Log.d("SWELL", "WellnessRestSever connecting to " + url.toString());
            //Log.d("SWELL", "WellnessRestSever uses authString " + this.user.getAuthenticationString());

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException("Error " + connection.getResponseCode()+ ": " + url.toString());
            } else {
                String result;
                StringBuilder resultBuilder = new StringBuilder();

                bufferedReader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
                while ((result = bufferedReader.readLine()) != null) {
                    resultBuilder.append(result);
                }
                bufferedReader.close();
                output = resultBuilder.toString();
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return output;
    }

    /***
     * Do a HTTP POST request to the @resourcePath in the server with @data as input
     * @param context application's context
     * @param url the url to a remote resource
     * @param data input data in key-value pairs (e.g., "name=Herman")
     * @return ResponseType.SUCCESS_202 if successful
     */
    @Override
    public String doPostRequest(Context context, URL url, String data) throws IOException {
        String output = null;
        BufferedReader bufferedReader = null;
        HttpURLConnection connection = null;
        try {
            // Preparation
            connection = this.getHttpConnectionToAResource(
                    url, this.user.getAuthenticationString(context));
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setDoOutput(true);

            // Send POST data
            OutputStreamWriter streamWriter = new OutputStreamWriter(connection.getOutputStream());
            streamWriter.write(data);
            streamWriter.flush();

            if (connection.getResponseCode() >= HttpURLConnection.HTTP_BAD_REQUEST) {
                throw new IOException(String.valueOf(connection.getResponseCode()));
            }

            // Read the POST response
            bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String result;
            StringBuilder resultBuilder = new StringBuilder();
            while ((result = bufferedReader.readLine()) != null) {
                resultBuilder.append(result);
            }

            streamWriter.close();
            bufferedReader.close();
            output = resultBuilder.toString();
        } finally {
            if (connection != null)
                connection.disconnect();
        }

        return output;
    }

    /***
     * Download file from url using HTTP GET then save as filename
     * @param context
     * @param filename
     * @param url
     * @return String contents of the get response from the url
     */
    @Override
    public String doGetRequestThenSave(Context context, String filename, URL url)
            throws IOException {
        String result = null;
        try {
            result = this.doGetRequest(context, url);
            writeFileToStorage(context, filename, result);
        } catch (MalformedURLException e) {
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
    public String doGetRequestUsingSaved(Context context, String filename, URL url)
            throws IOException {
        if (isFileExists(context, filename)) {
            return readFileFromStorage(context, filename);
        } else {
            return this.doGetRequestThenSave(context, filename, url);
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
    public String doGetRequestFromAResource(Context context, String jsonFile, String resourcePath, boolean useSaved)
            throws IOException {
        String result = null;
        try {
            if (isFileExists(context, jsonFile) && useSaved) {
                result = readFileFromStorage(context, jsonFile);
            } else {
                URL url = this.getResourceURL(resourcePath);
                result = this.doGetRequestThenSave(context, jsonFile, url);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Do an HTTP GET request to the @resourcePath
     * @param context application's context
     * @param resourcePath
     * @return
     * @throws IOException
     */
    @Override
    public String doSimpleGetRequestFromAResource(Context context, String resourcePath)
            throws IOException {
        URL url = getResourceURL(resourcePath);
        return this.doGetRequest(context, url);
    }

    /**
     * Do an HTTP POST request to the @resourcePath
     * @param context application's context
     * @param resourcePath
     * @return
     * @throws IOException
     */
    @Override
    public String doPostRequestFromAResource(Context context, String data, String resourcePath)
            throws IOException {
        String output = null;
        try {
            URL url = this.getResourceURL(resourcePath);
            output = this.doPostRequest(context, url, data);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return output;
    }

    @Override
    public boolean resetSaved(Context context, String filename) {
        if (isFileExists(context, filename)) {
            context.deleteFile(filename);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Initialize the ImageLoader
     *
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
     * @throws IOException
     */
    private HttpURLConnection getHttpConnectionToAResource(URL url, String auth)
            throws IOException {
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
     * Determines whether a file exists in the internal cache
     * @param context Android context
     * @param cachename
     * @return true if the file exists in the internal cache. Otherwise return false;
     */
    private static boolean isCacheExists(Context context, String cachename) {
        String cachePath = context.getCacheDir().getAbsolutePath() + cachename;
        File cacheFile = new File(cachePath);
        return cacheFile.exists();
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
            String readString = buffReader.readLine();
            while (readString != null) {
                sb.append(readString);
                readString = buffReader.readLine();
            }
            isReader.close();
            buffReader.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return sb.toString();
    }
}