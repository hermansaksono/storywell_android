package edu.neu.ccs.wellness.fitness.challenges;

import android.os.Build;
import android.support.annotation.RequiresApi;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
 * Created by hermansaksono on 4/4/18.sp
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
        //TODO add goal property
    }

    @Override
    public Map<Date, Float> getProgressFromPerson(Person person) throws PersonDoesNotExistException {
        multiDayFitness = this.groupFitness.getAPersonMultiDayFitness(person);
        if(multiDayFitness == null) throw new PersonDoesNotExistException();
        double goal = this.runningChallenge.getChallengeProgress().get(0).getGoal();
        HashMap<Date, Float> progressMap = new HashMap<>();
        ArrayList<OneDayFitnessInterface> oneDayFitnesses = (ArrayList<OneDayFitnessInterface>) multiDayFitness.getDailyFitness();
        for(int i = 0; i<oneDayFitnesses.size(); i++){
            progressMap.put(oneDayFitnesses.get(i).getDate(), (float) (oneDayFitnesses.get(i).getSteps()/goal));
        }
        return progressMap;
    }

    @Override
    public float getOverallGroupProgress() {
        int size = this.runningChallenge.getChallengeProgress().size();
        double overallGroupProgress = 0;
        for(ChallengeProgress challengeProgress : runningChallenge.getChallengeProgress()){
           double goal =  challengeProgress.getGoal();
           double sevenDayGoal = goal * 7;
           overallGroupProgress+=sevenDayGoal;
        }
        return (float) (overallGroupProgress/size);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public float getOverallGroupProgressByDate(Date date) throws DateTimeException {
        //TODO throw exception
        //TODO RK date does not exist exception?
        // TODO Requires API level

        String stringDate = date.toString();
        DateFormat formatter = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy");
        String format = formatter.format(date);
        if(!format.equals(stringDate)) throw new DateTimeException("Date is not formatted correctly");


        int numberOfPersons = this.groupFitness.getPersonMultiDayFitnessMap().size();
        Iterator iterator = groupFitness.getPersonMultiDayFitnessMap().entrySet().iterator();
        float personProgress = 0;
        boolean containsDate = true;
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
