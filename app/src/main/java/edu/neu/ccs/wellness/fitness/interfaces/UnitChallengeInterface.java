package edu.neu.ccs.wellness.fitness.interfaces;

import java.util.Date;

/**
 * Created by hermansaksono on 3/16/18.
 */

public interface UnitChallengeInterface {

    String getText();

    float getGoal();

    String getUnit();

    String getJsonText();

    Date getStartDate();

    void setStartDate(Date date);

    // Date getEndDate();
}
