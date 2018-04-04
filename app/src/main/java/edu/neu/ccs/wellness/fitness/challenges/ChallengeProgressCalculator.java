package edu.neu.ccs.wellness.fitness.challenges;

import java.time.DateTimeException;
import java.util.Date;
import java.util.Map;

import edu.neu.ccs.wellness.fitness.GroupFitness;
import edu.neu.ccs.wellness.fitness.interfaces.ChallengeProgressCalculatorInterface;
import edu.neu.ccs.wellness.people.Person;
import edu.neu.ccs.wellness.people.PersonDoesNotExistException;

/**
 * Created by hermansaksono on 4/4/18.
 */

public class ChallengeProgressCalculator implements ChallengeProgressCalculatorInterface {

    ChallengeProgressCalculator(Challenge challenge, GroupFitness groupFitness) {
        // TODO
    }

    ChallengeProgressCalculator(RunningChallenge challenge, GroupFitness groupFitness) {
        // TODO
    }

    @Override
    public Map<Date, Float> getProgressFromPerson(Person person) throws PersonDoesNotExistException {
        return null;
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
