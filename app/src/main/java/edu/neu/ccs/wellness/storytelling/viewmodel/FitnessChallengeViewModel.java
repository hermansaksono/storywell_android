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
import edu.neu.ccs.wellness.fitness.challenges.ChallengeProgress;
import edu.neu.ccs.wellness.fitness.challenges.ChallengeProgressCalculator;
import edu.neu.ccs.wellness.fitness.challenges.RunningChallenge;
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
import edu.neu.ccs.wellness.storytelling.settings.SynchronizedSetting;
import edu.neu.ccs.wellness.storytelling.settings.SynchronizedSettingRepository;
import edu.neu.ccs.wellness.storytelling.sync.FetchingStatus;
import edu.neu.ccs.wellness.utils.WellnessDate;
import edu.neu.ccs.wellness.utils.WellnessStringFormatter;
import edu.neu.ccs.wellness.utils.date.HourMinute;

/**
 * Created by hermansaksono on 5/16/18.
 */

public class FitnessChallengeViewModel extends AndroidViewModel {

    private static final int DEMO_ADULT_GOAL = 6000;
    private static final int DEMO_CHILD_GOAL = 8000;
    private static final int DEMO_ADULT_STEPS = 6100;
    private static final int DEMO_CHILD_STEPS = 8800;
    private static final float DEMO_ADULT_PROGRESS = 1f;
    private static final float DEMO_CHILD_PROGRESS = 1f;

    public static float MAX_FITNESS_CHALLENGE_PROGRESS = 1.0f;
    public static final int ZERO_DATA = 0;
    public static final int NULL_STEPS = -1;

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
     * Returns the date to visualize in the MonitoringView. May return null if the date to
     * visualize is not set yet.
     * @return
     */
    public Date getDateToVisualize() {
        return this.dateToVisualize;
    }

    /**
     * Determines whether the challenged has passed the end datetmine.
     * @return
     */
    public boolean hasChallengePassed() {
        if (this.runningChallenge != null) {
            return this.runningChallenge.isChallengePassed() || hasSoftEndDatePassed();
        } else {
            return false;
        }
    }

