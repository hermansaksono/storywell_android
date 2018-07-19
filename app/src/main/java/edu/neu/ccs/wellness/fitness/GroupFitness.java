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
    private Map<Person, MultiDayFitnessInterface> groupFitnessData;


    private GroupFitness(Map<Person, MultiDayFitnessInterface> personMultiDayFitnessMap ){
        this.groupFitnessData = personMultiDayFitnessMap;
    }

    public static GroupFitness newInstance(Map<Person, MultiDayFitnessInterface> personMultiDayFitnessMap){
        return new GroupFitness(personMultiDayFitnessMap);
    }


    @Override
    public MultiDayFitnessInterface getAPersonMultiDayFitness(Person person)
            throws PersonDoesNotExistException {
        if (groupFitnessData.containsKey(person)) {
            return groupFitnessData.get(person);
        } else {
            throw new PersonDoesNotExistException("Person does not exist.");
        }
    }

    @Override
    public Map<Person, MultiDayFitnessInterface> getGroupFitness() {
        return groupFitnessData;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Person, MultiDayFitnessInterface> entry : groupFitnessData.entrySet()) {
            sb
                    .append(entry.getKey().getName())
                    .append(" activities: \n")
                    .append(entry.getValue().toString())
                    .append("\n");
        }
        return sb.toString();
    }
}
