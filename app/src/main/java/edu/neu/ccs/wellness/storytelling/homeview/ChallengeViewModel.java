package edu.neu.ccs.wellness.storytelling.homeview;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;

import org.json.JSONException;

import java.io.IOException;

import edu.neu.ccs.wellness.fitness.interfaces.ChallengeManagerInterface;
import edu.neu.ccs.wellness.fitness.interfaces.ChallengeStatus;
import edu.neu.ccs.wellness.fitness.interfaces.FitnessRepositoryInterface;
import edu.neu.ccs.wellness.fitness.interfaces.UnitChallengeInterface;
import edu.neu.ccs.wellness.server.RestServer;
import edu.neu.ccs.wellness.storytelling.Storywell;

/**
 * Created by hermansaksono on 5/16/18.
 */

public class ChallengeViewModel extends AndroidViewModel {

    private MutableLiveData<UnitChallengeInterface> mutableUnitChallenge = null;
    private RestServer.ResponseType status = RestServer.ResponseType.OTHER;

    /* CONSTRUCTOR */
    public ChallengeViewModel(Application application) {
        super(application);
    }

    /* PUBLIC METHODS */
    public LiveData<UnitChallengeInterface> getUnitChallenge() {
        if (this.mutableUnitChallenge == null) {
            this.mutableUnitChallenge = new MutableLiveData<UnitChallengeInterface>();
            loadUnitChallenge();
        }
        return this.mutableUnitChallenge;
    }

    /* PRIVATE METHODS */
    private void loadUnitChallenge() {
        new LoadUnitChallenge().execute();
    }

    /* ASYNCTASKS */
    private class LoadUnitChallenge extends AsyncTask<Void, Integer, RestServer.ResponseType> {
        Storywell storywell = new Storywell(getApplication());
        FitnessRepositoryInterface fitnessManager = storywell.getFitnessManager();

        protected RestServer.ResponseType doInBackground(Void... voids) {
            if (storywell.isServerOnline() == false) {
                status = RestServer.ResponseType.NO_INTERNET;
            }

            try {
                ChallengeManagerInterface challengeManager = storywell.getChallengeManager();
                UnitChallengeInterface unitChallenge = null;
                if (challengeManager.getStatus() == ChallengeStatus.UNSYNCED_RUN) {
                    unitChallenge = challengeManager.getUnsyncedChallenge();
                } else if (challengeManager.getStatus() == ChallengeStatus.RUNNING) {
                    unitChallenge = challengeManager.getRunningChallenge();
                }

                mutableUnitChallenge.setValue(unitChallenge);
                status = RestServer.ResponseType.SUCCESS_202;
            } catch (JSONException e) {
                e.printStackTrace();
                status = RestServer.ResponseType.BAD_JSON;
            } catch (IOException e) {
                e.printStackTrace();
                status = RestServer.ResponseType.BAD_REQUEST_400;
            }

            return status;
        }

        private boolean isStatusRunning(ChallengeStatus status) {
            return  status == ChallengeStatus.UNSYNCED_RUN || status == ChallengeStatus.RUNNING;
        }
    }
}
