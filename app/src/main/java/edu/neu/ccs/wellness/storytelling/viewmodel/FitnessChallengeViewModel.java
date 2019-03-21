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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import edu.neu.ccs.wellness.fitness.GroupFitness;
import edu.neu.ccs.wellness.fitness.MultiDayFitness;
import edu.neu.ccs.wellness.fitness.challenges.ChallengeDoesNotExistsException;
import edu.neu.ccs.wellness.fitness.challenges.ChallengeProgressCalculator;
import edu.neu.ccs.wellness.fitness.interfaces.ChallengeManagerInterface;
import edu.neu.ccs.wellness.fitness.interfaces.ChallengeStatus;
import edu.neu.ccs.wellness.fitness.interfaces.FitnessException;
import edu.neu.ccs.wellness.fitness.interfaces.GroupFitnessInterface;
import edu.neu.ccs.wellness.fitness.interfaces.MultiDayFitnessInterface;
import edu.neu.ccs.wellness.fitness.interfaces.OneDayFitnessInterface;
import edu.neu.ccs.wellness.fitness.interfaces.RunningChallengeInterface;
import edu.neu.ccs.wellness.fitness.interfaces.UnitChallengeInterface;
import edu.neu.ccs.wellness.fitness.storage.FitnessRepository;
import edu.neu.ccs.wellness.people.Group;
import edu.neu.ccs.wellness.people.GroupInterface;
import edu.neu.ccs.wellness.people.Person;
import edu.neu.ccs.wellness.people.PersonDoesNotExistException;
import edu.neu.ccs.wellness.storytelling.Storywell;
import edu.neu.ccs.wellness.storytelling.settings.SynchronizedSettingRepository;
import edu.neu.ccs.wellness.storytelling.sync.FetchingStatus;
import edu.neu.ccs.wellness.utils.WellnessDate;

/**
 * Created by hermansaksono on 5/16/18.
 */

public class FitnessChallengeViewModel extends AndroidViewModel {

    public static float MAX_FITNESS_CHALLENGE_PROGRESS = 1.0f;
    private static String STEPS_STRING_FORMAT = "#,###";

    private MutableLiveData<FetchingStatus> status = null;

    private Storywell storywell;
    private Date startDate;
    private Date endDate;
    private Date dateToVisualize;
    private boolean isDemoMode;
    private Group group;
    private GroupFitnessInterface sevenDayFitness;
    private FitnessRepository fitnessRepository;
    private ChallengeManagerInterface challengeManager;
    private RunningChallengeInterface runningChallenge;
    private ChallengeStatus challengeStatus = ChallengeStatus.UNINITIALIZED;
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

    public void refreshChallengeAndFitnessData() {
        if (this.status == null) {
            this.status = new MutableLiveData<>();
        }
        this.status.setValue(FetchingStatus.FETCHING);
      
        //this.today = WellnessDate.getBeginningOfDay();
        new LoadChallengeAndFitnessDataAsync().execute();
    }

    /*
    public void refreshFitnessDataOnly(GregorianCalendar startDate, GregorianCalendar endDate) {
        //this.startDate = startDate;
        //this.endDate = endDate;
        this.fetchSevenDayFitness(storywell.getGroup());
    }
    */

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

    /**
     * Determines whether the challenged has passed the end datetmine.
     * @return
     */
    public boolean hasChallengePassed() {
        /*
        Date now = GregorianCalendar.getInstance(Locale.US).getTime();
        return now.after(endDate);
        */
        if (this.runningChallenge != null) {
            return this.runningChallenge.isChallengePassed();
        } else {
            return false;
        }
    }

