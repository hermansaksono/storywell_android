package edu.neu.ccs.wellness.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.support.v4.util.Pair;
import android.util.AttributeSet;
import android.view.View;
import android.widget.NumberPicker;

import edu.neu.ccs.wellness.storytelling.R;

/**
 * Created by hermansaksono on 7/22/18.
 */

public class FeetInchesPreference extends DialogPreference {

    public static final float DEFAULT_VALUE = 170;
    private static final float FOOT_TO_CM = 30.48f;
    private static final float INCH_TO_CM = 2.54f;

    private int valueOfFeet;
    private int valueOfInches;
    private float valueInCm;

    private NumberPicker editTextFeet;
    private NumberPicker editTextInches;

    public FeetInchesPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.pref_dialog_feet_inches);
    }

    /* PARENT METHODS */
    @Override
    protected View onCreateDialogView() {
        View view = super.onCreateDialogView();
        return view;
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            this.valueOfFeet = editTextFeet.getValue();
            this.valueOfInches = editTextInches.getValue();
            this.valueInCm = getValueInCm(this.valueOfFeet, this.valueOfInches);
            persistFloat(this.valueInCm);
        }
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        this.editTextFeet = view.findViewById(R.id.edit_feet);
        this.editTextFeet.setMinValue(1);
        this.editTextFeet.setMaxValue(7);
        this.editTextFeet.setValue(this.valueOfFeet);

        this.editTextInches = view.findViewById(R.id.edit_inches);
        this.editTextInches.setMinValue(0);
        this.editTextInches.setMaxValue(12);
        this.editTextInches.setValue(this.valueOfInches);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        //return Float.parseFloat(a.getString(index));
        return a.getFloat(index, DEFAULT_VALUE);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            setValue(getPersistedFloat(valueInCm));
        } else {
            setValue((float) defaultValue);
        }
    }

    void setValue(Float value) {
        persistFloat(value);
        notifyDependencyChange(false);
        this.valueInCm = value;
        this.valueOfFeet = getValueInFeet(value);
        this.valueOfInches = getValueInInches(value);
    }

    public float getValue() {
        return this.valueInCm;
    }

    /* HELPER METHODS */
    private static float getValueInCm(int feet, int inches) {
        return (FOOT_TO_CM * feet) + (INCH_TO_CM * inches);
    }

    private static int getValueInFeet(float cm) {
        return (int) Math.floor(cm / FOOT_TO_CM);
    }

    private static int getValueInInches(float cm) {
        int feet = getValueInFeet(cm);
        float feetToCm = feet * FOOT_TO_CM;
        float inches = (cm - feetToCm) / INCH_TO_CM;
        return Math.round(inches);
    }

}
