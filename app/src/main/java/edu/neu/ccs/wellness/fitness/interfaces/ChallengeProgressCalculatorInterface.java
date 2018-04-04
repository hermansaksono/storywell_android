package edu.neu.ccs.wellness.fitness.interfaces;

import java.time.DateTimeException;
import java.util.Date;
import java.util.Map;

import edu.neu.ccs.wellness.people.Person;
import edu.neu.ccs.wellness.people.PersonDoesNotExistException;

/**
 * Created by hermansaksono on 4/4/18.
 */

public interface ChallengeProgressCalculatorInterface {

    /**
     * Given a Person, returns the daily ratio of progress [0.0 - 1.0].
     * @param person The Person of interest
     * @return A Map with the Date as key, and float progress as the value.
     * @throws PersonDoesNotExistException when Person is not a member of the Group.
     */
    Map<Date, Float> getProgressFromPerson(Person person) throws PersonDoesNotExistException;

    /**
     * Get the overall progress. Formula: sum all individual progress and divide by the number
     * of Persons in Group
     * @return Overall progress [0.0 - 1.0]
     */
    float getOverallGroupProgress();

    /**
     * Get the overall progress on a paricular Date. Formula: sum all individual progress on a
     * particular Date and divide by the number of Persons in Group
     * @param date Date of interest
     * @return Overall progress [0.0 - 1.0] on a specific date
     * @throws DateTimeException if the Dates are beyond the start and end Dates
     */
    float getOverallGroupProgressByDate(Date date) throws DateTimeException;

}