    private boolean hasSoftEndDatePassed() {
        Date now = GregorianCalendar.getInstance(Locale.US).getTime();
        SynchronizedSetting setting = storywell.getSynchronizedSetting();
        HourMinute hourMinute = setting.getChallengeEndTime();

        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(endDate);
        endCalendar.set(Calendar.HOUR_OF_DAY, hourMinute.getHour());
        endCalendar.set(Calendar.MINUTE, hourMinute.getHour());
        Date localEndDate = endCalendar.getTime();

        return now.after(localEndDate);
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

    public long getTimeElapsedFromStartToNow() {
        try {
            switch (this.getChallengeStatus()) {
                case AVAILABLE:
                    return -1;
                case UNSYNCED_RUN:
                    // pass
                case RUNNING:
                    // pass
                case PASSED:
                    long now = Calendar.getInstance().getTimeInMillis();
                    return now - this.startDate.getTime();
                default:
                    return -1;
            }
        } catch (ChallengeDoesNotExistsException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /* PUBLIC METHODS FOR GETTING FITNESS PROGRESS AND GOALS */
    public int getAdultSteps() throws PersonDoesNotExistException {
        if (this.isDemoMode) {
            return DEMO_ADULT_STEPS;
        }
        return this.getPersonTotalSteps(Person.ROLE_PARENT);
    }

    public int getAdultGoal() throws ChallengeDoesNotExistsException, PersonDoesNotExistException {
        if (this.isDemoMode) {
            return DEMO_ADULT_GOAL;
        }
        // return (int) this.challengeManager.getRunningChallenge().getUnitChallenge().getGoal();
        return getPersonGoal(Person.ROLE_PARENT, runningChallenge);
    }

    public float getAdultProgress()
            throws ChallengeDoesNotExistsException, PersonDoesNotExistException,
            FitnessException {
        if (this.isDemoMode) {
            return DEMO_ADULT_PROGRESS;
        }
        return getPersonProgress(Person.ROLE_PARENT, dateToVisualize);
    }

    /*
    public String getAdultStepsString() throws PersonDoesNotExistException {
        int steps = this.getAdultSteps();

        if (steps == ZERO_DATA) {
            return STRING_NO_DATA;
        } else {
            return getFormattedSteps(steps);
        }
    }
    */

    /*
    public String getAdultGoalString() {
        try {
            return String.valueOf(this.getAdultGoal());
        } catch (ChallengeDoesNotExistsException e) {
            e.printStackTrace();
        } catch (PersonDoesNotExistException e) {
            e.printStackTrace();
        }
        return STRING_NO_DATA;
    }
    */

    public int getChildSteps() throws PersonDoesNotExistException {
        if (this.isDemoMode) {
            return DEMO_CHILD_STEPS;
        }
        return this.getPersonTotalSteps(Person.ROLE_CHILD);
    }

    public float getChildProgress()
            throws ChallengeDoesNotExistsException, PersonDoesNotExistException,
            FitnessException {
        if (this.isDemoMode) {
            return DEMO_CHILD_PROGRESS;
        }
        return getPersonProgress(Person.ROLE_CHILD, dateToVisualize);
    }

    public int getChildGoal() throws
            ChallengeDoesNotExistsException, PersonDoesNotExistException {
        if (this.isDemoMode) {
            return DEMO_CHILD_GOAL;
        }

        return getPersonGoal(Person.ROLE_CHILD, runningChallenge);
        //return (int) this.challengeManager.getRunningChallenge().getUnitChallenge().getGoal();
    }

    public float getOverallProgress()
            throws ChallengeDoesNotExistsException, FitnessException {
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

    public boolean isGoalAchieved()
            throws ChallengeDoesNotExistsException, PersonDoesNotExistException,
            FitnessException {
        return this.getOverallProgress() >= MAX_FITNESS_CHALLENGE_PROGRESS;
    }

    /* PRIVATE METHODS */
    private int getPersonGoal(String personRoleType, RunningChallengeInterface runningChallenge)
            throws ChallengeDoesNotExistsException, PersonDoesNotExistException{
        Person person = getPerson(personRoleType);

        for (ChallengeProgress challengeProgress: runningChallenge.getChallengeProgress()) {
            if (challengeProgress.getPersonId() == person.getId()) {
                return (int) challengeProgress.getGoal();
            }
        }
        throw new ChallengeDoesNotExistsException(
                "Person with id " + person.getId() + "has no challenge set.");
    }

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
        if (this.sevenDayFitness == null) {
            return NULL_STEPS;
        }

        int steps = 0;
        Person person = getPerson(personRoleType);
        MultiDayFitnessInterface multiDayFitness = this.sevenDayFitness
                .getAPersonMultiDayFitness(person);

        if (multiDayFitness.getDailyFitness().isEmpty()) {
            return NULL_STEPS;
        }

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
                Log.d("SWELL", "Loading challenge and fitness data ...");

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
                        runningChallenge = getRunningChallenge();
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

        // TODO this is temporary
        Calendar startDateCal = Calendar.getInstance(Locale.US);
        startDateCal.setTime(startDate);
        startDateCal = WellnessDate.getResetToBeginningOfDay(startDateCal);
        final Date startDateBeginning = startDateCal.getTime();
        // end temporary

        this.fitnessRepository.fetchDailyFitness(
                person, startDateBeginning, endDate, new ValueEventListener(){

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
        // UnitChallengeInterface unitChallenge = getUnitChallenge(challengeManager);
        // this.calculator = new ChallengeProgressCalculator(unitChallenge, sevenDayFitness);
        RunningChallengeInterface runningChallenge = getRunningChallenge(challengeManager);
        this.calculator = new ChallengeProgressCalculator(runningChallenge, sevenDayFitness);
        this.status.setValue(FetchingStatus.SUCCESS);
        Log.d("SWELL", "Loading fitness data successful.");
    }

    private void onFetchingFailed(FetchingStatus status, String msg) {
        this.status.setValue(status);
        Log.e("SWELL", "Loading fitness data failed: " + msg);
    }

    /* UNIT METHODS */
    private static RunningChallengeInterface getRunningChallenge(
            ChallengeManagerInterface challengeManager) {
        try {
            switch(challengeManager.getStatus()) {
                case UNSYNCED_RUN:
                    // return challengeManager.getUnsyncedChallenge();
                    return null; // TODO handle this
                case RUNNING:
                    return challengeManager.getRunningChallenge();
                case PASSED:
                    return challengeManager.getRunningChallenge();
                case CLOSED:
                    return challengeManager.getRunningChallenge();
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
            Calendar startDatCcalendar = GregorianCalendar.getInstance(Locale.US);
            startDatCcalendar.setTime(startDate);
            return WellnessDate.getResetToBeginningOfDay(startDatCcalendar).getTime();
        }
    }
}
