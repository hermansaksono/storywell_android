package edu.neu.ccs.wellness.storytelling.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.support.annotation.NonNull;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import edu.neu.ccs.wellness.reflection.ResponsePile;
import edu.neu.ccs.wellness.reflection.ResponsePileListFactory;
import edu.neu.ccs.wellness.storytelling.Storywell;
import edu.neu.ccs.wellness.storytelling.homeview.TreasureListLiveData;

/**
 * Created by hermansaksono on 1/16/19.
 */

public class TreasureListViewModel extends AndroidViewModel {

    private TreasureListLiveData treasureListLiveData;

    public TreasureListViewModel(Application application) {
        super(application);
    }

    @NonNull
    public LiveData<List<ResponsePile>> getTreasureListLiveData() {
        if (treasureListLiveData == null) {
            this.treasureListLiveData = getLiveData(this.getApplication());
        }
        return treasureListLiveData;
    }

    private static TreasureListLiveData getLiveData(Context context) {
        DatabaseReference firebaseDbRef = FirebaseDatabase.getInstance().getReference();
        Storywell storywell = new Storywell(context);
        String groupName = storywell.getGroup().getName();
        return new TreasureListLiveData(firebaseDbRef
                .child(ResponsePileListFactory.FIREBASE_REFLECTION_PILE)
                .child(groupName));
    }
}