    /**
     * Determines whether the challenge has been achieved today.
     * @return True if the challenge has been achieved. Otherwise return false;
     */
    public boolean isChallengeAchieved() {
        try {
            return this.getOverallProgress() >= 1.0f;
        } catch (ChallengeDoesNotExistsException e) {
            e.printStackTrace();
        } catch (PersonDoesNotExistException e) {
            e.printStackTrace();
        } catch (FitnessException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isChallengeClosed(){
        try {
            return ChallengeStatus.CLOSED.equals(this.getChallengeStatus());
        } catch (ChallengeDoesNotExistsException e) {
            e.printStackTrace();
            return false;
        }
    }

    /*
    public void setChallengeClosedIfAchieved() {

        try {
            if (this.isGoalAchieved()) {
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
    */

    /**
     * Mark the currently running challenge as closed and sync it to the server.
     * @throws ChallengeDoesNotExistsException
     */
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
    public float getAdultProgress()
            throws ChallengeDoesNotExistsException, PersonDoesNotExistException,
            FitnessException {
        if (this.isDemoMode) {
            return 1f;
        }
        return getPersonProgress(Person.ROLE_PARENT, dateToVisualize);
    }

    public String getAdultStepsString()
            throws PersonDoesNotExistException {
        return getFormattedSteps(this.getAdultSteps());
    }

    public int getAdultSteps() throws PersonDoesNotExistException {
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

    public float getChildProgress()
            throws ChallengeDoesNotExistsException, PersonDoesNotExistException,
            FitnessException {
        if (this.isDemoMode) {
            return 1f;
        }
        return getPersonProgress(Person.ROLE_CHILD, dateToVisualize);
    }

    public int getChildSteps() throws PersonDoesNotExistException {
        if (this.isDemoMode) {
            return 11000;
        }
        return this.getPersonTotalSteps(Person.ROLE_CHILD);
    }

    public String getChildStepsString()
            throws PersonDoesNotExistException {
        return getFormattedSteps(this.getChildSteps());
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

    public float getOverallProgress()
            throws ChallengeDoesNotExistsException, PersonDoesNotExistException,
            FitnessException {
        if (this.isDemoMode) {
            return 1f;
        }
        if (this.calculator == null) {
            throw new ChallengeDoesNotExistsException("Challenge data not initialized");
        } else {
            float familyProgresRaw = calculator.getGroupProgressByDate(dateToVisualize);
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

    public boolean isGoalAchieved()
            throws ChallengeDoesNotExistsException, PersonDoesNotExistException,
            FitnessException {
        return this.getOverallProgress() >= MAX_FITNESS_CHALLENGE_PROGRESS;
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

    /**
     * ASYNCTASKS:
     * Load the Challenge data from the Rest server, and when completed, load the fitness data from
     * Firebase.
     */
    private class LoadChallengeAndFitnessDataAsync extends AsyncTask<Void, Integer, FetchingStatus> {

        String errorMsg = "";

        protected FetchingStatus doInBackground(Void... voids) {
            if (storywell.isServerOnline() == false) {
                this.errorMsg = "No internet connection";
                return FetchingStatus.NO_INTERNET;
            }

            try {
                // Fetch Group data
                group = storywell.getGroup();

                // Fetch Challenge data using getStatus
                challengeManager = storywell.getChallengeManager();
                challengeStatus = challengeManager.getStatus();
                //runningChallenge = challengeManager.getRunningChallenge();
                //unitChallenge = getUnitChallenge(challengeManager);

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

                switch (challengeStatus) {
                    case AVAILABLE:
                        dateToVisualize = WellnessDate.getBeginningOfDay().getTime();
                        status.setValue(FetchingStatus.SUCCESS);
                        break;
                    case UNSYNCED_RUN:
                        //
                    case RUNNING:
                        //
                    case PASSED:
                        runningChallenge = getRunningChallenge();
                        startDate = runningChallenge.getStartDate();
                        endDate = runningChallenge.getEndDate();
                        dateToVisualize = getDateToShow(startDate, endDate);
                        fetchSevenDayFitness(storywell.getGroup(), startDate, endDate);
                        break;
                    case CLOSED:
                        status.setValue(FetchingStatus.SUCCESS);
                        break;
                }

            } else {
                onFetchingFailed(result, this.errorMsg);
            }
        }

        private RunningChallengeInterface getRunningChallenge() {
            try {
                return challengeManager.getRunningChallenge();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
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

    /**
     * ASYNCTASK: Fetch the fitness data of all of the {@link Person} in the {@link Group} from the
     * {@param startDate} to the {@param endDate}.
     * @param group
     * @param startDate
     * @param endDate
     */
    private void fetchSevenDayFitness(GroupInterface group, Date startDate, Date endDate) {
        this.sevenDayFitness = GroupFitness.newInstance(
                new HashMap<Person, MultiDayFitnessInterface>());
        List<Person> members = group.getMembers();
        this.iterateFetchPersonDailyFitness(members.iterator(), startDate, endDate);
    }

    private void iterateFetchPersonDailyFitness(
            Iterator<Person> personIterator, Date startDate, Date endDate) {
        if (personIterator.hasNext()) {
            this.fetchPersonDailyFitness(personIterator, startDate, endDate);
        } else {
            this.onCompletedPersonDailyFitnessIteration();
        }
    }

    private void fetchPersonDailyFitness(final Iterator<Person> personIterator,
                                         final Date startDate, final Date endDate) {
        final Person person = personIterator.next();
        this.fitnessRepository.fetchDailyFitness(
                person, startDate, endDate, new ValueEventListener(){

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                MultiDayFitness multiDayFitness = FitnessRepository
                        .getMultiDayFitness(startDate, endDate, dataSnapshot);
                sevenDayFitness.getGroupFitness().put(person, multiDayFitness);
                iterateFetchPersonDailyFitness(personIterator, startDate, endDate);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                onFetchingFailed(FetchingStatus.FAILED, databaseError.getMessage());
            }
        });
    }

    private void onCompletedPersonDailyFitnessIteration() {
        UnitChallengeInterface unitChallenge = getUnitChallenge(challengeManager);
        this.calculator = new ChallengeProgressCalculator(unitChallenge, sevenDayFitness);
        this.status.setValue(FetchingStatus.SUCCESS);
        Log.d("SWELL", "Fetching fitness data successful.");
    }

    private void onFetchingFailed(FetchingStatus status, String msg) {
        this.status.setValue(status);
        Log.e("SWELL", "Fetching fitness data failed: " + msg);
    }

    /* UNIT METHODS */
    private static UnitChallengeInterface getUnitChallenge(
            ChallengeManagerInterface challengeManager) {
        try {
            switch(challengeManager.getStatus()) {
                case UNSYNCED_RUN:
                    return challengeManager.getUnsyncedChallenge();
                case RUNNING:
                    return challengeManager.getRunningChallenge().getUnitChallenge();
                case PASSED:
                    return challengeManager.getRunningChallenge().getUnitChallenge();
                case CLOSED:
                    return challengeManager.getRunningChallenge().getUnitChallenge();
                default:
                    return null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /* HELPER METHODS */
    private static final Date getDateToShow(Date startDate, Date endDate) {
        Calendar todayCal = WellnessDate.getBeginningOfDay();
        Date today = todayCal.getTime();

        if (startDate.after(today) && endDate.before(today)) {
            return today;
        } else {
            return startDate;
        }
    }
}
