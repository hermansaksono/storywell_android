package edu.neu.ccs.wellness.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

/**
 * Created by hermansaksono on 3/19/19.
 */

public class WellnessBluetooth {

    public static String[] COARSE_PERMISSIONS = {Manifest.permission.ACCESS_COARSE_LOCATION};
    public static final int PERMISSION_REQUEST_COARSE_LOCATION = 8100;

    public static void tryRequestCoarsePermission(Activity activity) {
        if (!isCoarseLocationAllowed(activity)) {
            ActivityCompat.requestPermissions(activity, COARSE_PERMISSIONS,
                    PERMISSION_REQUEST_COARSE_LOCATION);
        }
    }

    public static boolean isCoarseLocationAllowed(Context context) {
        int permissionCoarseLocation = ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        return permissionCoarseLocation == PackageManager.PERMISSION_GRANTED;
    }
}
