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
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;

import edu.neu.ccs.wellness.fitness.MultiDayFitness;
import edu.neu.ccs.wellness.fitness.challenges.ChallengeDoesNotExistsException;
import edu.neu.ccs.wellness.fitness.challenges.ChallengeProgressCalculator;
import edu.neu.ccs.wellness.fitness.interfaces.ChallengeManagerInterface;
import edu.neu.ccs.wellness.fitness.interfaces.ChallengeStatus;
import edu.neu.ccs.wellness.fitness.interfaces.FitnessException;
import edu.neu.ccs.wellness.fitness.interfaces.GroupFitnessInterface;
import edu.neu.ccs.wellness.fitness.interfaces.MultiDayFitnessInterface;
import edu.neu.ccs.wellness.fitness.interfaces.OneDayFitnessInterface;
import edu.neu.ccs.wellness.fitness.interfaces.UnitChallengeInterface;
import edu.neu.ccs.wellness.fitness.storage.FitnessRepository;
import edu.neu.ccs.wellness.people.Group;
import edu.neu.ccs.wellness.people.GroupInterface;
import edu.neu.ccs.wellness.people.Person;
import edu.neu.ccs.wellness.people.PersonDoesNotExistException;
import edu.neu.ccs.wellness.storytelling.Storywell;
import edu.neu.ccs.wellness.storytelling.settings.SynchronizedSettingRepository;
import edu.neu.ccs.wellness.storytelling.sync.FetchingStatus;

/**
 * Created by hermansaksono on 5/16/18.
 */

public class FitnessChallengeViewModel extends AndroidViewModel {

    public static float MAX_FITNESS_CHALLENGE_PROGRESS = 1.0f;
    private static String STEPS_STRING_FORMAT = "#,###";

    private MutableLiveData<FetchingStatus> status = null;

    private Storywell storywell;
    private GregorianCalendar startDate;
    private GregorianCalendar endDate;
    private boolean isDemoMode;
    private Group group;
    private GroupFitnessInterface sevenDayFitness;
    private FitnessRepository fitnessRepository;
    private ChallengeManagerInterface challengeManager;
    // private ChallengeStatus challengeStatus;
    private UnitChallengeInterface unitChallenge;
    private ChallengeProgressCalculator calculator = null;

    /* CONSTRUCTOR */
    public FitnessChallengeViewModel(Application application) {
        super(application);
        this.storywell = new Storywell(getApplication());
        this.fitnessRepository = new FitnessRepository();
        this.isDemoMode = SynchronizedSettingRepository
                .getLocalInstance(application.getApplicationContext()).isDemoMode();
    }

    /* PUBLIC METHODS */
    public LiveData<FetchingStatus> getChallengeLiveData() {
        if (this.status == null) {
            this.status = new MutableLiveData<>();
            this.status.setValue(FetchingStatus.UNINITIALIZED);
        }
        return this.status;
    }

    public void refreshFitnessChallengeData(GregorianCalendar startDate, GregorianCalendar endDate) {
        if (this.status == null) {
            this.status = new MutableLiveData<>();
        }
        this.status.setValue(FetchingStatus.FETCHING);
        this.startDate = startDate;
        this.endDate = endDate;
        this.loadFitnessAndChallengeData();
    }

