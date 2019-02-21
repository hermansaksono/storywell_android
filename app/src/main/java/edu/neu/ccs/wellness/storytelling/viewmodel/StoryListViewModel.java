package edu.neu.ccs.wellness.storytelling.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import edu.neu.ccs.wellness.server.RestServer;
import edu.neu.ccs.wellness.story.interfaces.StoryInterface;
import edu.neu.ccs.wellness.storytelling.Storywell;
import edu.neu.ccs.wellness.storytelling.settings.SynchronizedSetting;
import edu.neu.ccs.wellness.storytelling.settings.SynchronizedSetting.StoryListInfo;
import edu.neu.ccs.wellness.storytelling.settings.SynchronizedSettingRepository;

/**
 * Created by hermansaksono on 5/16/18.
 */

public class StoryListViewModel extends AndroidViewModel {

    private Storywell storywell;
    private MutableLiveData<List<StoryInterface>> storiesLiveData;
    private MutableLiveData<StoryListInfo> metadataLiveData;
    private SynchronizedSetting.StoryListInfo nonLiveMetadata;
    private DatabaseReference firebaseSettingDbRef;

    public StoryListViewModel(Application application) {
        super(application);
        this.storywell =  new Storywell(getApplication());
        this.firebaseSettingDbRef = SynchronizedSettingRepository
                .getDefaultFirebaseRepository(application.getApplicationContext());
    }

    /**
     * Get the list of {@link StoryInterface}.
     * @return
     */
    public LiveData<List<StoryInterface>> getStories() {
        if (this.storiesLiveData == null) {
            this.storiesLiveData = new MutableLiveData<>();
            this.loadStories();
        }
        return this.storiesLiveData;
    }

    private void loadStories() {
        new LoadStoryListAsync().execute();
    }

    private class LoadStoryListAsync extends AsyncTask<Void, Integer, RestServer.ResponseType> {

        protected RestServer.ResponseType doInBackground(Void... voids) {
            if (storywell.isServerOnline()) {
                storywell.loadStoryList();
                return RestServer.ResponseType.SUCCESS_202;
            } else {
                return RestServer.ResponseType.NO_INTERNET;
            }
        }

        protected void onPostExecute(RestServer.ResponseType result) {
            if (result == RestServer.ResponseType.SUCCESS_202) {
                refreshStoryListMetadata();
            } else if (result == RestServer.ResponseType.NO_INTERNET) {
                // DO NOTHING
            }
        }

    }

    private void refreshStoryListMetadata() {
        this.firebaseSettingDbRef.child("storyListInfo")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        nonLiveMetadata = refreshStoryListMetadata(dataSnapshot);
                        storiesLiveData.setValue(storywell.getStoryList());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        nonLiveMetadata = new SynchronizedSetting.StoryListInfo();
                        storiesLiveData.setValue(storywell.getStoryList());
                    }
                });
    }

    private SynchronizedSetting.StoryListInfo refreshStoryListMetadata(DataSnapshot dataSnapshot) {
        if (dataSnapshot.exists()) {
            return dataSnapshot.getValue(SynchronizedSetting.StoryListInfo.class);
        } else {
            return new SynchronizedSetting.StoryListInfo();
        }
    }

    /**
     * Get the non-live story list metadata. This is not the fresh metadata because it is refreshed
     * only when the storyList is updated. During the user's interaction with the app, the metadata
     * can change but this variable will not be updated.
     * @return
     */
    public StoryListInfo getNonLiveMetadata() {
        return this.nonLiveMetadata;
    }

    /**
     * Get the metadata for the list of {@link StoryInterface}.
     * @return
     */
    public LiveData<StoryListInfo> getMetadata() {
        if (this.metadataLiveData == null) {
            this.metadataLiveData = new MutableLiveData<>();
            this.metadataLiveData.setValue(new SynchronizedSetting.StoryListInfo());
            this.loadMetadata();
        }
        return this.metadataLiveData;
    }

    private void loadMetadata() {
        this.firebaseSettingDbRef.child("storyListInfo")
                //.addListenerForSingleValueEvent(new ValueEventListener() {
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        metadataLiveData.setValue(refreshStoryListMetadata(dataSnapshot));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("SWELL",
                                "Error refreshing story metadata: "
                                        + databaseError.getDetails());
                    }
                });
    }

    /**
     * Remove the given story from the user's list of unread stories.
     * @param storyId
     */
    public void removeStoryFromUnread(String storyId) {
        this.metadataLiveData.getValue().getUnreadStories().remove(storyId);
        this.firebaseSettingDbRef.child("storyListInfo").setValue(this.metadataLiveData.getValue());
    }
}
