package edu.neu.ccs.wellness.fitness.interfaces;

import java.util.Date;

/**
 * Created by hermansaksono on 6/24/18.
 */

public interface FitnessSample {

    Date getDate();

    long getTimestamp();

    void setTimestamp(long timestamp);

    int getSteps();
}
