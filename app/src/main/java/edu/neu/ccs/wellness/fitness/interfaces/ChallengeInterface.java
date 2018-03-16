package edu.neu.ccs.wellness.fitness.interfaces;

import java.util.Date;

/**
 * Created by hermansaksono on 3/16/18.
 */

public interface ChallengeInterface {

    String getText();

    String getSubText();

    String getTotalDuration();

    Date getStartDate();

    Date getEndDate();

    int getLevelId();

    int getLevelOrder();

    float getGoal();

    String getUnit();
}
