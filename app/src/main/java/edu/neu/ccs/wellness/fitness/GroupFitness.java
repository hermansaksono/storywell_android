package edu.neu.ccs.wellness.fitness;

import android.content.Context;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.CheckedOutputStream;

import edu.neu.ccs.wellness.fitness.interfaces.GroupFitnessInterface;
import edu.neu.ccs.wellness.fitness.interfaces.MultiDayFitnessInterface;
import edu.neu.ccs.wellness.people.GroupInterface;
import edu.neu.ccs.wellness.people.Person;
import edu.neu.ccs.wellness.people.PersonDoesNotExistException;

/**
 * Created by hermansaksono on 3/20/18.
 */

public class GroupFitness implements GroupFitnessInterface {

    //PRIVATE MEMBERS
    private HashMap<Person, MultiDayFitness> personMultiDayFitnessMap;
    private Context context;


    private GroupFitness(Context context, HashMap<Person, MultiDayFitness> personMultiDayFitnessMap ){
        this.context = context;
        this.personMultiDayFitnessMap = personMultiDayFitnessMap;
    }

    public static GroupFitness create(Context context, HashMap<Person, MultiDayFitness> personMultiDayFitnessMap){
        return new GroupFitness(context, personMultiDayFitnessMap);
    }


    @Override
    public MultiDayFitnessInterface getAPersonMultiDayFitness(Person person)
            throws PersonDoesNotExistException {
                if(personMultiDayFitnessMap.containsKey(person)){
                    return personMultiDayFitnessMap.get(person);
                }
                else{
                    new PersonDoesNotExistException();
                }
                return null;
    }

    private void setPersonMultiDayFitnessMap(JSONArray jsonArray){

    }

    public Map<Person, MultiDayFitness> getPersonMultiDayFitnessMap(){
        return this.personMultiDayFitnessMap;
    }
}
