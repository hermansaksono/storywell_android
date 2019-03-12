package edu.neu.ccs.wellness.fitness.interfaces;

/**
 * Created by hermansaksono on 3/20/18.
 */

public class FitnessException extends Exception {

    public enum FitnessErrorType {
        UNSPECIFIED, NO_INTERNET, NO_DATA, IO_EXCEPTION, JSON_EXCEPTION,
    }

    private FitnessErrorType errorType = FitnessErrorType.UNSPECIFIED;

    public FitnessException(String message) {
        super(message);
    }

    public FitnessException(FitnessErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }

    public FitnessErrorType getErrorType() {
        return this.errorType;
    }
}
