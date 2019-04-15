package edu.neu.ccs.wellness.storytelling.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.SyncContext;
import android.support.annotation.NonNull;

import edu.neu.ccs.wellness.logging.WellnessUserLogging;
import edu.neu.ccs.wellness.storytelling.Storywell;
import edu.neu.ccs.wellness.storytelling.sync.FitnessSync;
import edu.neu.ccs.wellness.storytelling.sync.SyncStatus;
import edu.neu.ccs.wellness.storytelling.utils.StorywellPerson;
import edu.neu.ccs.wellness.storytelling.sync.FitnessSync.OnFitnessSyncProcessListener;
import edu.neu.ccs.wellness.storytelling.utils.UserLogging;

/**
 * Created by hermansaksono on 7/19/18.
 */

public class FitnessSyncViewModel extends AndroidViewModel
        implements OnFitnessSyncProcessListener {

    private WellnessUserLogging userLogging;
    private FitnessSync fitnessSync;
    private MutableLiveData<SyncStatus> status = null;


    /* CONSTRUCTOR*/
    public FitnessSyncViewModel(@NonNull Application application) {
        super(application);
        this.fitnessSync = new FitnessSync(application, this);
    }

    /* PUBLIC METHODS*/
    /**
     * Get the live data
     * @return
     */
    public LiveData<SyncStatus> getLiveStatus() {
        if (this.status == null) {
            this.status = new MutableLiveData<>();
            this.status.setValue(SyncStatus.UNINITIALIZED);
        }
        return this.status;
    }

    /**
     * Connect to the fitness tracker, download the data from the tracker, and upload the data
     * to the repository. These steps are performed to each of the members of Group. The data must be
     * downloaded starting from startDate that is unique to every user.
     * @return
     */
    public boolean perform() {
        Storywell storywell = new Storywell(this.getApplication());

        if (storywell.getSynchronizedSetting().isDemoMode()) {
            this.onSetUpdate(SyncStatus.COMPLETED);
            return true;
        }

        if (this.status != null) {
            UserLogging.logStartBleSync();
            this.fitnessSync.perform(storywell.getGroup());
            return true;
        } else {
            return false;
        }
    }

    /**
     * Perform synchronization on the next person in queue.
     */
    public boolean performNext () {
        return this.fitnessSync.performNext();
    }

    /**
     * Stop synchronization.
     * @return
     */
    public void stop() {
        this.fitnessSync.stop();
    }

    /**
     * Get the person whose data is currently being fetched.
     * @return @StorywellPerson Person who is currently being synchronized.
     */
    public StorywellPerson getCurrentPerson() {
        return this.fitnessSync.getCurrentPerson();
    }

    @Override
    public void onSetUpdate(SyncStatus syncStatus) {
        this.status.setValue(syncStatus);
        if (SyncStatus.COMPLETED.equals(syncStatus)) {
            UserLogging.logStopBleSync(true);
        } else if (SyncStatus.FAILED.equals(syncStatus)) {
            UserLogging.logStopBleSync(false);
        }
    }

    @Override
    public void onPostUpdate(SyncStatus syncStatus) {
        this.status.postValue(syncStatus);
    }
}