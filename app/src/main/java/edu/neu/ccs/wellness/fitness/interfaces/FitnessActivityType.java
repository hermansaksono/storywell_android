package edu.neu.ccs.wellness.fitness.interfaces;

/**
 * Created by hermansaksono on 2/5/18.
 */

public enum FitnessActivityType {
    UNKNOWN,
    STEPS,
    MINUTES,
    DISTANCE;

    public static FitnessActivityType getChallengeUnit(String unitString) {
        if (unitString.equals("steps")) {
            return FitnessActivityType.STEPS;
        } else if (unitString.equals("minutes")) {
            return FitnessActivityType.MINUTES;
        } else if (unitString.equals("distance")) {
            return FitnessActivityType.DISTANCE;
        } else {
            return FitnessActivityType.UNKNOWN;
        }
    }

}