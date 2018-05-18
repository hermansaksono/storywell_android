package edu.neu.ccs.wellness.fitness.interfaces;

import java.util.Date;

/**
 * Created by hermansaksono on 3/20/18.
 */

public interface FitnessManagerInterface {

    /**
     * If there is no saved group fitness data in then internal storage XOR current local time
     * is after cacheExpiryDate, then make a GET request to the RestServer. Otherwise, load the data
     * from the internal storage. Note: the group information is stored in the RestServer object
     * that is passed during the object's creation.
     * @param startDate The start Date of the fitness activities
     * @param endDate The end Date of the fitness activities
     * @param cacheExpiry The timestamp of the local storage's expiration date.
     * @return The GroupFitnessInterface object representing the fitness activities that begins at
     * startDate and ends on endDate.
     */
    GroupFitnessInterface getMultiDayFitness(Date startDate, Date endDate, Date cacheExpiry);

    /**
     * If there is no saved group fitness data in then internal storage XOR current local time
     * is after the implementation's cacheExpiryDate, then make a GET request to the RestServer.
     * Otherwise, load the data from the internal storage. Note: the group information is stored
     * in the RestServer object that is passed during the object's creation.
     * @param startDate The start Date of the fitness activities
     * @param endDate The end Date of the fitness activities
     * @return The GroupFitnessInterface object representing the fitness activities that begins at
     * startDate and ends on endDate.
     */
    GroupFitnessInterface getMultiDayFitness(Date startDate, Date endDate);

}
