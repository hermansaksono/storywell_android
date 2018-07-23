package edu.neu.ccs.wellness.fitness.storage;

/**
 * Created by hermansaksono on 7/20/18.
 */

public interface onDataUploadListener {

    /* Called when data upload succesful */
    void onSuccess();

    /* Called when data upload failed */
    void onFailed();
}
