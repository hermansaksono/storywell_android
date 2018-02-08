package edu.neu.ccs.wellness.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by hermansaksono on 11/2/17.
 */

public class WellnessIO {

    public static final String SHARED_PREFS = "WELLNESS";

    /***
     * Read the contents of a file named filename in internal storage
     * @param context Android context
     * @param filename Name of the file to be saved in internal storage
     * @return String contents of the file
     */
    public static String readFileFromStorage(Context context, String filename) {
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

    /***
     * Write String contents to internal storage
     * @param context Android context
     * @param filename Name of the file to be saved in internal storage
     * @param contents Contents of the file
     */
    public static void writeFileToStorage(Context context, String filename, String contents) {
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

    /***
     * Determines whether a file exists in the internal storage
     * @param context Android context
     * @param filename Name of the file to be saved in internal storage
     * @return true if the file exists in the internal storage. Otherwise return false;
     */
    public static boolean isFileExists(Context context, String filename) {
        File file = context.getFileStreamPath(filename);
        return file.exists();
    }

    public static SharedPreferences getSharedPref(Context context) {
        return context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
    }
}
