package edu.neu.ccs.wellness.fitness.interfaces;

import java.util.Date;

/**
 * Created by hermansaksono on 6/14/17.
 */

public interface OneDayFitnessInterface {

    /**
     * Gets the Date of this particular one-day fitness
     * @return Date object that describes the day when the fitness activities were collected
     */
    Date getDate();

    /**
     * Get the number of steps on a Date
     * @return The number of steps
     */
    int getSteps();

    /**
     * Get the number of calories burned on a Date
     * @return The numbers of calories
     */
    float getCalories();

    /**
     * Get the distance walked on a Date
     * @return The distance walked in miles
     */
    float getDistance();

    /**
     * Get the number of active minutes on a Date
     * @return The number of active minutes
     */
    float getActiveMinutes();
}
