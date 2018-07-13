package edu.neu.ccs.wellness.miband2.listeners;

import java.util.Calendar;
import java.util.List;

/**
 * Created by hermansaksono on 6/25/18.
 */

public interface FetchActivityListener {
    void OnFetchComplete(Calendar startDate, List<Integer> steps);
}
