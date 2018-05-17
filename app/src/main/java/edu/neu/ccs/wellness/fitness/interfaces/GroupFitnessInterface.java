package edu.neu.ccs.wellness.fitness.interfaces;

import edu.neu.ccs.wellness.people.GroupInterface;
import edu.neu.ccs.wellness.people.Person;
import edu.neu.ccs.wellness.people.PersonDoesNotExistException;

/**
 * Created by hermansaksono on 3/20/18.
 */

public interface GroupFitnessInterface {
    /**
     * Get a multi-day fitness data of a (@link Person) that belongs to a (@link GroupInterface)
     * @param person The Person of interest
     * @return A multi-day fitness data of that Person
     * @throws PersonDoesNotExistException if the Person is not a member the Group.
     */
    MultiDayFitnessInterface getAPersonMultiDayFitness(Person person)
            throws PersonDoesNotExistException;

}
