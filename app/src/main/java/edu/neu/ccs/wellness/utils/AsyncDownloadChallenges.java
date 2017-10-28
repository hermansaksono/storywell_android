package edu.neu.ccs.wellness.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import edu.neu.ccs.wellness.server.RestServer;
import edu.neu.ccs.wellness.server.WellnessRestServer;
import edu.neu.ccs.wellness.storytelling.Storywell;
import edu.neu.ccs.wellness.storytelling.models.challenges.GroupChallenge;


public class AsyncDownloadChallenges extends AsyncTask<Void, Integer, RestServer.ResponseType> {

    private Context asyncTaskContext;

    public AsyncDownloadChallenges(Context asyncTaskContext) {
        this.asyncTaskContext = asyncTaskContext;
    }

    protected RestServer.ResponseType doInBackground(Void... voids) {

        //WellnessUser user = new WellnessUser(Storywell.DEFAULT_USER, Storywell.DEFAULT_PASS);
        //WellnessRestServer server = new WellnessRestServer(Storywell.SERVER_URL, 0, Storywell.API_PATH, user);

        Storywell storywell = new Storywell(asyncTaskContext);
        if (!storywell.userHasLoggedIn())
            storywell.loginUser(Storywell.DEFAULT_USER, Storywell.DEFAULT_PASS);
        WellnessRestServer server = storywell.getServer();

        if (!server.isOnline(asyncTaskContext)) {
            return RestServer.ResponseType.NO_INTERNET;
        } else {
            return GroupChallenge.downloadChallenges(asyncTaskContext, server);
        }
    }

    protected void onPostExecute(RestServer.ResponseType result) {
        Log.d("WELL Challenges d/l", result.toString());
        if (result == RestServer.ResponseType.NO_INTERNET) {
            // TODO
        } else if (result == RestServer.ResponseType.NOT_FOUND_404) {
            // TODO
        } else if (result == RestServer.ResponseType.SUCCESS_202) {
            // TODO
        }
    }
}

