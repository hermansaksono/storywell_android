package edu.neu.ccs.wellness.fitness.storage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import edu.neu.ccs.wellness.fitness.interfaces.FitnessSample;
import edu.neu.ccs.wellness.people.Person;

/**
 * Created by hermansaksono on 7/18/18.
 */

public class DummySamples {

    public void putDummyData() {
        FitnessRepository repo = new FitnessRepository();
        Person caregiver = new Person(1, "Adult", "P");
        Person child= new Person(2, "Child", "C");

        Calendar cal = getDummyDate();
        List<FitnessSample> caregiverSamples = new ArrayList<>();
        caregiverSamples.add(new OneDayFitnessSample(cal.getTime(), 1000));
        cal.add(Calendar.DAY_OF_YEAR, 1);
        caregiverSamples.add(new OneDayFitnessSample(cal.getTime(), 2000));
        cal.add(Calendar.DAY_OF_YEAR, 1);
        caregiverSamples.add(new OneDayFitnessSample(cal.getTime(), 3000));
        cal.add(Calendar.DAY_OF_YEAR, 1);
        caregiverSamples.add(new OneDayFitnessSample(cal.getTime(), 4000));
        cal.add(Calendar.DAY_OF_YEAR, 1);
        caregiverSamples.add(new OneDayFitnessSample(cal.getTime(), 5000));
        cal.add(Calendar.DAY_OF_YEAR, 1);
        caregiverSamples.add(new OneDayFitnessSample(cal.getTime(), 6000));
        cal.add(Calendar.DAY_OF_YEAR, 1);
        caregiverSamples.add(new OneDayFitnessSample(cal.getTime(), 7000));


        cal = getDummyDate();
        List<FitnessSample> childSamples = new ArrayList<>();
        childSamples.add(new OneDayFitnessSample(cal.getTime(), 1100));
        cal.add(Calendar.DAY_OF_YEAR, 1);
        childSamples.add(new OneDayFitnessSample(cal.getTime(), 2100));
        cal.add(Calendar.DAY_OF_YEAR, 1);
        childSamples.add(new OneDayFitnessSample(cal.getTime(), 3100));
        cal.add(Calendar.DAY_OF_YEAR, 1);
        childSamples.add(new OneDayFitnessSample(cal.getTime(), 4100));
        cal.add(Calendar.DAY_OF_YEAR, 1);
        childSamples.add(new OneDayFitnessSample(cal.getTime(), 5100));
        cal.add(Calendar.DAY_OF_YEAR, 1);
        childSamples.add(new OneDayFitnessSample(cal.getTime(), 6100));
        cal.add(Calendar.DAY_OF_YEAR, 1);
        childSamples.add(new OneDayFitnessSample(cal.getTime(), 7100));

        /*
        repo.insertDailyFitness(caregiver, caregiverSamples, new onDataUploadListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailed() {

            }
        });
        repo.insertDailyFitness(child, childSamples, new onDataUploadListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailed() {

            }
        });
        */
    }

    public static Calendar getDummyDate() {
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTimeZone(TimeZone.getDefault());
        calendar.set(Calendar.YEAR, 2018);
        calendar.set(Calendar.MONTH, Calendar.JULY);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }
}
