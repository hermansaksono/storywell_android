package edu.neu.ccs.wellness.storytelling.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.support.annotation.NonNull;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import edu.neu.ccs.wellness.fitness.ChallengesStateRepository;
import edu.neu.ccs.wellness.reflection.ResponsePile;
import edu.neu.ccs.wellness.reflection.ResponsePileListFactory;
import edu.neu.ccs.wellness.storytelling.Storywell;
import edu.neu.ccs.wellness.storytelling.homeview.TreasureListLiveData;
import edu.neu.ccs.wellness.storytelling.storyview.CompletedChallengesLiveData;

/**
 * Created by hermansaksono on 1/22/19.
 */

public class CompletedChallengesViewModel extends AndroidViewModel {

    private CompletedChallengesLiveData completedChallenges;

    public CompletedChallengesViewModel(Application application) {
        super(application);
    }

    @NonNull
    public LiveData<List<String>> getTreasureListLiveData() {
        if (completedChallenges == null) {
            this.completedChallenges = getLiveData(this.getApplication());
        }
        return this.completedChallenges;
    }

    private static CompletedChallengesLiveData getLiveData(Context context) {
        DatabaseReference firebaseDbRef = FirebaseDatabase.getInstance().getReference();
        Storywell storywell = new Storywell(context);
        String groupName = storywell.getGroup().getName();
        return new CompletedChallengesLiveData(firebaseDbRef
                .child(ChallengesStateRepository.FIREBASE_COMPLETED_CHALLENGES)
                .child(groupName));
    }
}
