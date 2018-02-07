package edu.neu.ccs.wellness.storage;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by hermansaksono on 2/6/18.
 */

public class LocalStorageManager implements LocalStorageManagerInterface {
    /* PRIVATE VARIABLES */
    Context context;

    /* PUBLIC CONSTRUCTOR */
    public LocalStorageManager(Context context) {
        this.context = context;
    }

    /* PUBLIC METHODS */
    @Override
    public boolean isFileExist(String filename) {
        File file = this.context.getFileStreamPath(filename);
        return file.exists();
    }

    @Override
    public void write(String filename, String contents) {
        try {
            FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
            fos.write(contents.getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String read(String filename) {
        StringBuffer sb = new StringBuffer("");
        try {
            FileInputStream fileInputStream = context.openFileInput(filename);
            InputStreamReader isReader = new InputStreamReader(fileInputStream);
            BufferedReader buffReader = new BufferedReader(isReader);
            String readString = buffReader.readLine();
            while (readString != null) {
                sb.append(readString);
                readString = buffReader.readLine();
            }
            isReader.close();
            buffReader.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return sb.toString();
    }
}
