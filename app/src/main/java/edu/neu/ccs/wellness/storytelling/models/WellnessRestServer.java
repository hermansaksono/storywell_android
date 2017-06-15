package edu.neu.ccs.wellness.storytelling.models;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import edu.neu.ccs.wellness.storytelling.interfaces.RestServerInterface;

/**
 * Created by hermansaksono on 6/14/17.
 */

public class WellnessRestServer implements RestServerInterface {
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
    public String makeGetRequest(String resourcePath) {
        String output = null;
        try {
            String result;
            StringBuilder resultBuilder = new StringBuilder();
            HttpURLConnection connection = this.getConnection(resourcePath);

            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
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

    // PRIVATE METHODS
    /***
     * @param resourcePath the path to make the request
     * @return HttpURLConnection object for making requests to the REST server
     * @throws MalformedURLException
     * @throws IOException
     */
    private HttpURLConnection getConnection (String resourcePath)
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
}
