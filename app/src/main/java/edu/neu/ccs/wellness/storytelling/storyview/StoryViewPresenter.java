package edu.neu.ccs.wellness.storytelling.storyview;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import java.util.List;

import edu.neu.ccs.wellness.storytelling.viewmodel.CompletedChallengesViewModel;

/**
 * Created by hermansaksono on 1/23/19.
 */

public class StoryViewPresenter {

    private CompletedChallengesViewModel completedChallengesViewModel;
    private List<String> completedChallenges;

    public StoryViewPresenter(final FragmentActivity activity) {
        this.completedChallengesViewModel = ViewModelProviders.of(activity)
                .get(CompletedChallengesViewModel.class);
        this.completedChallengesViewModel.getTreasureListLiveData()
                .observe(activity, new Observer<List<String>>() {
            @Override
            public void onChanged(@Nullable List<String> updatedCompletedChallenges) {
                completedChallenges = updatedCompletedChallenges;
            }
        });
    }

    public boolean isCompletedChallengesListReady() {
        return this.completedChallenges != null;
    }

    public boolean isThisChallengeCompleted(String challengeId) {
        return this.completedChallenges.contains(challengeId);
    }
}
