package edu.neu.ccs.wellness.storytelling.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import edu.neu.ccs.wellness.server.RestServer;
import edu.neu.ccs.wellness.storytelling.Storywell;
import edu.neu.ccs.wellness.fitness.challenges.GroupChallenge;


public class AsyncDownloadChallenges extends AsyncTask<Void, Integer, RestServer.ResponseType> {

    private Context asyncTaskContext;

    public AsyncDownloadChallenges(Context asyncTaskContext) {
        this.asyncTaskContext = asyncTaskContext;
    }

    protected RestServer.ResponseType doInBackground(Void... voids) {
        Storywell storywell = new Storywell(this.asyncTaskContext);
        if (!storywell.isServerOnline()) {
            return RestServer.ResponseType.NO_INTERNET;
        } else {
            return GroupChallenge.downloadChallenges(this.asyncTaskContext, storywell.getServer());
        }
    }

    protected void onPostExecute(RestServer.ResponseType result) {
        Log.d("WELL Challenges d/l", result.toString());
    }
}

