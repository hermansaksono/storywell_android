package edu.neu.ccs.wellness.story;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import edu.neu.ccs.wellness.storytelling.Storywell;

/**
 * Created by hermansaksono on 3/1/19.
 */

public class CalmingReflectionViewModel extends AndroidViewModel {

    private static final String KEY_PATH = "app_calming_pile";

    private MutableLiveData<CalmingReflectionGroup> reflectionGroupLiveData;
    private boolean isExists = false;

    public CalmingReflectionViewModel(Application application) {
        super(application);
    }

    /**
     * Get a {@link LiveData} that contains the requested {@link CalmingReflectionGroup}.
     * @param reflGroupId
     * @return
     */
    public LiveData<CalmingReflectionGroup> getReflectionGroup(String reflGroupId) {
        if (this.reflectionGroupLiveData == null) {
            this.reflectionGroupLiveData = new MutableLiveData<>();
            this.fetchReflectionGroup(reflGroupId);
        }
        return this.reflectionGroupLiveData;
    }

    private void fetchReflectionGroup(String reflGroupId) {
        getDefaultFirebaseRepository(this.getApplication().getApplicationContext())
                .child(reflGroupId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        processReflGroupDS(dataSnapshot);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void processReflGroupDS(DataSnapshot dataSnapshot) {
        if (dataSnapshot.exists()) {
            this.isExists = false;
            this.reflectionGroupLiveData.setValue(
                    dataSnapshot.getValue(CalmingReflectionGroup.class));
        } else {
            this.isExists = false;
        }
    }

    /**
     * Determines whether the requested {@link CalmingReflectionGroup} exists in the server.
     * @return
     */
    public boolean isReflectionGroupExists() {
        return this.isExists;
    }

    /* HELPER FUNCTIONS */
    public static DatabaseReference getDefaultFirebaseRepository(Context context) {
        String uid = new Storywell(context).getGroup().getName();
        DatabaseReference firebaseDbRef = FirebaseDatabase.getInstance().getReference()
                .child(KEY_PATH).child(uid);
        return firebaseDbRef;
    }
}
