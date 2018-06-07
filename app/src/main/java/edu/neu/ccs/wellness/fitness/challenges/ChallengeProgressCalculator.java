package edu.neu.ccs.wellness.fitness.challenges;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import edu.neu.ccs.wellness.fitness.FitnessDataDoesNotExistException;
import edu.neu.ccs.wellness.fitness.GroupFitness;
import edu.neu.ccs.wellness.fitness.interfaces.ChallengeProgressCalculatorInterface;
import edu.neu.ccs.wellness.fitness.interfaces.GroupFitnessInterface;
import edu.neu.ccs.wellness.fitness.interfaces.MultiDayFitnessInterface;
import edu.neu.ccs.wellness.fitness.interfaces.OneDayFitnessInterface;
import edu.neu.ccs.wellness.fitness.interfaces.UnitChallengeInterface;
import edu.neu.ccs.wellness.people.Person;
import edu.neu.ccs.wellness.people.PersonDoesNotExistException;

/**
 * Created by hermansaksono on 4/4/18.sp
 */

public class ChallengeProgressCalculator implements ChallengeProgressCalculatorInterface {

    private UnitChallengeInterface challenge;
    private GroupFitnessInterface groupFitness;

    public ChallengeProgressCalculator(UnitChallengeInterface challenge, GroupFitnessInterface groupFitness) {
        this.groupFitness = groupFitness;
        this.challenge = challenge;
    }

    ChallengeProgressCalculator(RunningChallenge runningChallenge, GroupFitness groupFitness) {
        this.groupFitness = groupFitness;
        this.challenge = runningChallenge.getUnitChallenge();
    }

    @Override
    public Map<Date, Float> getPersonProgress(Person person) throws PersonDoesNotExistException {
        MultiDayFitnessInterface multiDayFitness = this.groupFitness.getAPersonMultiDayFitness(person);

        Map<Date, Float> progressMap = new HashMap<>();
        double goal = this.challenge.getGoal();

        for(OneDayFitnessInterface oneDayFitness : multiDayFitness.getDailyFitness()) {
            float progress = (float) (oneDayFitness.getSteps() / goal);
            progressMap.put(oneDayFitness.getDate(), progress);
        }
        return progressMap;
    }

    @Override
    public float getPersonProgressByDate(Person person, Date date)
            throws PersonDoesNotExistException, FitnessDataDoesNotExistException {
        Map<Date, Float> progressMap = this.getPersonProgress(person);
        if (progressMap.containsKey(date)) {
            return progressMap.get(date);
        } else {
            throw new FitnessDataDoesNotExistException("Can't find Fitness data on that Date");
        }
    }

    @Override
    public Map<Date, Float> getGroupProgress() {
        int numPeople = this.groupFitness.getGroupFitness().size();
        Map<Date, Float> groupProgress = new HashMap<>();

        for (MultiDayFitnessInterface multiDayFitness : groupFitness.getGroupFitness().values()) {
            for (OneDayFitnessInterface oneDayFitness : multiDayFitness.getDailyFitness()) {
                float value = 0.0f;
                Date date = oneDayFitness.getDate();
                if (groupProgress.containsKey(date)) {
                    value = groupProgress.get(date);
                }
                value += oneDayFitness.getSteps() / numPeople;
                groupProgress.put(date, value);
            }
        }

        return groupProgress;
    }

    @Override
    public float getGroupProgressByDate(Date date) throws FitnessDataDoesNotExistException {
        /*
        int numPeople = this.groupFitness.getGroupFitness().size();
        double overallGroupProgress = 0;
        for (Map.Entry<Person, MultiDayFitnessInterface> entry : groupFitness.getGroupFitness().entrySet()) {
            overallGroupProgress += getOneDayFitness(entry.getValue(), date);
        }
        return (float) (overallGroupProgress / (numPeople * this.challenge.getGoal()));
        */
        Map<Date, Float> groupProgress = this.getGroupProgress();
        if (groupProgress.containsKey(date)) {
            return groupProgress.get(date);
        } else {
            throw new FitnessDataDoesNotExistException("Can't find Fitness data on that date");
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

    /* HELPER METHODS */
    /*
    private static double getAverageMultiDayFitness(MultiDayFitnessInterface multiDayFitness) {
        double overallFitness = 0.0;
        int numDays = 0;
        for(OneDayFitnessInterface oneDayFitness : multiDayFitness.getDailyFitness()) {
            overallFitness += oneDayFitness.getSteps();
            numDays += 1;
        }
        return overallFitness / numDays;
    }

    private static double getOneDayFitness(MultiDayFitnessInterface multiDayFitness, Date date) {
        for(OneDayFitnessInterface oneDayFitness : multiDayFitness.getDailyFitness()) {
            if (date.equals(oneDayFitness.getDate())) {
                return oneDayFitness.getSteps();
            }
        }
        return 0.0;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public float getGroupProgressByDate(Date date) {
        //TODO throw exception
        //TODO RK date does not exist exception?
        // TODO Requires API level

        String stringDate = date.toString();
        DateFormat formatter = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy");
        String format = formatter.format(date);
        if(!format.equals(stringDate)) throw new Exception("Date is not formatted correctly");


        int numberOfPersons = this.groupFitness.getPersonMultiDayFitnessMap().size();
        Iterator iterator = groupFitness.getPersonMultiDayFitnessMap().entrySet().iterator();
        float personProgress = 0;
        boolean containsDate = true;
        float overallGroupProgressByDate = 0;
        for(int i = 0; i<numberOfPersons; i++){
            Map.Entry<Person, MultiDayFitness> entry = (Map.Entry<Person, MultiDayFitness>) iterator.next();
            HashMap<Date, Float> map = null;
            try {
                map = (HashMap<Date, Float>) getPersonProgress(entry.getKey());
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
    */
}
