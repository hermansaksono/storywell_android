package edu.neu.ccs.wellness.storytelling.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;

import java.io.IOException;

import edu.neu.ccs.wellness.fitness.interfaces.AvailableChallengesInterface;
import edu.neu.ccs.wellness.fitness.interfaces.ChallengeManagerInterface;
import edu.neu.ccs.wellness.server.RestServer.ResponseType;
import edu.neu.ccs.wellness.server.WellnessRestServer;
import edu.neu.ccs.wellness.story.interfaces.StoryInterface;
import edu.neu.ccs.wellness.storytelling.Storywell;

/**
 * Created by hermansaksono on 5/16/18.
 */

public class ChallengePickerViewModel extends AndroidViewModel {

    private Storywell storywell;
    private MutableLiveData<AvailableChallengesInterface> groupChallengeLiveData;

    public ChallengePickerViewModel(Application application) {
        super(application);
        this.storywell =  new Storywell(getApplication());
    }

    /**
     * Get the list of {@link StoryInterface}.
     * @return
     */
    public LiveData<AvailableChallengesInterface> getGroupChallenges() {
        if (this.groupChallengeLiveData == null) {
            this.groupChallengeLiveData = new MutableLiveData<>();
            this.loadChallenges();
        }
        return this.groupChallengeLiveData;
    }

    private void loadChallenges() {
        new LoadChallengesAsync().execute();
    }


    private class LoadChallengesAsync extends AsyncTask<Void, Integer, ResponseType> {

        protected ResponseType doInBackground(Void... voids) {
            if (WellnessRestServer.isServerOnline(getApplication()) == false) {
                return ResponseType.NO_INTERNET;
            }

            try {
                ChallengeManagerInterface challengeManager = storywell.getChallengeManager();
                groupChallengeLiveData.postValue(challengeManager.getAvailableChallenges());
                return ResponseType.SUCCESS_202;
            } catch (JSONException e) {
                e.printStackTrace();
                return ResponseType.BAD_JSON;
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseType.BAD_REQUEST_400;
            }
        }

        protected void onPostExecute(ResponseType result) {
            switch (result) {
                case SUCCESS_202:
                    Log.d("SWELL", "ChallengePicker loaded this challenge: " +
                            result.toString());
                    break;
                default:
                    break;
            }
        }

    }
}
