package edu.neu.ccs.wellness.storytelling.homeview;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by hermansaksono on 9/27/18.
 */

public interface AdventurePresenter {

    /* Methods for View */
    void startGameView();

    void resumeGameView();

    void pauseGameView();

    void stopGameView();

    /* Methods for synchronization */
    void tryFetchFitnessChallengeData(Fragment fragment);

    boolean trySyncFitnessData(Fragment fragment);

    /* Methods for animations */
    boolean processTapOnGameView(MotionEvent event, View view);

    void startPerformProgressAnimation(Fragment fragment);

    void startProgressAnimation();

    void resetProgressAnimation();

    void showControlForProgressInfo(Context context);

    void showControlForFirstCard(Context context);

    void showControlForPrevNext(Context context);
}