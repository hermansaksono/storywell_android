package edu.neu.ccs.wellness.trackers;

import java.util.GregorianCalendar;

import edu.neu.ccs.wellness.trackers.callback.FetchActivityListener;

/**
 * Created by hermansaksono on 7/25/18.
 */

public interface StepsTrackingDevice {
    /**
     * Fetch steps count data from the device.
     *
     * @param startTime             Determines the start time of the activity data that will be
     *                              fetched.
     * @param fetchActivityListener This listener will take care of the data once the MI Band
     *                              completed the request.
     */
    void fetchActivityData(GregorianCalendar startTime, FetchActivityListener fetchActivityListener);
}
