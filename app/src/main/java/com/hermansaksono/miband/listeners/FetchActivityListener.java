package com.hermansaksono.miband.listeners;

import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by hermansaksono on 6/25/18.
 */

public interface FetchActivityListener {
    void OnFetchComplete(GregorianCalendar startDate, List<Integer> steps);
}
