package edu.neu.ccs.wellness.storytelling.resolutionview;

/**
 * Created by hermansaksono on 2/25/19.
 */

public class ResolutionStatus {
    /**
     * No resolution has been decided.
     */
    public static final int UNSTARTED = 0;

    /**
     * Resolution has been decided, but the user hasn't proceed to the next step. I.e., user hasn't
     * seen the final outcome of the resolution, or hasn't proceeded from the resolution outcome.
     */
    public static final int DETERMINED = 1;

    /**
     * User has completed the resolution.
     */
    public static final int EXECUTED = 2;
}
