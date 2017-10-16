package edu.neu.ccs.wellness.storytelling.interfaces;

import java.util.Date;
import java.util.List;

/**
 * Created by hermansaksono on 10/16/17.
 */

public interface GroupChallengeInterface {

    public enum ChallengeStatus {
        UNINITIATED,
        AVAILABLE,
        RUNNING
    }

    public enum ChallengeUnit {
        UNKNOWN,
        STEPS,
        MINUTES,
        DISTANCE
    }

    public ChallengeStatus getStatus();

    public String getText();

    public String getSubtext();

    public List<PersonChallengeInterface> getAvailableChallenges()
            throws StorytellingException;

    public List<PersonChallengeInterface> getCurrentProgress()
            throws StorytellingException;
}
