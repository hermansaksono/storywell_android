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
import java.util.List;
import java.util.Locale;

import edu.neu.ccs.wellness.fitness.MultiDayFitness;
import edu.neu.ccs.wellness.fitness.OneDayFitness;
import edu.neu.ccs.wellness.fitness.interfaces.FitnessSample;
import edu.neu.ccs.wellness.fitness.interfaces.OneDayFitnessInterface;
import edu.neu.ccs.wellness.people.Person;

/**
 * Created by hermansaksono on 6/24/18.
 */

public class FitnessRepository {

    public static final String FIREBASE_PATH_DAILY = "person_daily_fitness";
    public static final String FIREBASE_PATH_INTRADAY = "person_intraday_fitness";
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    private DatabaseReference firebaseDbRef;

    public FitnessRepository() {
        this.firebaseDbRef = FirebaseDatabase.getInstance().getReference();
    }

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

    public void insertDailyFitness(Person person, List<FitnessSample> samples,
                                   onDataUploadListener onDataUploadListener) {
        DatabaseReference ref = this.firebaseDbRef
                .child(FIREBASE_PATH_DAILY)
                .child(String.valueOf(person.getId()));
        for (FitnessSample sample : samples) {
            ref.child(getDateString(sample.getDate())).setValue(sample);
        }
        onDataUploadListener.onSuccess();
    }

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

    /**
     * Insert the daily steps to the person's intra-day activity list.
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

        insertIntradayFitness(person, date, samples, onDataUploadListener);
    }

    public void insertIntradayFitness(Person person, Date date, List<FitnessSample> samples,
                                      onDataUploadListener onDataUploadListener) {
        DatabaseReference ref = this.firebaseDbRef
                .child(FIREBASE_PATH_INTRADAY)
                .child(String.valueOf(person.getId()));
        for (FitnessSample sample : samples) {
            ref.child(getDateString(sample.getDate()))
                    .child(String.valueOf(sample.getTimestamp()))
                    .setValue(sample);
        }
        onDataUploadListener.onSuccess();
    }

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
                        insertDailyFitness(person, getDailyFitnessFromIntraday(dataSnapshot), onDataUploadListener);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        onDataUploadListener.onFailed();
                    }
                });
    }

    public static MultiDayFitness getMultiDayFitness(Date startDate, Date endDate, DataSnapshot dataSnapshot) {
        List<OneDayFitnessSample> dailySamples = new ArrayList<>();
        if (dataSnapshot.exists()) {
            for (DataSnapshot sample : dataSnapshot.getChildren()) {
                dailySamples.add(sample.getValue(OneDayFitnessSample.class));
            }
        }
        return MultiDayFitness.newInstance(startDate, endDate, getListOfFitnessObjects(dailySamples));
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

    /* ITERATOR HELPER METHODS */
    private static List<IntradayFitnessSample> getIntradayFitnessSamples(DataSnapshot dataSnapshot) {
        List<IntradayFitnessSample> intradaySamples = new ArrayList<>();
        if (dataSnapshot.exists()) {
            for (DataSnapshot sample : dataSnapshot.getChildren()) {
                intradaySamples.add(sample.getValue(IntradayFitnessSample.class));
            }
        }
        return intradaySamples;
    }

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
}
