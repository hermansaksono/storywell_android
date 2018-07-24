package edu.neu.ccs.wellness.storytelling.viewmodel.unused;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import edu.neu.ccs.wellness.fitness.interfaces.FitnessException;
import edu.neu.ccs.wellness.fitness.WellnessFitnessRepo;
import edu.neu.ccs.wellness.fitness.interfaces.FitnessRepositoryInterface;
import edu.neu.ccs.wellness.fitness.interfaces.GroupFitnessInterface;
import edu.neu.ccs.wellness.server.RestServer;
import edu.neu.ccs.wellness.server.RestServer.ResponseType;
import edu.neu.ccs.wellness.storytelling.Storywell;

/**
 * Created by hermansaksono on 5/17/18.
 */

public class OneDayGroupFitnessViewModel extends AndroidViewModel {
    private static final String DEFAULT_DATE = "2017-06-01";
    private MutableLiveData<GroupFitnessInterface> groupFitnessLiveData;

    public OneDayGroupFitnessViewModel(Application application) {
        super(application);
    }

    public LiveData<GroupFitnessInterface> getGroupFitness() {
        if (this.groupFitnessLiveData == null) {
            this.groupFitnessLiveData = new MutableLiveData<GroupFitnessInterface>();
            readOrFetchGroupFitness();
        }
        return this.groupFitnessLiveData;
    }

    private void readOrFetchGroupFitness() {
        new LoadGroupFitnessAsync().execute();
    }

    /* ASYNCTASKS */
    private class LoadGroupFitnessAsync extends AsyncTask<Void, Void, ResponseType> {
        Storywell storywell = new Storywell(getApplication());
        GroupFitnessInterface result = null;
        Date todayDate = getDummyDate();
        Date endDate = getNDaysLater(todayDate, 1);

        protected ResponseType doInBackground(Void... voids) {
            if (storywell.isServerOnline() == false) {
                return ResponseType.NO_INTERNET;
            }

            try {
                FitnessRepositoryInterface fitMan = storywell.getFitnessManager();
                result = fitMan.getMultiDayFitness(todayDate, endDate);
                return ResponseType.SUCCESS_202;
            } catch (FitnessException e) {
                e.printStackTrace();
                FitnessException.FitnessErrorType errorType = e.getErrorType();
                if (errorType.equals(FitnessException.FitnessErrorType.JSON_EXCEPTION)) {
                    return ResponseType.BAD_JSON;
                } else if (errorType.equals(FitnessException.FitnessErrorType.IO_EXCEPTION)) {
                    return ResponseType.BAD_REQUEST_400;
                } else {
                    return ResponseType.OTHER;
                }
            }
        }

        protected void onPostExecute(ResponseType response) {
            if (response == ResponseType.SUCCESS_202) {
                groupFitnessLiveData.setValue(result);
            } else if (response == RestServer.ResponseType.BAD_REQUEST_400) {
                // DO NOTHING
            } else if (response == RestServer.ResponseType.BAD_JSON) {
                // DO NOTHING
            } else if (response == RestServer.ResponseType.NO_INTERNET) {
                // DO NOTHING
            }
        }
    }

    /* HELPER METHODS */
    private static Date getDummyDate() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(WellnessFitnessRepo.JSON_DATE_FORMAT);
            return sdf.parse(DEFAULT_DATE);
        } catch (ParseException e) {
            e.printStackTrace();
            return new Date();
        }
    }

    private static Date getNDaysLater(Date date, int numDays) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, numDays);
        return cal.getTime();
    }
}
