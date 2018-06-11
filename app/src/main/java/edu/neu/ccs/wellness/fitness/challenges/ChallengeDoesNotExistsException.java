package edu.neu.ccs.wellness.fitness.challenges;

/**
 * Created by hermansaksono on 6/11/18.
 */

public class ChallengeDoesNotExistsException extends Exception {
    public ChallengeDoesNotExistsException(String message) {
        super(message);
    }
}
