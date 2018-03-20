package edu.neu.ccs.wellness.fitness;

import edu.neu.ccs.wellness.fitness.interfaces.GroupFitnessInterface;
import edu.neu.ccs.wellness.fitness.interfaces.MultiDayFitnessInterface;
import edu.neu.ccs.wellness.people.GroupInterface;
import edu.neu.ccs.wellness.people.Person;
import edu.neu.ccs.wellness.people.PersonDoesNotExistException;

/**
 * Created by hermansaksono on 3/20/18.
 */

public class GroupFitness implements GroupFitnessInterface {

    @Override
    public MultiDayFitnessInterface getAPersonMultiDayFitness(Person person)
            throws PersonDoesNotExistException {
        return null;
    }
}
