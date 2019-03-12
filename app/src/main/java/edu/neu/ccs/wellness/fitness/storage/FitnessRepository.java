package edu.neu.ccs.wellness.fitness.storage;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import edu.neu.ccs.wellness.fitness.MultiDayFitness;
import edu.neu.ccs.wellness.fitness.OneDayFitness;
import edu.neu.ccs.wellness.fitness.interfaces.FitnessSample;
import edu.neu.ccs.wellness.fitness.interfaces.OneDayFitnessInterface;
import edu.neu.ccs.wellness.people.Person;

/**
 * Created by hermansaksono on 6/24/18.
 */

public class FitnessRepository {

    private static final String FIREBASE_PATH_DAILY = "person_daily_fitness";
    private static final String FIREBASE_PATH_DAILY_CHILD = "/%s/";
    private static final String FIREBASE_PATH_INTRADAY = "person_intraday_fitness";
    private static final String FIREBASE_PATH_INTRADAY_CHILD = "/%s/%s/";
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    private DatabaseReference firebaseDbRef;

    public FitnessRepository() {
        this.firebaseDbRef = FirebaseDatabase.getInstance().getReference();
    }

    /* PUBLIC METHOD */

    /**
     * Fetches the daily fitness data of the given person starting from startDate to endDate.
     * Then perform the listener.
     * @param person
     * @param startDate
     * @param endDate
     * @param listener
     */
    public void fetchDailyFitness(Person person, Date startDate, Date endDate,
                                  final ValueEventListener listener) {
        this.firebaseDbRef
                .child(FIREBASE_PATH_DAILY)
                .child(String.valueOf(person.getId()))
                .orderByChild(OneDayFitnessSample.KEY_TIMESTAMP)
                .startAt(startDate.getTime())
                .endAt(endDate.getTime())
                .addListenerForSingleValueEvent(listener);
    }

    /**
     * Fetches the intraday fitness data for the given person starting from startDate to endDate.
     * Then perform the listener.
     * @param person
     * @param date
     * @param listener
     */
    public void fetchIntradayFitness(Person person, Date date, final ValueEventListener listener) {
        this.firebaseDbRef
                .child(FIREBASE_PATH_INTRADAY)
                .child(String.valueOf(person.getId()))
                .child(String.valueOf(getDateString(date)))
                .orderByKey()
                .addListenerForSingleValueEvent(listener);
    }

    /**
     * Insert the daily steps to the person's intra-day activity list. Then perform the listener.
     * @param person
     * @param date
     * @param dailySteps
     */
    public void insertIntradaySteps(Person person, Date date, List<Integer> dailySteps,
                                    onDataUploadListener onDataUploadListener) {
        int numDays = dailySteps.size();
        List<FitnessSample> samples = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        for (int i = 0; i < numDays; i++) {
            samples.add(new OneDayFitnessSample(cal.getTime(), dailySteps.get(i)));
            cal.add(Calendar.MINUTE, 1);
        }

        insertIntradayFitness(person, samples, onDataUploadListener);
    }

    private void insertIntradayFitness(Person person, List<FitnessSample> samples,
                                      onDataUploadListener onDataUploadListener) {
        DatabaseReference ref = this.firebaseDbRef
                .child(FIREBASE_PATH_INTRADAY)
                .child(String.valueOf(person.getId()));

        Map<String, Object> personIntradayFitnessMap = new HashMap<>();
        for (FitnessSample sample : samples) {
            String subPath = String.format(FIREBASE_PATH_INTRADAY_CHILD,
                    getDateString(sample.getDate()), sample.getTimestamp());
            personIntradayFitnessMap.put(subPath, sample);
        }
        ref.updateChildren(personIntradayFitnessMap);

        onDataUploadListener.onSuccess();
    }

