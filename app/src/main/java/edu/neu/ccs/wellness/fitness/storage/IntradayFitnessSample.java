package edu.neu.ccs.wellness.fitness.storage;

import com.google.firebase.database.Exclude;

import java.util.Date;

import edu.neu.ccs.wellness.fitness.interfaces.FitnessSample;

/**
 * Created by hermansaksono on 6/24/18.
 */

public class IntradayFitnessSample implements FitnessSample {

    private static final String TO_STRING = "Intraday fitness on %s: %d steps";
    private long timestamp = 0;
    private int steps = 0;

    public IntradayFitnessSample() {}

    public IntradayFitnessSample(long timestamp, int steps) {
        this.timestamp = timestamp;
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
