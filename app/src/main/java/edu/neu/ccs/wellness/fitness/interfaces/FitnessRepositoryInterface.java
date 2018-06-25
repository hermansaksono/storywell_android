package edu.neu.ccs.wellness.fitness.interfaces;

import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.List;

import edu.neu.ccs.wellness.people.Person;

/**
 * Created by hermansaksono on 6/24/18.
 */

public interface FitnessRepositoryInterface {

    void fetchDailyFitness(Person person, Date startDate, Date endDate, final ValueEventListener listener);

    void insertDailyFitness(Person person, List<FitnessSample> samples);

    void fetchIntradayFitness(Person person, Date date, final ValueEventListener listener);

    void insertIntradayFitness(Person person, Date date, List<FitnessSample> samples);
}
