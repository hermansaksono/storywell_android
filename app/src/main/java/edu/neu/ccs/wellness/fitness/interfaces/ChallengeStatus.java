package edu.neu.ccs.wellness.fitness.interfaces;

/**
 * Created by hermansaksono on 1/23/18.
 */

public enum ChallengeStatus {
    UNSTARTED,     // This is the initial state when the challenge has not been initialized
    AVAILABLE,     // This is when the challenge definition has been downloaded
    UNSYNCED_RUN,  // This is when a challenge has been selected but not synced with the REST server
    RUNNING,       // This is when a challenge has been selected and synced with the rest server
    COMPLETED,     // This is when a group has completed the challenge
    ERROR_CONNECTING, MALFORMED_JSON, UNINITIALIZED;

    /*
    * The state transitions are as follow:
    *                 App installed or a challenge was completed and shown to user -> UNINITIALIZED
    * UNINITIALIZED + ChallengeManager downloaded Available challenges             -> AVAILABLE
    * AVAILABLE     + User picked a challenge and press Submit                     -> UNSYNCED_RUN
    * UNSYNCED_RUN  + SyncManager posted the running challenge to the REST server  -> RUNNING
    * RUNNING       + ChallengeManager determined that the group completed their goal -> COMPLETED
    * COMPLETED     + The Challenge complete is shown to user                      -> UNINITIALIZED
    * */

    public static String toStringCode(ChallengeStatus status) {
        if (status == ChallengeStatus.UNSTARTED) {
            return "UNSTARTED";
        } else if (status == ChallengeStatus.AVAILABLE) {
            return "AVAILABLE";
        } else if (status == ChallengeStatus.UNSYNCED_RUN) {
            return "UNSYNCED_RUN";
        } else if (status == ChallengeStatus.RUNNING) {
            return "RUNNING";
        } else if (status == ChallengeStatus.COMPLETED) {
            return "COMPLETED";
        } else if (status == ChallengeStatus.ERROR_CONNECTING) {
            return "ERROR_CONNECTING";
        } else if (status == ChallengeStatus.MALFORMED_JSON) {
            return "MALFORMED_JSON";
        } else if(status == ChallengeStatus.UNINITIALIZED){
            return "UNINITIALIZED";
        }
        return null;
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
        } else if (string.equals("COMPLETED")) {
            return ChallengeStatus.COMPLETED;
        } else if (string.equals("ERROR_CONNECTING")) {
            return ChallengeStatus.ERROR_CONNECTING;
        } else if (string.equals("MALFORMED_JSON")) {
            return ChallengeStatus.MALFORMED_JSON;
        } else if(string.equals("UNSTARTED")){
            return ChallengeStatus.UNSTARTED;
        }
        else if(string.equals("UNINITIALIZED")){
            return ChallengeStatus.UNINITIALIZED;
        }
        return null;
    }
}
