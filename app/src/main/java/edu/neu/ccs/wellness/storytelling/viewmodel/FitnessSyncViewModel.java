package edu.neu.ccs.wellness.storytelling.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import edu.neu.ccs.wellness.people.Group;
import edu.neu.ccs.wellness.storytelling.sync.FitnessSync;
import edu.neu.ccs.wellness.storytelling.utils.StorywellPerson;
import edu.neu.ccs.wellness.storytelling.sync.FitnessSync.OnFitnessSyncProcessListener;

/**
 * Created by hermansaksono on 7/19/18.
 */

public class RefactoredFitnessSyncViewModel extends AndroidViewModel
        implements OnFitnessSyncProcessListener {

    private FitnessSync fitnessSync;

    private MutableLiveData<SyncStatus> status = null;


    /* CONSTRUCTOR*/
    public RefactoredFitnessSyncViewModel(@NonNull Application application) {
        super(application);
        this.fitnessSync = new FitnessSync(application, this);
    }

    /* PUBLIC METHODS*/
    /**
     * Connect to fitness trackers, download the data from the tracker, and upload it to the
     * repository. These steps are performed to each of the members of Group. The data must be
     * downloaded starting from startDate that is unique to every user.
     * @param group
     * @return
     */
    public LiveData<SyncStatus> perform(Group group) {
        if (this.status == null) {
            this.status = new MutableLiveData<>();
            this.status.setValue(SyncStatus.UNINITIALIZED);
        }
        this.fitnessSync.perform(group);
        return this.status;
    }

    /**
     * Perform synchronization on the next person in queue.
     */
    public boolean performNext () {
        return this.fitnessSync.performNext();
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
    }

    @Override
    public void onPostUpdate(SyncStatus syncStatus) {
        this.status.postValue(syncStatus);
    }
}