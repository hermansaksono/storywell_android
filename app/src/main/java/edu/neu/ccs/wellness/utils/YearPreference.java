package edu.neu.ccs.wellness.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.NumberPicker;

import java.util.Calendar;

import edu.neu.ccs.wellness.storytelling.R;

/**
 * Created by hermansaksono on 7/23/18.
 */

public class YearPreference extends DialogPreference {

    private static final int DEFAULT_YEAR = 2000;
    private static final int MIN_YEAR = 1945;

    private int year = DEFAULT_YEAR;

    private NumberPicker picker_year;

    public YearPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.pref_dialog_year);
    }

    /* PARENT METHODS */
    @Override
    protected View onCreateDialogView() {
        View view = super.onCreateDialogView();
        return view;
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        this.year = picker_year.getValue();
        persistInt(this.year);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        this.picker_year = view.findViewById(R.id.picker_year);
        this.picker_year.setMinValue(MIN_YEAR);
        this.picker_year.setMaxValue(getMaxYear());
        this.picker_year.setValue(this.year);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return Integer.parseInt(a.getString(index));
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            setValue(getPersistedInt(DEFAULT_YEAR));
        } else {
            setValue((int) defaultValue);
        }
    }

    private void setValue(int value) {
        persistInt(value);
        notifyDependencyChange(false);
        this.year = value;
    }

    public float getValue() {
        return this.year;
    }

    /* HELPER METHODS */
    private static int getThisYear() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.YEAR);
    }

    private static int getMaxYear() {
        return getThisYear() - 2;
    }
}