    public void refreshFitnessDataOnly(GregorianCalendar startDate, GregorianCalendar endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.loadSevenDayFitness(storywell.getGroup());
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
        if (this.challengeManager == null) {
            throw new ChallengeDoesNotExistsException("Challenge data not initialized");
        }

        ChallengeStatus status = null;
        try {
             status = this.challengeManager.getStatus();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return status;
    }

    public boolean isChallengeAvailable() throws ChallengeDoesNotExistsException {
        return ChallengeStatus.AVAILABLE.equals(this.getChallengeStatus());
    }

    public boolean isChallengeRunning() throws ChallengeDoesNotExistsException {
        return ChallengeStatus.RUNNING.equals(this.getChallengeStatus()) ||
                ChallengeStatus.UNSYNCED_RUN.equals(this.getChallengeStatus());
    }

    public boolean hasChallengePassed() {
        Calendar now = GregorianCalendar.getInstance();
        return now.after(this.endDate);
    }

    public boolean isChallengeAchieved(GregorianCalendar today)
            throws ChallengeDoesNotExistsException, PersonDoesNotExistException, FitnessException {
        return this.getOverallProgress(today) >= 1.0f;
    }

    public boolean isChallengeClosed(){
        try {
            return ChallengeStatus.CLOSED.equals(this.getChallengeStatus());
        } catch (ChallengeDoesNotExistsException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void setChallengeClosedIfAchieved(Calendar onThisDay) {
        try {
            if (this.isGoalAchieved(onThisDay)) {
                this.setChallengeClosed();
            }
        } catch (PersonDoesNotExistException e) {
            Log.e("SWELL", "Person does not exist.");
        } catch (ChallengeDoesNotExistsException e) {
            Log.e("SWELL", "Challenge does not exist.");
        } catch (FitnessException e) {
            Log.e("SWELL", "Fitness exception when closing the challenge: " + e.toString());
        }
    }

    public void setChallengeClosed() throws ChallengeDoesNotExistsException {
        try {
            if (this.challengeManager != null) {
                this.challengeManager.closeChallenge();
                new CloseFitnessChallengeAsync().execute();
            } else {
                throw new ChallengeDoesNotExistsException("Challenge data not initialized");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* PUBLIC METHODS FOR GETTING FITNESS PROGRESS AND GOALS */
    public float getAdultProgress(GregorianCalendar thisDay)
            throws ChallengeDoesNotExistsException, PersonDoesNotExistException,
            FitnessException {
        if (this.isDemoMode) {
            return 1f;
        }
        Date date = thisDay.getTime();
        return getPersonProgress(Person.ROLE_PARENT, date);
    }

    public String getAdultStepsString(GregorianCalendar thisDay)
            throws PersonDoesNotExistException {
        return getFormattedSteps(this.getAdultSteps(thisDay));
    }

    public int getAdultSteps(GregorianCalendar thisDay) throws PersonDoesNotExistException {
        if (this.isDemoMode) {
            return 10000;
        }
        return this.getPersonTotalSteps(Person.ROLE_PARENT);
    }

    public String getAdultGoalString() throws IOException, JSONException {
        return String.valueOf(this.getAdultGoal());
    }

    private int getAdultGoal() throws IOException, JSONException {
        if (this.isDemoMode) {
            return 10560;
        }
        return (int) this.challengeManager.getRunningChallenge().getUnitChallenge().getGoal();
    }

    public float getChildProgress(GregorianCalendar thisDay)
            throws ChallengeDoesNotExistsException, PersonDoesNotExistException,
            FitnessException {
        if (this.isDemoMode) {
            return 1f;
        }
        Date date = thisDay.getTime();
        return getPersonProgress(Person.ROLE_CHILD, date);
    }

    public int getChildSteps(GregorianCalendar thisDay) throws PersonDoesNotExistException {
        if (this.isDemoMode) {
            return 11000;
        }
        return this.getPersonTotalSteps(Person.ROLE_CHILD);
    }

    public String getChildStepsString(GregorianCalendar thisDay)
            throws PersonDoesNotExistException {
        return getFormattedSteps(this.getChildSteps(thisDay));
    }

    private int getChildGoal() throws IOException, JSONException {
        if (this.isDemoMode) {
            return 10000;
        }
        return (int) this.challengeManager.getRunningChallenge().getUnitChallenge().getGoal();
    }

    public String getChildGoalString() throws IOException, JSONException {
        return String.valueOf(this.getChildGoal());
    }

    public float getOverallProgress(Calendar thisDay)
            throws ChallengeDoesNotExistsException, PersonDoesNotExistException,
            FitnessException {
        if (this.isDemoMode) {
            return 1f;
        }
        if (this.calculator == null) {
            throw new ChallengeDoesNotExistsException("Challenge data not initialized");
        } else {
            Date date = thisDay.getTime();
            float familyProgresRaw = calculator.getGroupProgressByDate(date);
            return Math.min(MAX_FITNESS_CHALLENGE_PROGRESS, familyProgresRaw);
        }
    }

    public static String getFormattedSteps(float steps) {
        return getFormattedSteps(Math.round(steps));
    }

    public static String getFormattedSteps(int steps) {
        DecimalFormat formatter = new DecimalFormat(STEPS_STRING_FORMAT);
        return formatter.format(steps);
    }

    public boolean isGoalAchieved(Calendar thisDay)
            throws ChallengeDoesNotExistsException, PersonDoesNotExistException,
            FitnessException {
        return this.getOverallProgress(thisDay) >= MAX_FITNESS_CHALLENGE_PROGRESS;
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

    private int getPersonTotalSteps(String personRoleType)
            throws PersonDoesNotExistException {
        int steps = 0;
        Person person = getPerson(personRoleType);
        MultiDayFitnessInterface multiDayFitness = this.sevenDayFitness
                .getAPersonMultiDayFitness(person);
        for (OneDayFitnessInterface oneDayFitness : multiDayFitness.getDailyFitness()) {
            steps += oneDayFitness.getSteps();
        }
        return steps;
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
                unitChallenge = getUnitChallenge(challengeManager);
                Log.d("SWELL", "Challenge data fetched: " + challengeManager.getStatus());

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

    private class CloseFitnessChallengeAsync extends AsyncTask<Void, Void, Void> {

        protected Void doInBackground(Void... voids) {
            try {
                challengeManager.syncCompletedChallenge();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    /* FIREBASE FITNESS FETCHING */
    private void loadSevenDayFitness(GroupInterface group) {
        // TODO there's a bug causes by this. I'm disabling this functionality for now.
        /*
        this.sevenDayFitness = GroupFitness.newInstance(new HashMap<Person, MultiDayFitnessInterface>());
        this.iterateFetchPersonDailyFitness(group.getMembers().iterator());
        */
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
            return challengeManager.getRunningChallenge().getUnitChallenge();
        } else if (challengeManager.getStatus() == ChallengeStatus.PASSED) {
            return challengeManager.getRunningChallenge().getUnitChallenge();
        } else if (challengeManager.getStatus() == ChallengeStatus.CLOSED) {
            return challengeManager.getRunningChallenge().getUnitChallenge();
        } else {
            return null;
        }
    }
}
