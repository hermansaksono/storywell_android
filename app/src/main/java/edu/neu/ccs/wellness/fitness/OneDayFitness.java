package edu.neu.ccs.wellness.fitness;

import android.content.Context;

import java.util.Date;

import edu.neu.ccs.wellness.fitness.interfaces.OneDayFitnessInterface;

/**
 * Created by hermansaksono on 3/20/18.
 */

public class OneDayFitness implements OneDayFitnessInterface {

    //PRIVATE MEMBERS
    private Date date;
    private int steps;
    private float calories;
    private float distance;
    private float activeMinutes;
    private Context context;

    private OneDayFitness(Context context, Date date, int steps, float calories, float distance, float activeMinutes){
        this.context = context;
        this.date = date;
        this.steps = steps;
        this.calories = calories;
        this.distance = distance;
        this.activeMinutes = activeMinutes;
    }

    public static OneDayFitness create(Context context, Date date, int steps, float calories, float distance, float activeMinutes){
        return new OneDayFitness(context,date, steps, calories, distance, activeMinutes);
    }

    @Override
    public Date getDate() {
        return null;
    }

    @Override
    public int getSteps() {
        return 0;
    }

    @Override
    public float getCalories() {
        return 0;
    }

    @Override
    public float getDistance() {
        return 0;
    }

    @Override
    public float getActiveMinutes() {
        return 0;
    }
}
