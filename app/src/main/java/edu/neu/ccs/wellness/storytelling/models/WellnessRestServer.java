package edu.neu.ccs.wellness.storytelling.models;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

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

    @Override
    public boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        return (netInfo != null && netInfo.isConnectedOrConnecting());
    }

    /***
     * Do a HTTP GET Request to the @resourcePath in the server
     * @param resourcePath the path to a remote resource
     * @return The HTTP Response from the String
     * INVARIANT: This function assumes that internet connection is available,
     * the server is up, and the url is correct. TODO Make this more flexible
     */
    @Override
    public String makeGetRequest(String resourcePath) {
        String output = null;
        BufferedReader bufferedReader = null;
        try {
            String result;
            StringBuilder resultBuilder = new StringBuilder();
            HttpURLConnection connection = this.getHttpConnectionToAResource(resourcePath);

            bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            while ((result = bufferedReader.readLine()) != null) {
                resultBuilder.append(result);
            }
            bufferedReader.close();
            output = resultBuilder.toString();
        }

        catch (MalformedURLException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return output;
    }

    /***
     * If the jsonFile does not exist in the internal storage, then do HTTP GET request to the
     * server and save it as jsonFile. Then return the contents of jsonFile.
     * @param context
     * @param jsonFile
     * @param resourcePath
     * @return
     */
    public String loadGetRequest(Context context, String jsonFile, String resourcePath) {
        if (isFileExists(context, jsonFile) == false) {
            String jsonString = this.makeGetRequest(resourcePath);
            writeJsonFileToStorage(context, jsonFile, jsonString);
            return jsonString;
        }
        else {
            return readJsonFileFromStorage(context, jsonFile);
        }
    }

    /***
     * If the filename does not exist in the external storage, then download file.
     * @param context
     * @param filename
     * @param Url
     * @return
     */
    @Override
    public String downloadToStorage(Context context, String filename, String Url) {
        if (isFileExists(context, filename) == false) {

        }
        return null;
    }

    // PRIVATE METHODS
    /***
     * @param resourcePath the path to make the request
     * @return HttpURLConnection object for making requests to the REST server
     * @throws MalformedURLException
     * @throws IOException
     */
    private HttpURLConnection getHttpConnectionToAResource (String resourcePath)
            throws MalformedURLException, IOException {
        URL url = this.getURL(resourcePath);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.addRequestProperty("Authorization", this.user.getAuthenticationString());
        return connection;
    }

    /***
     * @param resourcePath
     * @return URL object that contains the URL to make the REST request
     * @throws MalformedURLException
     */
    private URL getURL (String resourcePath) throws MalformedURLException {
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

    private static void writeJsonFileToStorage(Context context, String jsonFile, String jsonString) {
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

    private static String readJsonFileFromStorage (Context context, String jsonFilename) {
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
