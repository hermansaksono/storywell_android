package edu.neu.ccs.wellness.fitness;

import java.util.Map;

import edu.neu.ccs.wellness.fitness.interfaces.GroupFitnessInterface;
import edu.neu.ccs.wellness.fitness.interfaces.MultiDayFitnessInterface;
import edu.neu.ccs.wellness.people.Person;
import edu.neu.ccs.wellness.people.PersonDoesNotExistException;

/**
 * Created by hermansaksono on 3/20/18.
 */

public class GroupFitness implements GroupFitnessInterface {

    //PRIVATE MEMBERS
    private Map<Person, MultiDayFitnessInterface> personMultiDayFitnessMap;


    private GroupFitness(Map<Person, MultiDayFitnessInterface> personMultiDayFitnessMap ){
        this.personMultiDayFitnessMap = personMultiDayFitnessMap;
    }

    public static GroupFitness newInstance(Map<Person, MultiDayFitnessInterface> personMultiDayFitnessMap){
        return new GroupFitness(personMultiDayFitnessMap);
    }


    @Override
    public MultiDayFitnessInterface getAPersonMultiDayFitness(Person person)
            throws PersonDoesNotExistException {
        if (personMultiDayFitnessMap.containsKey(person)) {
            return personMultiDayFitnessMap.get(person);
        } else {
            throw new PersonDoesNotExistException();
        }
    }

    @Override
    public Map<Person, MultiDayFitnessInterface> getGroupFitness() {
        return personMultiDayFitnessMap;
    }
}
