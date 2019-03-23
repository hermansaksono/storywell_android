package edu.neu.ccs.wellness.storytelling.homeview;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by hermansaksono on 9/27/18.
 */

public interface AdventurePresenter {

    /* Interface */
    interface AdventurePresenterListener {
        void goToStoriesTab(String highlightedStoryId);
    }

    /* Methods for View */
    void startGameView();

    void resumeGameView();

    void pauseGameView();

    void stopGameView();

    /* Methods for synchronization */
    void tryFetchChallengeAndFitnessData(Fragment fragment);

    void stopObservingChallengeData(Fragment fragment);

    boolean trySyncFitnessData(Fragment fragment);

    // void stopObservingSync(Fragment fragment);

    /* Methods for animations */
    boolean onTouchOnGameView(MotionEvent event, View view);

    void startPerformBluetoothSync(Fragment fragment);

    void startProgressAnimation();

    void resetProgressAnimation();

    void showControlForProgressInfo(Context context);

    void showControlForFirstCard(Context context);

    void showControlForPrevNext(Context context);

    /* Methods for chapters */
    boolean markCurrentChallengeAsUnlocked(Context context);

    void setAdventureFragmentListener(AdventurePresenterListener listener);
}
