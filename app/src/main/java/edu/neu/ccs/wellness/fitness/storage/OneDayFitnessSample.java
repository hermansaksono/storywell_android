package edu.neu.ccs.wellness.fitness.storage;

import com.google.firebase.database.Exclude;

import java.util.Date;

import edu.neu.ccs.wellness.fitness.interfaces.FitnessSample;

/**
 * Created by hermansaksono on 6/24/18.
 */

public class OneDayFitnessSample implements FitnessSample {

    private static final String TO_STRING = "Fitness on %s: %d steps";
    public static final String KEY_TIMESTAMP = "timestamp";

    private long timestamp = 0;
    private int steps = 0;

    public OneDayFitnessSample() {}

    public OneDayFitnessSample(Date date, int steps) {
        this.timestamp = date.getTime();
        this.steps = steps;
    }

    @Override @Exclude
    public Date getDate() {
        return new Date(timestamp);
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public int getSteps() {
        return this.steps;
    }

    @Override
    public String toString() {
        return String.format(TO_STRING, this.getDate().toString(), this.getSteps());
    }
}
