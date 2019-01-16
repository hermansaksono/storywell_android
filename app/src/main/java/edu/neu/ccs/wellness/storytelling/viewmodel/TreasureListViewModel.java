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
import edu.neu.ccs.wellness.storytelling.treasure.TreasureInterface;

/**
 * Created by hermansaksono on 1/16/19.
 */

public class TreasureListViewModel extends AndroidViewModel {

    private MutableLiveData<List<ReflectionPiles>> treasuresLiveData;

    public TreasureListViewModel(Application application) {
        super(application);
    }

    public LiveData<List<TreasureInterface>> getTreasures() {
        if (this.treasuresLiveData == null) {
            this.treasuresLiveData = new MutableLiveData<List<TreasureInterface>>();
            this.refreshTreasures();
        }
        return this.treasuresLiveData;
    }

    public boolean refreshTreasures() {
        if (treasuresLiveData == null) {
            return false;
        } else {
            new LoadTreasureListAsync().execute();
            return true;
        }
    }

    /* ASYNCTASKS */
    private class LoadTreasureListAsync extends AsyncTask<Void, Integer, RestServer.ResponseType> {
        Storywell storywell = new Storywell(getApplication());

        protected RestServer.ResponseType doInBackground(Void... voids) {
            return RestServer.ResponseType.SUCCESS_202;
        }

        protected void onPostExecute(RestServer.ResponseType result) {
            if (result == RestServer.ResponseType.SUCCESS_202) {
                //storiesLiveData.setValue(storywell.getStoryList());
            } else if (result == RestServer.ResponseType.NO_INTERNET) {
                // DO NOTHING
            }
        }

    }
}
