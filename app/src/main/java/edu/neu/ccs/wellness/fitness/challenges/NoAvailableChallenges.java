package edu.neu.ccs.wellness.fitness.challenges;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.neu.ccs.wellness.fitness.interfaces.AvailableChallengesInterface;

/**
 * Created by hermansaksono on 10/16/17.
 */

public class NoAvailableChallenges implements AvailableChallengesInterface {

    private static final String TEXT = "";
    private static final String SUBTEXT = "";
    private static final List<UnitChallenge> CHALLENGES = new ArrayList<>();

    public NoAvailableChallenges() { }

    @Override
    public String getText() { return TEXT; }

    @Override
    public String getSubtext() { return SUBTEXT; }

    @Override
    public String toString() {
        return "Can't find available challenges";
    }

    @Override
    public List<UnitChallenge> getChallenges() {
        return CHALLENGES;
    }

    @Override
    public Map<String, List<UnitChallenge>> getChallengesByPerson() {
        return null;
    }
}
