package edu.neu.ccs.wellness.fitness.challenges;

/**
 * Created by RAJ on 2/19/2018.
 */

public class ChallengeProgress {

    int personId;
    double goal;
    String unit;
    String unitDuration;

    ChallengeProgress(int personId, double goal, String unit, String unitDuration){
        this.personId = personId;
        this.goal = goal;
        this.unit = unit;
        this.unitDuration = unitDuration;
    }


    public int getPersonId() {
        return personId;
    }

    public void setPersonId(int personId) {
        this.personId = personId;
    }

    public double getGoal() {
        return goal;
    }

    public void setGoal(double goal) {
        this.goal = goal;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getUnitDuration() {
        return unitDuration;
    }

    public void setUnitDuration(String unitDuration) {
        this.unitDuration = unitDuration;
    }
}
