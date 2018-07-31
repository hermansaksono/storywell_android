package edu.neu.ccs.wellness.storytelling.sync;

/**
 * Created by hermansaksono on 7/19/18.
 */

public enum SyncStatus {
    UNINITIALIZED, NO_NEW_DATA, INITIALIZING,
    CONNECTING, DOWNLOADING, UPLOADING, IN_PROGRESS,
    COMPLETED, FAILED,
    NO_INTERNET
}
