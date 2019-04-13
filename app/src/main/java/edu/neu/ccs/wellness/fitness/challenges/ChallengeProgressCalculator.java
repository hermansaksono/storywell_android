package edu.neu.ccs.wellness.fitness.challenges;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import edu.neu.ccs.wellness.fitness.interfaces.FitnessException;
import edu.neu.ccs.wellness.fitness.GroupFitness;
import edu.neu.ccs.wellness.fitness.interfaces.ChallengeProgressCalculatorInterface;
import edu.neu.ccs.wellness.fitness.interfaces.GroupFitnessInterface;
import edu.neu.ccs.wellness.fitness.interfaces.MultiDayFitnessInterface;
import edu.neu.ccs.wellness.fitness.interfaces.OneDayFitnessInterface;
import edu.neu.ccs.wellness.fitness.interfaces.RunningChallengeInterface;
import edu.neu.ccs.wellness.fitness.interfaces.UnitChallengeInterface;
import edu.neu.ccs.wellness.people.Person;
import edu.neu.ccs.wellness.people.PersonDoesNotExistException;

/**
 * Created by hermansaksono on 4/4/18.sp
 */

public class ChallengeProgressCalculator implements ChallengeProgressCalculatorInterface {

    // private UnitChallengeInterface challenge;
    private GroupFitnessInterface groupFitness;
    //private RunningChallengeInterface runningChallenge;
    private Map<Integer, Float> personGoalMap;

    /*
    public ChallengeProgressCalculator(
    UnitChallengeInterface challenge, GroupFitnessInterface groupFitness) {
        this.groupFitness = groupFitness;
        this.challenge = challenge;
    }
    */

    public ChallengeProgressCalculator(
            RunningChallengeInterface runningChallenge, GroupFitnessInterface groupFitness) {
        this.groupFitness = groupFitness;
        // this.challenge = runningChallenge.getUnitChallenge();
        //this.runningChallenge = runningChallenge;
        this.personGoalMap = new HashMap<>();

        for (ChallengeProgress challengeProgress: runningChallenge.getChallengeProgress()) {
            int personId = challengeProgress.getPersonId();
            float personGoal = (float) challengeProgress.getGoal();
            personGoalMap.put(personId, personGoal);
        }
    }

    @Override
    public Map<Date, Float> getPersonProgress(Person person) throws PersonDoesNotExistException {
        float goal;
        if (this.personGoalMap.containsKey(person.getId())) {
            goal = this.personGoalMap.get(person.getId());
        } else {
            throw new PersonDoesNotExistException(
                    "Person with id " + person.getId() + "doesn't exist");
        }

        MultiDayFitnessInterface multiDayFitness = this.groupFitness
                .getAPersonMultiDayFitness(person);
        Map<Date, Float> progressMap = new HashMap<>();
        //double goal = this.challenge.getGoal();

        for(OneDayFitnessInterface oneDayFitness : multiDayFitness.getDailyFitness()) {
            float progress = oneDayFitness.getSteps() / goal;
            progressMap.put(oneDayFitness.getDate(), progress);
        }

        return progressMap;
    }

    @Override
    public float getPersonProgressByDate(Person person, Date date)
            throws PersonDoesNotExistException, FitnessException {
        Map<Date, Float> progressMap = this.getPersonProgress(person);
        if (progressMap.containsKey(date)) {
            return progressMap.get(date);
        } else {
            throw new FitnessException("Can't find Fitness data on Date " + date.toString());
        }
    }

    @Override
    public Map<Date, Float> getGroupProgress() {
        /*
        int numPeople = this.groupFitness.getGroupFitness().size();
        double goal = this.challenge.getGoal();
        Map<Date, Float> groupProgress = new HashMap<>();

        for (MultiDayFitnessInterface multiDayFitness : groupFitness.getGroupFitness().values()) {
            for (OneDayFitnessInterface oneDayFitness : multiDayFitness.getDailyFitness()) {
                float value = 0.0f;
                Date date = oneDayFitness.getDate();
                if (groupProgress.containsKey(date)) {
                    value = groupProgress.get(date);
                }
                value += oneDayFitness.getSteps() / (numPeople * goal);
                groupProgress.put(date, value);
            }
        }

        return groupProgress;
        */
        int numPeople = this.groupFitness.getGroupFitness().size();
        Map<Date, Float> groupProgress = new HashMap<>();

        for (Person person : groupFitness.getGroupFitness().keySet()) {
            float goal = personGoalMap.get(person.getId());
            MultiDayFitnessInterface multiDayFitness = groupFitness.getGroupFitness().get(person);

            for (OneDayFitnessInterface oneDayFitness : multiDayFitness.getDailyFitness()) {
                float value = 0.0f;
                Date date = oneDayFitness.getDate();
                if (groupProgress.containsKey(date)) {
                    value = groupProgress.get(date);
                }
                value += oneDayFitness.getSteps() / (numPeople * goal);
                groupProgress.put(date, value);
            }
        }

        return groupProgress;
    }

    @Override
    public float getGroupProgressByDate(Date date) throws FitnessException {
        Map<Date, Float> groupProgress = this.getGroupProgress();
        if (groupProgress.containsKey(date)) {
            return groupProgress.get(date);
        } else {
            throw new FitnessException("Can't find Fitness data on that date");
        }
    }

    @Override
    public float getAveragedGroupProgress() {
        /*
        int numPeople = this.groupFitness.getGroupFitness().size();
        double overallGroupProgress = 0;
        for(MultiDayFitnessInterface multiDayFitness : this.groupFitness.getGroupFitness().values()){
            overallGroupProgress += getAverageMultiDayFitness(multiDayFitness);
        }
        return (float) (overallGroupProgress / (numPeople * this.challenge.getGoal()));
        */
        float totalProgress = 0.0f;
        int numDays = 0;
        for(Float oneDayProgress : this.getGroupProgress().values()) {
            totalProgress += oneDayProgress;
            numDays += 1;
        }
        return totalProgress / numDays;
    }
}
