package edu.neu.ccs.wellness.trackers.callback;

import java.util.Calendar;
import java.util.List;

/**
 * Created by hermansaksono on 6/25/18.
 */

public interface FetchActivityListener {
    /**
     * A listener to handle activity fethcing from wellness tracker
     * @param startDate Tells the date of the first entry in steps.
     * @param expectedSamples Tells how many samples were promised by the device. The actual
     *                        length of the steps data might be smaller than what is promised.
     * @param steps List of minute-by-minute steps data starting from startDate to the last data
     *              available on the device.
     */
    void OnFetchComplete(Calendar startDate, int expectedSamples, List<Integer> steps);

    void OnFetchProgress(int index, int numData);
}
