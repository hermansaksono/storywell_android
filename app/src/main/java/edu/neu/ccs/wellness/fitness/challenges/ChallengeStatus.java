package edu.neu.ccs.wellness.fitness.challenges;

/**
 * Created by hermansaksono on 1/23/18.
 */

public enum ChallengeStatus {
    UNINITIALIZED, AVAILABLE, RUNNING,
    ERROR_CONNECTING, MALFORMED_JSON;

    public static String toStringCode(ChallengeStatus status) {
        if (status == ChallengeStatus.UNINITIALIZED) {
            return "UNINITIALIZED";
        } else if (status == ChallengeStatus.AVAILABLE) {
            return "AVAILABLE";
        } else if (status == ChallengeStatus.RUNNING) {
            return "RUNNING";
        } else if (status == ChallengeStatus.ERROR_CONNECTING) {
            return "ERROR_CONNECTING";
        } else if (status == ChallengeStatus.MALFORMED_JSON) {
            return "MALFORMED_JSON";
        } else {
            return "UNINITIALIZED";
        }
    }

    public static ChallengeStatus fromStringCode(String string) {
        if (string.equals("UNINITIALIZED")) {
            return ChallengeStatus.UNINITIALIZED;
        } else if (string.equals("AVAILABLE")) {
            return ChallengeStatus.AVAILABLE;
        } else if (string.equals("RUNNING")) {
            return ChallengeStatus.RUNNING;
        } else if (string.equals("ERROR_CONNECTING")) {
            return ChallengeStatus.ERROR_CONNECTING;
        } else if (string.equals("MALFORMED_JSON")) {
            return ChallengeStatus.MALFORMED_JSON;
        } else {
            return ChallengeStatus.UNINITIALIZED;
        }
    }
}
