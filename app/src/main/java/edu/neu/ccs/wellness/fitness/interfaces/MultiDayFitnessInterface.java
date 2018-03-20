package edu.neu.ccs.wellness.fitness.interfaces;

import java.util.Date;
import java.util.List;

/**
 * Created by hermansaksono on 3/20/18.
 */

public interface MultiDayFitnessInterface {

    /**
     * Get the start date of the multi-day fitness data
     * @return The Date of the first day of the multi-day fitness data
     */
    Date getStartDate();

    /**
     * Get the end date of the multi-day fitness data
     * @return The Date of the last day of a multi-day fitness data
     */
    Date getEndDate();

    /**
     * Get the number of days contained in this multi-day fitness data
     * @return The number of daya
     */
    int getNumDays();

    /**
     * Get the number of days that has elapsed from the start Date to today. For example, if start
     * Date is Jan 1st and today is Jan 6th, then this method will return 6.
     * @return The number of days that have elapsed including today
     */
    int getElapsedDays();

    /**
     * Get a (@link List) of (@link OneDayFitnessInterface) from the start Date to the end Date. The
     * length of the List is equal to getElapsedDays().
     * @return A list of (@link OneDayFitnessInterface).
     */
    List<OneDayFitnessInterface> getDailyFitness();
}
