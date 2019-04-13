package edu.neu.ccs.wellness.fitness.interfaces;

import java.util.List;
import java.util.Map;

import edu.neu.ccs.wellness.fitness.challenges.UnitChallenge;

/**
 * Created by hermansaksono on 10/16/17.
 */

public interface AvailableChallengesInterface {

    String getText();

    String getSubtext();

    List<UnitChallenge> getChallenges();

    Map<String, List<UnitChallenge>> getChallengesByPerson();

}
