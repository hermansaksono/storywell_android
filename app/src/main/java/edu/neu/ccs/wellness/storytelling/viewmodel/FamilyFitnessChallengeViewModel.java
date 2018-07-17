package edu.neu.ccs.wellness.storytelling.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;

import java.io.IOException;
import java.util.Date;

import edu.neu.ccs.wellness.fitness.challenges.ChallengeDoesNotExistsException;
import edu.neu.ccs.wellness.fitness.challenges.ChallengeProgressCalculator;
import edu.neu.ccs.wellness.fitness.interfaces.ChallengeManagerInterface;
import edu.neu.ccs.wellness.fitness.interfaces.ChallengeStatus;
import edu.neu.ccs.wellness.fitness.interfaces.FitnessException;
import edu.neu.ccs.wellness.fitness.interfaces.FitnessRepositoryInterface;
import edu.neu.ccs.wellness.fitness.interfaces.GroupFitnessInterface;
import edu.neu.ccs.wellness.fitness.interfaces.UnitChallengeInterface;
import edu.neu.ccs.wellness.people.Group;
import edu.neu.ccs.wellness.people.Person;
import edu.neu.ccs.wellness.people.PersonDoesNotExistException;
import edu.neu.ccs.wellness.server.RestServer;
import edu.neu.ccs.wellness.server.RestServer.ResponseType;
import edu.neu.ccs.wellness.storytelling.Storywell;

/**
 * Created by hermansaksono on 5/16/18.
 */

public class FamilyFitnessChallengeViewModel extends AndroidViewModel {

    public static float MAX_FITNESS_CHALLENGE_PROGRESS = 1.0f;

    private MutableLiveData<RestServer.ResponseType> status = null;

    Storywell storywell;
    private Date startDate;
    private Date endDate;
    private Group group;
    private GroupFitnessInterface sevenDayFitness;
    private FitnessRepositoryInterface fitnessRepository;
    private ChallengeManagerInterface challengeManager;
    private ChallengeStatus challengeStatus;
    private UnitChallengeInterface unitChallenge;
    private ChallengeProgressCalculator calculator = null;

    /* CONSTRUCTOR */
    public FamilyFitnessChallengeViewModel(Application application) {
        super(application);
        this.storywell = new Storywell(getApplication());
        this.fitnessRepository = storywell.getFitnessManager();
        this.challengeManager = storywell.getChallengeManager();
    }

    /* PUBLIC METHODS */
    public LiveData<ResponseType> fetchSevenDayFitness(Date startDate, Date endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
        if (this.status == null) {
            this.status = new MutableLiveData<>();
            this.status.setValue(ResponseType.FETCHING);
            loadSevenDayFitness();
        }
        return this.status;
    }

    public ResponseType getFetchingStatus() {
        if (status != null) {
            return status.getValue();
        } else {
            return ResponseType.UNINITIALIZED;
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

    public float getAdultProgress(Date date)
            throws ChallengeDoesNotExistsException, PersonDoesNotExistException,
            FitnessException {
        return 0.6f;//getPersonProgress(Person.ROLE_PARENT, date);
    }

    public float getChildProgress(Date date)
            throws ChallengeDoesNotExistsException, PersonDoesNotExistException,
            FitnessException {
        return 0.8f;//getPersonProgress(Person.ROLE_CHILD, date);
    }

    public float getOverallProgress(Date date)
            throws ChallengeDoesNotExistsException, PersonDoesNotExistException,
            FitnessException {
        if (this.calculator == null) {
            throw new ChallengeDoesNotExistsException("Challenge data not initialized");
        } else {
            //float familyProgresRaw = calculator.getGroupProgressByDate(date); // TODO Uncomment this on production
            return 0.7f;//Math.min(MAX_FITNESS_CHALLENGE_PROGRESS, familyProgresRaw); // TODO Uncomment this on production
        }
    }

    /* PRIVATE METHODS */
    private void loadSevenDayFitness() {
        new LoadFamilyFitnessChallengeAsync().execute();
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

    private Person getPerson(String personRoleType) throws PersonDoesNotExistException {
        for (Person personInList : group.getMembers()) {
            if (personInList.isRole(personRoleType)) {
                return personInList;
            }
        }
        throw new PersonDoesNotExistException("Person is not on the list");
    }

    /* ASYNCTASKS */
    private class LoadFamilyFitnessChallengeAsync extends AsyncTask<Void, Integer, ResponseType> {

        protected ResponseType doInBackground(Void... voids) {
            if (storywell.isServerOnline() == false) {
                return ResponseType.NO_INTERNET;
            }

            try {
                // Fetch Group data
                Log.d("SWELL", "Fetching group data");
                group = storywell.getGroup();
                Log.d("SWELL", "Group data fetched: " + group.toString());

                // Fetch Challenge data using getStatus
                Log.d("SWELL", "Fetching Challenge data ...");
                challengeStatus = challengeManager.getStatus();
                unitChallenge = getUnitChallenge(challengeManager);
                Log.d("SWELL", "Challenge data fetched: " + challengeStatus.toString());

                // Fetch seven day fitness data
                Log.d("SWELL", "Fetching Fitness data ...");
                sevenDayFitness = fitnessRepository.getMultiDayFitness(startDate, endDate);
                calculator = new ChallengeProgressCalculator(unitChallenge, sevenDayFitness);
                Log.d("SWELL", "Fetching Fitness data successful");

                // Return success
                return ResponseType.SUCCESS_202;
            } catch (JSONException e) {
                e.printStackTrace();
                return ResponseType.BAD_JSON;
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseType.BAD_REQUEST_400;
            } catch (FitnessException e) {
                e.printStackTrace();
                return ResponseType.BAD_JSON;
            }
        }

        protected void onPostExecute(ResponseType result) {
            status.setValue(result);
        }
    }

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
