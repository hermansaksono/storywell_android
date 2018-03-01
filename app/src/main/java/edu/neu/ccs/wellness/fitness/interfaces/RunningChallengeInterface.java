package edu.neu.ccs.wellness.fitness.interfaces;

import java.util.List;

import edu.neu.ccs.wellness.fitness.challenges.ChallengeProgress;

/**
 * Created by RAJ on 2/19/2018.
 */

public interface RunningChallengeInterface {

    boolean isCurrentlyRunning();
    String getText();
    String getSubText();
    String getTotalDuration();
    String getStartDate();
    String getEndDate();
    int getLevelId();
    int getLevelOrder();
    List<ChallengeProgress> getChallengeProgress();

}
