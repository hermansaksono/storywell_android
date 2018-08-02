package edu.neu.ccs.wellness.storytelling.sync;

/**
 * Created by hermansaksono on 7/19/18.
 */

public enum SyncStatus {
    UNINITIALIZED,
    NO_NEW_DATA, NEW_DATA_AVAILABLE,
    INITIALIZING, CONNECTING, DOWNLOADING, UPLOADING, IN_PROGRESS,
    COMPLETED, FAILED,
    NO_INTERNET
}
