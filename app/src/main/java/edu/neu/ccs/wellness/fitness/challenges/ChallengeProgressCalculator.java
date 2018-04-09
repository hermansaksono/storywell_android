package edu.neu.ccs.wellness.fitness.challenges;

import java.time.DateTimeException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
        return 0;
    }

    @Override
    public float getOverallGroupProgressByDate(Date date) throws DateTimeException {
        return 0;
    }
}
