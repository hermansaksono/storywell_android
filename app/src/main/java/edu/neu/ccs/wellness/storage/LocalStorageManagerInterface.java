package edu.neu.ccs.wellness.storage;

/**
 * Created by hermansaksono on 2/6/18.
 */

public interface LocalStorageManagerInterface {
    boolean isFileExist(String filename);

    void write(String filename, String contents);

    String read(String filename);
}
