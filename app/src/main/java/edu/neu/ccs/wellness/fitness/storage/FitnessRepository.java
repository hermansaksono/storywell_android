package edu.neu.ccs.wellness.fitness.storage;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.neu.ccs.wellness.fitness.interfaces.FitnessRepositoryInterface;
import edu.neu.ccs.wellness.fitness.interfaces.FitnessSample;
import edu.neu.ccs.wellness.people.Person;

/**
 * Created by hermansaksono on 6/24/18.
 */

public class FitnessRepository implements FitnessRepositoryInterface {

    public static final String FIREBASE_PATH_DAILY = "person_daily_fitness";
    public static final String FIREBASE_PATH_INTRADAY = "person_intraday_fitness";
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    private DatabaseReference firebaseDbRef;

    public FitnessRepository() {
        this.firebaseDbRef = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public void fetchDailyFitness(Person person, Date startDate, Date endDate, final ValueEventListener listener) {
        this.firebaseDbRef
                .child(FIREBASE_PATH_DAILY)
                .child(String.valueOf(person.getId()))
                //.orderByKey()
                //.startAt(String.valueOf(startDate.getTime()))
                //.endAt(String.valueOf(endDate.getTime()))
                .orderByChild(OneDayFitnessSample.KEY_TIMESTAMP)
                .startAt(startDate.getTime())
                .endAt(endDate.getTime())
                .addListenerForSingleValueEvent(listener);
    }

    @Override
    public void insertDailyFitness(Person person, List<FitnessSample> samples) {
        DatabaseReference ref = this.firebaseDbRef
                .child(FIREBASE_PATH_DAILY)
                .child(String.valueOf(person.getId()));
        for (FitnessSample sample : samples) {
            //ref.child(String.valueOf(sample.getTimestamp())).setValue(sample);
            ref.child(getDateString(sample.getDate())).setValue(sample);
        }

    }

    @Override
    public void fetchIntradayFitness(Person person, Date date, final ValueEventListener listener) {
        this.firebaseDbRef
                .child(FIREBASE_PATH_INTRADAY)
                .child(String.valueOf(person.getId()))
                .child(String.valueOf(getDateString(date)))
                .orderByKey()
                //.child(String.valueOf(date.getTime()))
                //.orderByChild(OneDayFitnessSample.KEY_TIMESTAMP)
                .addListenerForSingleValueEvent(listener);
    }

    @Override
    public void insertIntradayFitness(Person person, Date date, List<FitnessSample> samples) {
        DatabaseReference ref = this.firebaseDbRef
                .child(FIREBASE_PATH_INTRADAY)
                .child(String.valueOf(person.getId()))
                .child(getDateString(date));
                //.child(String.valueOf(date.getTime()));
        for (FitnessSample sample : samples) {
            ref.child(String.valueOf(sample.getTimestamp())).setValue(sample);
        }
    }

    public static List<OneDayFitnessSample> getDailyFitnessSamples(DataSnapshot dataSnapshot) {
        List<OneDayFitnessSample> dailySamples = new ArrayList<>();
        if (dataSnapshot.exists()) {
            for (DataSnapshot sample : dataSnapshot.getChildren()) {
                dailySamples.add(sample.getValue(OneDayFitnessSample.class));
            }
        }
        return dailySamples;
    }

    public static List<IntradayFitnessSample> getIntradayFitnessSamples(DataSnapshot dataSnapshot, int timeInterval) {
        List<IntradayFitnessSample> minuteByMinuteSamples = getIntradayFitnessSamples(dataSnapshot);
        if (timeInterval != 1) {
            return getIntradayFitnessSamplesByMinutes(minuteByMinuteSamples, timeInterval);
        } else {
            return minuteByMinuteSamples;
        }
    }

    public static List<IntradayFitnessSample> getIntradayFitnessSamples(DataSnapshot dataSnapshot) {
        List<IntradayFitnessSample> intradaySamples = new ArrayList<>();
        if (dataSnapshot.exists()) {
            for (DataSnapshot sample : dataSnapshot.getChildren()) {
                intradaySamples.add(sample.getValue(IntradayFitnessSample.class));
            }
        }
        return intradaySamples;
    }

    private static List<IntradayFitnessSample> getIntradayFitnessSamplesByMinutes(List<IntradayFitnessSample> samples, int interval) {
        List<IntradayFitnessSample> intradaySamples = new ArrayList<>();
        int numOfSamples = samples.size();
        for(int i = 0; i < numOfSamples; i += interval) {
            IntradayFitnessSample sample = new IntradayFitnessSample(samples.get(i).getTimestamp()
                    , sumSteps(samples, i, interval));
            intradaySamples.add(sample);
        }
        return intradaySamples;
    }

    private static int sumSteps(List<IntradayFitnessSample> samples, int startIndex, int interval) {
        int endIndex = startIndex + interval;
        int steps = 0;
        for (int i = startIndex; i < endIndex; i++) {
            steps += samples.get(i).getSteps();
        }
        return steps;
    }

    public static String getDateString(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        return sdf.format(date);
    }
}
