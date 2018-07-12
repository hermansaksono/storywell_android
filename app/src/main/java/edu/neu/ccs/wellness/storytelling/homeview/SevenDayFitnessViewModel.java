package edu.neu.ccs.wellness.storytelling.homeview;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;

import java.util.Date;

import edu.neu.ccs.wellness.fitness.interfaces.FitnessException;
import edu.neu.ccs.wellness.fitness.interfaces.FitnessException.FitnessErrorType;
import edu.neu.ccs.wellness.fitness.interfaces.FitnessRepositoryInterface;
import edu.neu.ccs.wellness.fitness.interfaces.GroupFitnessInterface;
import edu.neu.ccs.wellness.server.RestServer.ResponseType;
import edu.neu.ccs.wellness.storytelling.Storywell;

/**
 * Created by hermansaksono on 5/16/18.
 */

public class SevenDayFitnessViewModel extends AndroidViewModel {

    private GroupFitnessInterface sevenDayFitness;
    private MutableLiveData<GroupFitnessInterface> sevenDayFitnessLiveDate;
    private MutableLiveData<ResponseType> status;

    /* CONSTRUCTOR */
    public SevenDayFitnessViewModel(Application application) {
        super(application);
    }

    /* PUBLIC METHODS */
    public LiveData<GroupFitnessInterface> getSevenDayFitness(Date startDate, Date endDate) {
        if (this.sevenDayFitnessLiveDate == null) {
            this.sevenDayFitnessLiveDate = new MutableLiveData<>();
            loadSevenDayFitness(startDate, endDate);
        }
        return this.sevenDayFitnessLiveDate;
    }

    public LiveData<ResponseType> fetchSevenDayFitness(Date startDate, Date endDate) {
        if (this.sevenDayFitnessLiveDate == null) {
            this.sevenDayFitnessLiveDate = new MutableLiveData<>();
        }
        if (this.status == null) {
            this.status = new MutableLiveData<>();
            loadSevenDayFitness(startDate, endDate);
        }
        return this.status;
    }

    public GroupFitnessInterface getSevenDayFitness() throws FitnessException {
        if (this.sevenDayFitness != null) {
            return this.sevenDayFitness;
        } else {
            throw new FitnessException(FitnessErrorType.NO_DATA, "Seven-day Fitness data is still null.");
        }
    }

    /* PRIVATE METHODS */
    private void loadSevenDayFitness(Date startDate, Date endDate) {
        new LoadSevenDayFitnessAsync(startDate, endDate).execute();
    }

    /* ASYNCTASKS */
    private class LoadSevenDayFitnessAsync extends AsyncTask<Void, Integer, ResponseType> {
        Storywell storywell = new Storywell(getApplication());
        Date startDate;
        Date endDate;

        public LoadSevenDayFitnessAsync(Date startDate, Date endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }

        protected ResponseType doInBackground(Void... voids) {
            if (storywell.isServerOnline() == false) {
                return ResponseType.NO_INTERNET;
            }

            try {
                sevenDayFitness = doGetMultiDayFitness();
                return ResponseType.SUCCESS_202;
            } catch (FitnessException e) {
                e.printStackTrace();
                FitnessErrorType errorType = e.getErrorType();
                if (errorType.equals(FitnessErrorType.JSON_EXCEPTION)) {
                    return ResponseType.BAD_JSON;
                } else if (errorType.equals(FitnessErrorType.IO_EXCEPTION)) {
                    return ResponseType.BAD_REQUEST_400;
                } else {
                    return ResponseType.OTHER;
                }
            }
        }

        protected void onPostExecute(ResponseType result) {
            sevenDayFitnessLiveDate.setValue(sevenDayFitness);
            status.setValue(result);
        }

        private GroupFitnessInterface doGetMultiDayFitness() throws FitnessException{
            FitnessRepositoryInterface fitnessManager = storywell.getFitnessManager();
            return fitnessManager.getMultiDayFitness(this.startDate, this.endDate);
        }
    }
}
