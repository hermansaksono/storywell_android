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
    private double calories;
    private double distance;
    private double activeMinutes;
    private Context context;

    private OneDayFitness(Context context, Date date, int steps, double calories, double distance, double activeMinutes){
        this.context = context;
        this.date = date;
        this.steps = steps;
        this.calories = calories;
        this.distance = distance;
        this.activeMinutes = activeMinutes;
    }

    public static OneDayFitness create(Context context, Date date, int steps, double calories, double distance, double activeMinutes){
        return new OneDayFitness(context,date, steps, calories, distance, activeMinutes);
    }

    @Override
    public Date getDate() {
        return this.date;
    }

    @Override
    public int getSteps() {
        return this.steps;
    }

    @Override
    public double getCalories() {
        return this.calories;
    }

    @Override
    public double getDistance() {
        return this.distance;
    }

    @Override
    public double getActiveMinutes() {
        return this.activeMinutes;
    }
}