    /**
     * Update a person's daily fitness data using the intraday data from the database.
     * @param person
     * @param date
     * @param onDataUploadListener
     */
    public void updateDailyFitness(final Person person, Date date,
                                   final onDataUploadListener onDataUploadListener) {
        this.firebaseDbRef
                .child(FIREBASE_PATH_INTRADAY)
                .child(String.valueOf(person.getId()))
                .orderByKey()
                .startAt(getDateString(date))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //Log.d("SWELL", dataSnapshot.toString());
                        //Log.d("SWELL", getDailyFitnessFromIntraday(dataSnapshot).toString());
                        insertDailyFitness(
                                person,
                                getDailyFitnessFromIntraday(dataSnapshot),
                                onDataUploadListener);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        onDataUploadListener.onFailed();
                    }
                });
    }

    private void insertDailyFitness(Person person, List<FitnessSample> samples,
                                    onDataUploadListener onDataUploadListener) {
        DatabaseReference ref = this.firebaseDbRef
                .child(FIREBASE_PATH_DAILY)
                .child(String.valueOf(person.getId()));
        Map<String, Object> personDailyFitnessMap = new HashMap<>();
        for (FitnessSample sample : samples) {
            String subPath = String.format(
                    FIREBASE_PATH_DAILY_CHILD, getDateString(sample.getDate()));
            personDailyFitnessMap.put(subPath, sample);
        }
        ref.updateChildren(personDailyFitnessMap);

        onDataUploadListener.onSuccess();
    }

    /* PUBLIC HELPER METHODS */

    /**
     * Get {@link MultiDayFitness} instance using the given the {@link DataSnapshot} as well as the
     * start and end {@link Date}.
     * @param startDate
     * @param endDate
     * @param dataSnapshot
     * @return
     */
    public static MultiDayFitness getMultiDayFitness(Date startDate, Date endDate, DataSnapshot dataSnapshot) {
        List<OneDayFitnessSample> dailySamples = new ArrayList<>();
        if (dataSnapshot.exists()) {
            for (DataSnapshot sample : dataSnapshot.getChildren()) {
                dailySamples.add(sample.getValue(OneDayFitnessSample.class));
            }
        }
        return MultiDayFitness.newInstance(startDate, endDate, getListOfFitnessObjects(dailySamples));
    }

    /* ITERATOR HELPER METHODS */
    private List<FitnessSample> getDailyFitnessFromIntraday(@NonNull DataSnapshot dataSnapshot) {
        List<FitnessSample> samples = new ArrayList<>();
        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
            Date date = getDate(snapshot.getKey());
            int steps = 0;
            for (DataSnapshot intradaySnapshot : snapshot.getChildren()) {
                steps += intradaySnapshot.getValue(IntradayFitnessSample.class).getSteps();
            }
            Log.d("SWELL", String.format(
                    "Fitness data on %s: %d steps", date.toString(), steps));
            samples.add(new OneDayFitnessSample(date, steps));
        }
        return samples;
    }

    /* FITNESS SAMPLE CONVERSION METHODS */
    private static List<OneDayFitnessInterface> getListOfFitnessObjects (List<OneDayFitnessSample> fitnessSamples) {
        List<OneDayFitnessInterface> listOfFitnessObjects = new ArrayList<>();
        for (OneDayFitnessSample sample : fitnessSamples) {
            OneDayFitnessInterface oneDayFitness = OneDayFitness.newInstance(sample.getDate(),
                    sample.getSteps(), -1, -1, -1);
            listOfFitnessObjects.add(oneDayFitness);
        }
        return listOfFitnessObjects;
    }

    /* HELPER METHODS */
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

    public static Date getDate(String dateString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
            return sdf.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            Calendar cal = Calendar.getInstance();
            return cal.getTime();
        }
    }

    /* UNUSED METHODS */
    /* ITERATOR HELPER METHODS */
    /*
    private static List<IntradayFitnessSample> getIntradayFitnessSamples(DataSnapshot dataSnapshot) {
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
    */
    /*
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
    */
}
