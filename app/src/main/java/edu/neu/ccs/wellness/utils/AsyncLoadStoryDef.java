package edu.neu.ccs.wellness.utils;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import edu.neu.ccs.wellness.server.RestServer;
import edu.neu.ccs.wellness.storytelling.R;
import edu.neu.ccs.wellness.storytelling.models.StoryState;

/**
 * Created by hermansaksono on 10/31/17.
 */

// ASYNCTASK CLASSES
private class AsyncLoadStoryDef extends AsyncTask<Void, Integer, RestServer.ResponseType> {
    protected RestServer.ResponseType doInBackground(Void... nothingburger) {
        RestServer.ResponseType result = null;
        if (server.isOnline(getApplicationContext())) {
            story.loadStoryDef(getApplicationContext(), server);
            tempCodeToPopulateStoryState(); // TODO Remove this
            result = RestServer.ResponseType.SUCCESS_202;
        } else {
            result = RestServer.ResponseType.NO_INTERNET;
        }
        return result;
    }

    protected void onPostExecute(RestServer.ResponseType result) {
        Log.d("WELL Story download", result.toString());
        if (result == RestServer.ResponseType.NO_INTERNET) {
            showErrorMessage(getString(R.string.error_no_internet));
        } else if (result == RestServer.ResponseType.SUCCESS_202) {
            InitStoryContentFragments();
        }
    }

    private void tempCodeToPopulateStoryState() { // TODO Remove this
        StoryState state = (StoryState) story.getState();
        state.addReflection(5, "http://recording_reflection_1_in_page_5");
        state.addReflection(6, "http://recording_reflection_2_in_page_6");
    }
}

    // PRIVATE HELPER METHODS
    private void showErrorMessage(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }
