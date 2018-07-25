package edu.neu.ccs.wellness.storytelling.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;

import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;

import edu.neu.ccs.wellness.fitness.GroupFitness;
import edu.neu.ccs.wellness.fitness.MultiDayFitness;
import edu.neu.ccs.wellness.fitness.challenges.ChallengeDoesNotExistsException;
import edu.neu.ccs.wellness.fitness.challenges.ChallengeProgressCalculator;
import edu.neu.ccs.wellness.fitness.interfaces.ChallengeManagerInterface;
import edu.neu.ccs.wellness.fitness.interfaces.ChallengeStatus;
import edu.neu.ccs.wellness.fitness.interfaces.FitnessException;
import edu.neu.ccs.wellness.fitness.interfaces.GroupFitnessInterface;
import edu.neu.ccs.wellness.fitness.interfaces.MultiDayFitnessInterface;
import edu.neu.ccs.wellness.fitness.interfaces.UnitChallengeInterface;
import edu.neu.ccs.wellness.fitness.storage.FitnessRepository;
import edu.neu.ccs.wellness.people.Group;
import edu.neu.ccs.wellness.people.GroupInterface;
import edu.neu.ccs.wellness.people.Person;
import edu.neu.ccs.wellness.people.PersonDoesNotExistException;
import edu.neu.ccs.wellness.storytelling.Storywell;
import edu.neu.ccs.wellness.storytelling.sync.FetchingStatus;

/**
 * Created by hermansaksono on 5/16/18.
 */

public class FitnessChallengeViewModel extends AndroidViewModel {

    public static float MAX_FITNESS_CHALLENGE_PROGRESS = 1.0f;

    private MutableLiveData<FetchingStatus> status = null;

    private Storywell storywell;
    private GregorianCalendar startDate;
    private GregorianCalendar endDate;
    private Group group;
    private GroupFitnessInterface sevenDayFitness;
    private FitnessRepository fitnessRepository;
    private ChallengeManagerInterface challengeManager;
    private ChallengeStatus challengeStatus;
    private UnitChallengeInterface unitChallenge;
    private ChallengeProgressCalculator calculator = null;

    /* CONSTRUCTOR */
    public FitnessChallengeViewModel(Application application) {
        super(application);
        this.storywell = new Storywell(getApplication());
        this.fitnessRepository = new FitnessRepository();
    }

    /* PUBLIC METHODS */
    public LiveData<FetchingStatus> fetchSevenDayFitness(GregorianCalendar startDate,
                                                         GregorianCalendar endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
        if (this.status == null) {
            this.status = new MutableLiveData<>();
            this.status.setValue(FetchingStatus.FETCHING);
            loadFitnessAndChallengeData();
        }
        return this.status;
    }

    public FetchingStatus getFetchingStatus() {
        if (status != null) {
            return status.getValue();
        } else {
            return FetchingStatus.UNINITIALIZED;
        }
    }

    public GroupFitnessInterface getSevenDayFitness() throws FitnessException {
        if (this.sevenDayFitness != null) {
            return this.sevenDayFitness;
        } else {
            throw new FitnessException("Seven-day Fitness data not initialized.");
        }
    }

    public ChallengeStatus getChallengeStatus() throws ChallengeDoesNotExistsException {
        if (this.challengeStatus != null) {
            return this.challengeStatus;
        } else {
            throw new ChallengeDoesNotExistsException("Challenge data not initialized");
        }
    }

    public float getAdultProgress(GregorianCalendar thisDay)
            throws ChallengeDoesNotExistsException, PersonDoesNotExistException,
            FitnessException {
        Date date = thisDay.getTime();
        return 0.6f;//getPersonProgress(Person.ROLE_PARENT, date);
    }

    public float getChildProgress(GregorianCalendar thisDay)
            throws ChallengeDoesNotExistsException, PersonDoesNotExistException,
            FitnessException {
        Date date = thisDay.getTime();
        return 0.8f;//getPersonProgress(Person.ROLE_CHILD, date);
    }

    public float getOverallProgress(GregorianCalendar thisDay)
            throws ChallengeDoesNotExistsException, PersonDoesNotExistException,
            FitnessException {
        if (this.calculator == null) {
            throw new ChallengeDoesNotExistsException("Challenge data not initialized");
        } else {
            Date date = thisDay.getTime();
            //float familyProgresRaw = calculator.getGroupProgressByDate(date); // TODO Uncomment this on production
            return 0.7f;//Math.min(MAX_FITNESS_CHALLENGE_PROGRESS, familyProgresRaw); // TODO Uncomment this on production
        }
    }

    /* PRIVATE METHODS */
    private float getPersonProgress(String personRoleType, Date date)
            throws ChallengeDoesNotExistsException, PersonDoesNotExistException,
            FitnessException {
        if (this.calculator != null) {
            Person person = getPerson(personRoleType);
            float personProgresRaw = calculator.getPersonProgressByDate(person, date);
            return Math.min(MAX_FITNESS_CHALLENGE_PROGRESS, personProgresRaw);
        } else {
            throw new ChallengeDoesNotExistsException("Challenge data not initialized");
        }
    }

    private Person getPerson(String personRoleType) throws PersonDoesNotExistException {
        for (Person personInList : group.getMembers()) {
            if (personInList.isRole(personRoleType)) {
                return personInList;
            }
        }
        throw new PersonDoesNotExistException("Person is not on the list");
    }

    private void loadFitnessAndChallengeData() {
        new LoadFamilyFitnessChallengeAsync().execute();
    }

    /* ASYNCTASKS */
    private class LoadFamilyFitnessChallengeAsync extends AsyncTask<Void, Integer, FetchingStatus> {

        String errorMsg = "";

        protected FetchingStatus doInBackground(Void... voids) {
            if (storywell.isServerOnline() == false) {
                this.errorMsg = "No internet connection";
                return FetchingStatus.NO_INTERNET;
            }

            try {
                // Fetch Group data
                Log.d("SWELL", "Fetching group data");
                group = storywell.getGroup();
                Log.d("SWELL", "Group data fetched: " + group.toString());

                // Fetch Challenge data using getStatus
                Log.d("SWELL", "Fetching Challenge data ...");
                challengeManager = storywell.getChallengeManager();
                challengeStatus = challengeManager.getStatus();
                unitChallenge = getUnitChallenge(challengeManager);
                Log.d("SWELL", "Challenge data fetched: " + challengeStatus.toString());

                return FetchingStatus.SUCCESS;
            } catch (JSONException e) {
                e.printStackTrace();
                this.errorMsg = "JSON format error";
                return FetchingStatus.FAILED;
            } catch (IOException e) {
                e.printStackTrace();
                this.errorMsg = "IO exception";
                return FetchingStatus.FAILED;
            }
        }

        protected void onPostExecute(FetchingStatus result) {
            if (FetchingStatus.SUCCESS.equals(result)) {
                Log.d("SWELL", "Fetching Fitness data ...");
                loadSevenDayFitness(storywell.getGroup());
            } else {
                onFetchingFailed(result, this.errorMsg);
            }
        }
    }

    /* FIREBASE FITNESS FETCHING */
    private void loadSevenDayFitness(GroupInterface group) {
        this.sevenDayFitness = GroupFitness.newInstance(new HashMap<Person, MultiDayFitnessInterface>());
        this.iterateFetchPersonDailyFitness(group.getMembers().iterator());
    }

    private void iterateFetchPersonDailyFitness(Iterator<Person> personIterator) {
        if (personIterator.hasNext()) {
            this.fetchPersonDailyFitness(personIterator);
        } else {
            this.onCompletedPersonDailyFitnessIteration();
        }
    }

    private void fetchPersonDailyFitness(final Iterator<Person> personIterator) {
        final Person person = personIterator.next();
        final Date startTime = startDate.getTime();
        final Date endTime = endDate.getTime();
        this.fitnessRepository.fetchDailyFitness(person, startTime, endTime, new ValueEventListener(){

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                MultiDayFitness multiDayFitness = FitnessRepository.getMultiDayFitness(startTime, endTime, dataSnapshot);
                sevenDayFitness.getGroupFitness().put(person, multiDayFitness);
                iterateFetchPersonDailyFitness(personIterator);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                onFetchingFailed(FetchingStatus.FAILED, databaseError.getMessage());
            }
        });
    }

    private void onCompletedPersonDailyFitnessIteration() {
        this.calculator = new ChallengeProgressCalculator(unitChallenge, sevenDayFitness);
        this.status.setValue(FetchingStatus.SUCCESS);
        Log.d("SWELL", "Fetching fitness data successful.");
    }

    private void onFetchingFailed(FetchingStatus status, String msg) {
        this.status.setValue(status);
        Log.e("SWELL", "Fetching fitness data failed: " + msg);
    }

    /* UNIT METHODS */
    private static UnitChallengeInterface getUnitChallenge(ChallengeManagerInterface challengeManager)
            throws IOException, JSONException {
        if (challengeManager.getStatus() == ChallengeStatus.UNSYNCED_RUN) {
            return challengeManager.getUnsyncedChallenge();
        } else if (challengeManager.getStatus() == ChallengeStatus.RUNNING) {
            return challengeManager.getRunningChallenge();
        } else {
            return null;
        }
    }
}