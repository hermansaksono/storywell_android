package edu.neu.ccs.wellness.storytelling.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;

import java.util.List;

import edu.neu.ccs.wellness.server.RestServer;
import edu.neu.ccs.wellness.story.interfaces.StoryInterface;
import edu.neu.ccs.wellness.storytelling.Storywell;

/**
 * Created by hermansaksono on 5/16/18.
 */

public class StoryListViewModel extends AndroidViewModel {

    private MutableLiveData<List<StoryInterface>> storiesLiveData;

    public StoryListViewModel(Application application) {
        super(application);
    }

    public LiveData<List<StoryInterface>> getStories() {
        if (this.storiesLiveData == null) {
            this.storiesLiveData = new MutableLiveData<List<StoryInterface>>();
            this.loadStories();
        }
        return this.storiesLiveData;
    }

    private void loadStories() {
        new LoadStoryListAsync().execute();
    }

    /* ASYNCTASKS */
    private class LoadStoryListAsync extends AsyncTask<Void, Integer, RestServer.ResponseType> {
        Storywell storywell = new Storywell(getApplication());

        protected RestServer.ResponseType doInBackground(Void... voids) {
            if (storywell.isServerOnline()) {
                storywell.loadStoryList();
                return RestServer.ResponseType.SUCCESS_202;
            } else {
                return RestServer.ResponseType.NO_INTERNET;
            }
        }

        protected void onPostExecute(RestServer.ResponseType result) {
            if (result == RestServer.ResponseType.SUCCESS_202) {
                storiesLiveData.setValue(storywell.getStoryList());
            } else if (result == RestServer.ResponseType.NO_INTERNET) {
                // DO NOTHING
            }
        }

    }
}
