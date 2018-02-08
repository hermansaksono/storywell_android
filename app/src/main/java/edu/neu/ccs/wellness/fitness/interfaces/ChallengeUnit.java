package edu.neu.ccs.wellness.fitness.interfaces;

/**
 * Created by hermansaksono on 2/5/18.
 */

public enum ChallengeUnit {
    UNKNOWN,
    STEPS,
    MINUTES,
    DISTANCE;

    public static ChallengeUnit getChallengeUnit(String unitString) {
        if (unitString.equals("steps")) {
            return ChallengeUnit.STEPS;
        } else if (unitString.equals("minutes")) {
            return ChallengeUnit.MINUTES;
        } else if (unitString.equals("distance")) {
            return ChallengeUnit.DISTANCE;
        } else {
            return ChallengeUnit.UNKNOWN;
        }
    }

}