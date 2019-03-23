package edu.neu.ccs.wellness.utils;

import java.text.DecimalFormat;

/**
 * Created by hermansaksono on 3/22/19.
 */

public class WellnessStringFormatter {
    private static String STEPS_STRING_FORMAT = "#,###";

    public static String getFormattedSteps(int steps) {
        DecimalFormat formatter = new DecimalFormat(STEPS_STRING_FORMAT);
        return formatter.format(steps);
    }
}
