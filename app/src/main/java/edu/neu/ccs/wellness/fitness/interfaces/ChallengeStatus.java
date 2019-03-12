package edu.neu.ccs.wellness.fitness.interfaces;

/**
 * Created by hermansaksono on 1/23/18.
 */

public enum ChallengeStatus {
    UNSTARTED,     // This is the initial state when the challenge has not been initialized
    AVAILABLE,     // This is when the challenge definition has been downloaded
    UNSYNCED_RUN,  // This is when a challenge has been selected but not synced with the REST server
    RUNNING,       // This is when a challenge has been selected and synced with the rest server
    PASSED,        // This is when a challenge has passed the end datetime
    ACHIEVED,
    MISSED,
    TOKEN_ACQUIRED,
    COMPLETED,
    CLOSED,     // This is when a group has completed the challenge // TODO Delete this
    ERROR_CONNECTING, MALFORMED_JSON, UNINITIALIZED;

    /*
    * The state transitions are as follow:
    *                 App installed or a challenge was completed and shown to user -> UNINITIALIZED
    * UNINITIALIZED + ChallengeManager downloaded Available challenges             -> AVAILABLE
    * AVAILABLE     + User picked a challenge and press Submit                     -> UNSYNCED_RUN
    * UNSYNCED_RUN  + SyncManager posted the running challenge to the REST server  -> RUNNING
    * RUNNING       + ChallengeManager determined that the end date has passed     -> PASSED
    * PASSED        + User decided to set the challenge as CLOSED                  -> CLOSED
    * CLOSED        + The UnitChallenge complete is shown to user                  -> AVAILABLE
    * */

    public static String toStringCode(ChallengeStatus status) {
        switch (status) {
            case UNSTARTED:
                return "UNSTARTED";
            case AVAILABLE:
                return "AVAILABLE";
            case UNSYNCED_RUN:
                return "UNSYNCED_RUN";
            case RUNNING:
                return "RUNNING";
            case PASSED:
                return "PASSED";
            case ACHIEVED:
                return "ACHIEVED";
            case MISSED:
                return "MISSED";
            case TOKEN_ACQUIRED:
                return "TOKEN_ACQUIRED";
            case COMPLETED:
                return "COMPLETED";
            case CLOSED: // TODO DELETE
                return "CLOSED";
            case ERROR_CONNECTING:
                return "ERROR_CONNECTING";
            case MALFORMED_JSON:
                return "MALFORMED_JSON";
            case UNINITIALIZED:
                return "UNINITIALIZED";
            default:
                return "UNSTARTED";
        }
    }

    public static ChallengeStatus fromStringCode(String string) {
        if (string.equals("UNSTARTED")) {
            return ChallengeStatus.UNSTARTED;
        } else if (string.equals("AVAILABLE")) {
            return ChallengeStatus.AVAILABLE;
        } else if (string.equals("UNSYNCED_RUN")) {
            return ChallengeStatus.UNSYNCED_RUN;
        } else if (string.equals("RUNNING")) {
            return ChallengeStatus.RUNNING;
        } else if (string.equals("PASSED")) {
            return ChallengeStatus.PASSED;
        } else if (string.equals("ACHIEVED")) {
            return ChallengeStatus.ACHIEVED;
        } else if (string.equals("MISSED")) {
            return ChallengeStatus.MISSED;
        } else if (string.equals("TOKEN_ACQUIRED")) {
            return ChallengeStatus.TOKEN_ACQUIRED;
        } else if (string.equals("COMPLETED")) {
            return ChallengeStatus.COMPLETED;
        } else if (string.equals("CLOSED")) {
            return ChallengeStatus.CLOSED;
        } else if (string.equals("ERROR_CONNECTING")) {
            return ChallengeStatus.ERROR_CONNECTING;
        } else if (string.equals("MALFORMED_JSON")) {
            return ChallengeStatus.MALFORMED_JSON;
        } else if(string.equals("UNSTARTED")){
            return ChallengeStatus.UNSTARTED;
        } else if(string.equals("UNINITIALIZED")){
            return ChallengeStatus.UNINITIALIZED;
        } else {
            return null;
        }
    }
}
