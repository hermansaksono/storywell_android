package edu.neu.ccs.wellness.utils;

import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

/**
 * Created by hermansaksono on 3/8/19.
 */

public class LbsToKgsEditTextPreference extends EditTextPreference {

    private static final float LBS_TO_KG = 2.205f;

    public LbsToKgsEditTextPreference(Context context) {
        super(context);
    }

    public LbsToKgsEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LbsToKgsEditTextPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected String getPersistedString(String defaultReturnValue) {
        int lbs = (int) (getPersistedInt(-1) * LBS_TO_KG);
        return String.valueOf(lbs);
    }

    @Override
    protected boolean persistString(String value) {
        int kgs = (int) (Integer.valueOf(value) / LBS_TO_KG);
        return persistInt(kgs);
    }
}
