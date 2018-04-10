package edu.neu.ccs.wellness.fitness.challenges;

import java.time.DateTimeException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import edu.neu.ccs.wellness.fitness.GroupFitness;
import edu.neu.ccs.wellness.fitness.MultiDayFitness;
import edu.neu.ccs.wellness.fitness.OneDayFitness;
import edu.neu.ccs.wellness.fitness.interfaces.ChallengeProgressCalculatorInterface;
import edu.neu.ccs.wellness.fitness.interfaces.MultiDayFitnessInterface;
import edu.neu.ccs.wellness.fitness.interfaces.OneDayFitnessInterface;
import edu.neu.ccs.wellness.people.Person;
import edu.neu.ccs.wellness.people.PersonDoesNotExistException;

/**
 * Created by hermansaksono on 4/4/18.
 */

public class ChallengeProgressCalculator implements ChallengeProgressCalculatorInterface {

    private Challenge challenge;
    private RunningChallenge runningChallenge;
    private GroupFitness groupFitness;

    private MultiDayFitnessInterface multiDayFitness;

    ChallengeProgressCalculator(Challenge challenge, GroupFitness groupFitness) {
        this.challenge = challenge;
        this.groupFitness = groupFitness;
    }

    public ChallengeProgressCalculator(RunningChallenge challenge, GroupFitness groupFitness) {
        this.runningChallenge = challenge;
        this.groupFitness = groupFitness;
    }

    @Override
    public Map<Date, Float> getProgressFromPerson(Person person) throws PersonDoesNotExistException {
        multiDayFitness = this.groupFitness.getAPersonMultiDayFitness(person);
        double goal = this.runningChallenge.getChallengeProgress().get(0).getGoal();
        HashMap<Date, Float> progressMap = new HashMap<>();
        ArrayList<OneDayFitnessInterface> oneDayFitnesses = (ArrayList<OneDayFitnessInterface>) multiDayFitness.getDailyFitness();
        for(int i = 0; i<oneDayFitnesses.size(); i++){
            //TODO RK need to check the math
            progressMap.put(oneDayFitnesses.get(i).getDate(), (float) (oneDayFitnesses.get(i).getSteps()/goal));
        }
        return progressMap;
    }

    @Override
    public float getOverallGroupProgress() {
        int numberOfPersons = this.groupFitness.getPersonMultiDayFitnessMap().size();
        MultiDayFitness multiDayFitness = null;
        float overallGroupProgress = 0;
        float totalSteps = 0;
        Iterator iterator = groupFitness.getPersonMultiDayFitnessMap().entrySet().iterator();
        for(int i = 0; i<numberOfPersons; i++){
            Map.Entry<Person, MultiDayFitness> entry = (Map.Entry<Person, MultiDayFitness>) iterator.next();
            multiDayFitness = entry.getValue();
            ArrayList<OneDayFitnessInterface> oneDayFitnessArrayList = (ArrayList<OneDayFitnessInterface>) multiDayFitness.getDailyFitness();
            for(int j = 0; j<oneDayFitnessArrayList.size(); j++){
               totalSteps += oneDayFitnessArrayList.get(j).getSteps();
            }
        }
        overallGroupProgress = totalSteps/numberOfPersons;
        return overallGroupProgress;
    }

    @Override
    public float getOverallGroupProgressByDate(Date date) throws DateTimeException {
        int numberOfPersons = this.groupFitness.getPersonMultiDayFitnessMap().size();
        Iterator iterator = groupFitness.getPersonMultiDayFitnessMap().entrySet().iterator();
        float personProgress = 0;
        float overallGroupProgressByDate = 0;
        for(int i = 0; i<numberOfPersons; i++){
            Map.Entry<Person, MultiDayFitness> entry = (Map.Entry<Person, MultiDayFitness>) iterator.next();
            HashMap<Date, Float> map = null;
            try {
                map = (HashMap<Date, Float>) getProgressFromPerson(entry.getKey());
            } catch (PersonDoesNotExistException e) {
                e.printStackTrace();
            }
            if(map.containsKey(date)) {
                personProgress += map.get(date);
            }
        }
        overallGroupProgressByDate = personProgress/numberOfPersons;
        return overallGroupProgressByDate;
    }
}
