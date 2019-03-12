package edu.neu.ccs.wellness.fitness.storage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by hermansaksono on 6/25/18.
 */

public class FitnessStorageUtils {

    public List<IntradayFitnessSample> stepsArrayToList(int[] activites, GregorianCalendar startDate) {
        List<IntradayFitnessSample> samples = new ArrayList<>();
        Calendar cal = startDate;
        int numMinutes = activites.length;
        for (int i = 0; i < numMinutes; i++) {
            cal.add(Calendar.MINUTE, i);
            samples.add(new IntradayFitnessSample(cal.getTimeInMillis(), activites[i]));
        }
        return samples;
    }

    public OneDayFitnessSample getOneDayFitnessSample(List<IntradayFitnessSample> samples)
            throws Exception {
        Date fitnessDate = getDay(samples);
        if (isStartOfDay(fitnessDate)) {
            return new OneDayFitnessSample(fitnessDate, getTotalSteps(samples));
        } else {
            throw new Exception("First intraday sample is not the start of the day");
        }
    }

    private int getTotalSteps(List<IntradayFitnessSample> samples) {
        int totalSteps = 0;
        for (IntradayFitnessSample intradaySample : samples) {
            totalSteps += intradaySample.getSteps();
        }
        return totalSteps;
    }

    private boolean isStartOfDay(Date date) {
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.HOUR) == 0
                && cal.get(Calendar.MINUTE) == 0
                && cal.get(Calendar.SECOND) == 0
                && cal.get(Calendar.MILLISECOND) == 0;
    }

    private Date getDay (List<IntradayFitnessSample> samples) {
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(samples.get(0).getDate());
        return cal.getTime();
    }
}
