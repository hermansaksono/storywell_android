package edu.neu.ccs.wellness.storytelling.homeview;

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.ViewAnimator;

import org.json.JSONException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;

import edu.neu.ccs.wellness.fitness.challenges.ChallengeDoesNotExistsException;
import edu.neu.ccs.wellness.fitness.interfaces.ChallengeStatus;
import edu.neu.ccs.wellness.fitness.interfaces.FitnessException;
import edu.neu.ccs.wellness.logging.Param;
import edu.neu.ccs.wellness.logging.WellnessUserLogging;
import edu.neu.ccs.wellness.people.Person;
import edu.neu.ccs.wellness.people.PersonDoesNotExistException;
import edu.neu.ccs.wellness.storytelling.MonitoringActivity;
import edu.neu.ccs.wellness.storytelling.R;
import edu.neu.ccs.wellness.storytelling.Storywell;
import edu.neu.ccs.wellness.storytelling.monitoringview.HeroSprite;
import edu.neu.ccs.wellness.storytelling.monitoringview.MonitoringController;
import edu.neu.ccs.wellness.storytelling.monitoringview.MonitoringView;
import edu.neu.ccs.wellness.storytelling.monitoringview.interfaces.GameLevelInterface;
import edu.neu.ccs.wellness.storytelling.monitoringview.interfaces.OnAnimationCompletedListener;
import edu.neu.ccs.wellness.storytelling.utils.StorywellPerson;
import edu.neu.ccs.wellness.storytelling.viewmodel.FitnessSyncViewModel;
import edu.neu.ccs.wellness.storytelling.sync.SyncStatus;
import edu.neu.ccs.wellness.storytelling.sync.FetchingStatus;
import edu.neu.ccs.wellness.storytelling.viewmodel.FitnessChallengeViewModel;
import edu.neu.ccs.wellness.utils.WellnessDate;

/**
 * Created by hermansaksono on 6/11/18.
 */

public class HomeAdventurePresenter implements AdventurePresenter {

    public static final boolean IS_DEMO_MODE = false;

    public static final int CONTROL_PLAY = 0;
    public static final int CONTROL_SYNCING = 1;
    public static final int CONTROL_READY = 2;
    public static final int CONTROL_PROGRESS_INFO = 3;
    public static final int CONTROL_SYNCING_CAREGIVER = 4;
    public static final int CONTROL_SYNCING_CHILD = 5;
    public static final int CONTROL_PREV_NEXT = 6;
    public static final int CONTROL_CLOSED = 7;
    public static final int CONTROL_MISSED = 8;
    public static final int CONTROL_NO_RUNNING = 9;
    public static final String DATE_FORMAT_STRING = "EEE, MMM d";
    private static final String LOG_TAG = "SWELL-ADV";
    private final int heroId;

    private GregorianCalendar today;
    private GregorianCalendar startDate;
    private GregorianCalendar endDate;
    private ProgressAnimationStatus progressAnimationStatus = ProgressAnimationStatus.UNREADY;
    private SyncStatus fitnessSyncStatus = SyncStatus.UNINITIALIZED;
    private boolean isSyncronizingFitnessData;
    private Storywell storywell;

    private View rootView;
    private ViewAnimator gameviewViewAnimator;
    private ViewAnimator controlViewAnimator;

    private FitnessChallengeViewModel fitnessChallengeViewModel;
    private FitnessSyncViewModel fitnessSyncViewModel;
    private MonitoringController gameController;
    private MonitoringView gameView;

    /* CONSTRUCTOR */
    public HomeAdventurePresenter(View rootView) {
        /* Basic data */
        this.today = WellnessDate.getTodayDate();
        //this.today = getDummyDate();
        this.startDate = WellnessDate.getFirstDayOfWeek(this.today);
        this.endDate = WellnessDate.getEndDate(this.startDate);
        this.isSyncronizingFitnessData = false;
        this.storywell = new Storywell(rootView.getContext());
        this.heroId = R.drawable.hero_dora;

        /* Views */
        this.rootView = rootView;
        this.gameviewViewAnimator = this.rootView.findViewById(R.id.gameview_view_animator);
        this.controlViewAnimator = this.rootView.findViewById(R.id.control_view_animator);
        this.gameView = rootView.findViewById(R.id.layout_monitoringView);

        /* Set the date */
        TextView dateTextView = this.rootView.findViewById(R.id.textview_date);
        dateTextView.setText(new SimpleDateFormat(DATE_FORMAT_STRING, Locale.US)
                .format(this.today.getTime()));

        /* Game's Controller */
        this.gameController = getGameController(this.gameView, this.heroId);
    }

    /* BUTTON AND TAP METHODS */
    @Override
    public boolean processTapOnGameView(MotionEvent event, View view) {
        if (this.progressAnimationStatus == ProgressAnimationStatus.READY) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (this.gameView.isOverHero(event)) {
                    this.tryStartProgressAnimation();
                    this.showControlForProgressInfo(view.getContext());
                }
            }
        }
        return true;
    }

    @Override
    public void startProgressAnimation() {
        if (this.progressAnimationStatus == ProgressAnimationStatus.READY) {
            this.tryStartProgressAnimation();
        }
    }

    @Override
    public void resetProgressAnimation() {
        if (this.progressAnimationStatus != ProgressAnimationStatus.READY) {
            this.doResetProgressAnimation();
        }
    }

    @Override
    public void startPerformProgressAnimation(Fragment fragment) {
        if (IS_DEMO_MODE) {
            this.showControlForReady(fragment.getContext());
            return;
        }
        WellnessUserLogging userLogging = new WellnessUserLogging(storywell.getGroup().getName());
        Bundle bundle = new Bundle();
        bundle.putString(Param.BUTTON_NAME, "PLAY_ANIMATION");
        userLogging.logEvent("PLAY_ANIMATION_BUTTON_CLICK", bundle);

        if (SyncStatus.NO_NEW_DATA.equals(this.fitnessSyncStatus)) {
            this.showControlForReady(fragment.getContext());
        } else if (SyncStatus.NEW_DATA_AVAILABLE.equals(this.fitnessSyncStatus)){
            this.trySyncFitnessData(fragment);
            this.showControlForSyncing(fragment.getContext());
        } else if (SyncStatus.COMPLETED.equals(this.fitnessSyncStatus)) {
            this.showControlForReady(fragment.getContext());
        } else if (SyncStatus.FAILED.equals(this.fitnessSyncStatus)) {
            this.showControlForFailure(fragment.getContext());
        }  else {
            this.showControlForSyncing(fragment.getContext());
        }
    }

    @Override
    public void showControlForFirstCard(Context context) {
        this.setContolChangeToMoveRight(context);
        controlViewAnimator.setDisplayedChild(CONTROL_PLAY);
    }

    public void showControlForSyncing(Context context) {
        this.setContolChangeToMoveLeft(context);
        controlViewAnimator.setDisplayedChild(CONTROL_SYNCING);
    }

    private void showControlForSyncingThisPerson(View view, StorywellPerson storywellPerson) {
        try {
            ChallengeStatus status = fitnessChallengeViewModel.getChallengeStatus();
            this.showControlForSyncingByChallengeStatus(view, storywellPerson, status);
        } catch (ChallengeDoesNotExistsException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "Challenge does not exist");
        }
    }

    private void showControlForSyncingByChallengeStatus(View view, StorywellPerson storywellPerson, ChallengeStatus status) {
        switch(status) {
            case AVAILABLE:
                break;
            case UNSYNCED_RUN:
                // PASS to show Sync control
            case RUNNING:
                this.showControlForSyncingForRunningChallenge(view, storywellPerson);
                break;
            case PASSED:
                break;
            case CLOSED:
                break;
            default:
                break;
        }
    }

    private void showControlForSyncingForRunningChallenge(View view, StorywellPerson storywellPerson) {
        if (this.controlViewAnimator.getDisplayedChild() != CONTROL_PLAY) {
            if (Person.ROLE_PARENT.equals(storywellPerson.getPerson().getRole())) {
                this.showControlForSyncingCaregiver(view, storywellPerson.getPerson().getName());
            } else {
                this.showControlForSyncingChild(view, storywellPerson.getPerson().getName());
            }
        }
    }

    public void showControlForSyncingCaregiver(View view, String name) {
        this.setContolChangeToMoveLeft(view.getContext());
        this.controlViewAnimator.setDisplayedChild(CONTROL_SYNCING_CAREGIVER);

        String text = (String) view.getResources().getText(R.string.adventure_info_syncing_caregiver);
        TextView textView = view.findViewById(R.id.text_syncing_caregiver_info);

        textView.setText(String.format(text, name));
    }

    public void showControlForSyncingChild(View view, String name) {
        this.setContolChangeToMoveLeft(view.getContext());
        this.controlViewAnimator.setDisplayedChild(CONTROL_SYNCING_CHILD);

        String text = (String) view.getResources().getText(R.string.adventure_info_syncing_child);
        TextView textView = view.findViewById(R.id.text_syncing_child_info);

        textView.setText(String.format(text, name));
    }

    public void showControlForReady(Context context) {
        this.setContolChangeToMoveLeft(context);
        controlViewAnimator.setDisplayedChild(CONTROL_READY);
    }

    public void showControlForProgressInfo(Context context) {
        this.setContolChangeToMoveLeft(context);
        controlViewAnimator.setDisplayedChild(CONTROL_PROGRESS_INFO);
    }

    @Override
    public void showControlForPrevNext(Context context) {
        this.setContolChangeToMoveRight(context);
        controlViewAnimator.setDisplayedChild(CONTROL_PREV_NEXT);
    }

    public void showControlForFailure(Context context) {
        this.setContolChangeToMoveRight(context);
        controlViewAnimator.setDisplayedChild(CONTROL_PREV_NEXT);
    }

    public void showControlForAchieved(Context context) {
        this.setContolChangeToMoveRight(context);
        controlViewAnimator.setDisplayedChild(CONTROL_CLOSED);
    }

    public void showControlForMissed(Context context) {
        this.setContolChangeToMoveRight(context);
        controlViewAnimator.setDisplayedChild(CONTROL_MISSED);
    }

    public void showControlForMotivation(Context context) {
        this.setContolChangeToMoveRight(context);
        // TODO HS: controlViewAnimator.setDisplayedChild(CONTROL_MISSED);
    }

    public void showAlternateExit(Context context) {
        this.setContolChangeToMoveRight(context);
        // TODO HS: controlViewAnimator.setDisplayedChild(CONTROL_MISSED);
    }

    public void showNoAvailableChallenges(Context context) {
        if (this.controlViewAnimator.getDisplayedChild() != CONTROL_NO_RUNNING) {
            this.setGameviewVisorIsVisible(false);
            this.setHeroIsVisible(false);
            this.setContolChangeToMoveRight(context);
            this.controlViewAnimator.setDisplayedChild(CONTROL_NO_RUNNING);
        }
    }

    private void setGameviewVisorIsVisible(boolean isVisible) {
        if (IS_DEMO_MODE) {
            return;
        }
        View view = this.rootView.findViewById(R.id.gameview_visor);
        if (isVisible) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.INVISIBLE);
        }
    }

    private void setHeroIsVisible(boolean isVisible) {
        if (IS_DEMO_MODE) {
            return;
        }
        this.gameController.setHeroIsVisible(isVisible);
    }

    /*
    private void doChangeHeroVisibilityByStatus(ChallengeStatus status) {
        if (IS_DEMO_MODE) {
            return;
        }
        if (ChallengeStatus.AVAILABLE.equals(status)) {
            this.gameController.setHeroIsVisible(false);
        } else {
            this.gameController.setHeroIsVisible(true);
        }
    }
    */

    private void updateGroupStepsProgress() {
        try {
            TextView adultStepsTextview = this.rootView.findViewById(R.id.textview_progress_adult);
            TextView childStepsTextview = this.rootView.findViewById(R.id.textview_progress_child);
            adultStepsTextview.setText(this.fitnessChallengeViewModel.getAdultStepsString(today));
            childStepsTextview.setText(this.fitnessChallengeViewModel.getChildStepsString(today));
        } catch (PersonDoesNotExistException e) {
            e.printStackTrace();
        }

    }

    private void updateGroupGoal() {
        try {
            TextView adultGoalTextview = this.rootView.findViewById(R.id.textview_progress_adult_goal);
            TextView childGoalTextview = this.rootView.findViewById(R.id.textview_progress_child_goal);
            adultGoalTextview.setText(this.fitnessChallengeViewModel.getAdultGoalString());
            childGoalTextview.setText(this.fitnessChallengeViewModel.getChildGoalString());
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /* VIEW ANIMATOR METHODS */
    private void setContolChangeToMoveLeft(Context context) {
        controlViewAnimator.setInAnimation(context, R.anim.view_move_left_next);
        controlViewAnimator.setOutAnimation(context, R.anim.view_move_left_current);
    }

    private void setContolChangeToMoveRight(Context context) {
        controlViewAnimator.setInAnimation(context, R.anim.view_move_right_prev);
        controlViewAnimator.setOutAnimation(context, R.anim.view_move_right_current);
        //this.gameController.setHeroIsVisible(false);
    }

    /* GAMEVIEW METHODS */
    @Override
    public void startGameView() {
        this.gameController.start();
    }

    @Override
    public void resumeGameView() {
        this.gameController.resume();
    }

    @Override
    public void pauseGameView() {
        this.gameController.pause();
    }

    @Override
    public void stopGameView() {
        this.gameController.stop();
    }

    /* PROGRESS ANIMATION METHODS */
    private void tryStartProgressAnimation() {
        if (isFitnessAndChallengeDataFetched()) {
            doStartProgressAnimation();
        }
    }

    private void doStartProgressAnimation() {
        try {
            float adultProgress = this.fitnessChallengeViewModel.getAdultProgress(today);
            float childProgress = this.fitnessChallengeViewModel.getChildProgress(today);
            float overallProgress = this.fitnessChallengeViewModel.getOverallProgress(today);
            this.gameController.setProgress(adultProgress, childProgress, overallProgress, new OnAnimationCompletedListener() {
                @Override
                public void onAnimationCompleted() {
                    progressAnimationStatus = ProgressAnimationStatus.COMPLETED;
                }
            });
            this.progressAnimationStatus = ProgressAnimationStatus.PLAYING;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doResetProgressAnimation() {
        this.progressAnimationStatus = ProgressAnimationStatus.READY;
        this.gameController.resetProgress();
    }


    /* FITNESS CHALLENGE VIEW MODEL METHODS */
    @Override
    public void tryFetchChallengeData(Fragment fragment) {
        /*
        if (this.isFitnessAndChallengeDataReady() == false){
            this.fetchChallengeData(fragment);
        }
        */
        this.fetchChallengeData(fragment);
    }

    private void fetchChallengeData(final Fragment fragment) {
        this.fitnessChallengeViewModel = ViewModelProviders.of(fragment)
                .get(FitnessChallengeViewModel.class);
        this.fitnessChallengeViewModel
                .fetchSevenDayFitnessAndChallengeData(startDate, endDate)
                .observe(fragment, new Observer<FetchingStatus>() {
                    @Override
                    public void onChanged(@Nullable final FetchingStatus status) {
                        doHandleFetchingStatusChanged(status, fragment);
                    }
                });
    }

    private void doHandleFetchingStatusChanged(@Nullable FetchingStatus status, Fragment fragment) {
        switch (status) {
            case SUCCESS:
                Log.d(LOG_TAG, "Fitness challenge data fetched");
                doHandleFetchingSuccess(fragment);
                break;
            case NO_INTERNET:
                Log.e(LOG_TAG, "Failed to fetch fitness challenge data: no internet");
                break;
            case FETCHING:
                break;
            default:
                Log.e(LOG_TAG, "Failed to fetch fitness challenge data: " + status.toString());
                break;
        }
    }

    private void doHandleFetchingSuccess(Fragment fragment) {
        try {
            ChallengeStatus status = this.fitnessChallengeViewModel.getChallengeStatus();
            boolean isChallengeAchieved = this.fitnessChallengeViewModel.isChallengeAchieved(today);
            this.doProcessFitnessChallenge(fragment, status, isChallengeAchieved);
        } catch (ChallengeDoesNotExistsException e) {
            Log.e(LOG_TAG, "Fitness challenge does not exist");
            e.printStackTrace();
        } catch (FitnessException e) {
            Log.e(LOG_TAG, "Fitness data exception");
            e.printStackTrace();
        } catch (PersonDoesNotExistException e) {
            Log.e(LOG_TAG, "Fitness calculation failed because person does not exist.");
            e.printStackTrace();
        }
    }

    private void doProcessFitnessChallenge(
            Fragment fragment, ChallengeStatus status, boolean isChallengeAchieved)
            throws ChallengeDoesNotExistsException {
        if (IS_DEMO_MODE) {
            this.updateGroupStepsProgress();
            this.doHandleRunningChallenge(fragment);
            return;
        }
        switch(status) {
            case AVAILABLE:
                doHandleChallengeAvailable(fragment);
                break;
            case UNSYNCED_RUN:
                // PASS to show Sync control
            case RUNNING:
                this.updateGroupGoal();
                if (this.fitnessSyncStatus == SyncStatus.COMPLETED) {
                    this.doHandleRunningChallenge(fragment);
                    this.updateGroupStepsProgress();
                } else {
                    this.showControlForSyncing(fragment.getContext());
                }
                break;
            case PASSED:
                this.updateGroupGoal();
                doHandleChallengePassed(fragment, isChallengeAchieved);
                break;
            case CLOSED:
                this.updateGroupGoal();
                doHandleChallengeClosed(fragment, isChallengeAchieved);
                break;
            default:
                break;
        }
    }

    private void recalculateChallengeData(final Fragment fragment) {
        this.fitnessChallengeViewModel.refreshFitnessData(startDate, endDate);
        /*
        this.fitnessChallengeViewModel.fetchSevenDayFitness(startDate, endDate)
                .observe(fragment, new Observer<FetchingStatus>() {
                    @Override
                    public void onChanged(@Nullable final FetchingStatus status) {
                        doObserveFetchingStatusChange(status, fragment);
                    }
                });
                */
    }

    /*
    private void doObserveFetchingStatusChange(FetchingStatus status, Fragment fragment) {
        switch (status) {
            case SUCCESS:
                Log.d(LOG_TAG, "Fitness challenge status calculated");
                doPostFitnessSyncChallengeActions(fragment);
                break;
            case NO_INTERNET:
                Log.e(LOG_TAG, "Fetching fitness data failed: no internet");
                break;
            case FETCHING:
                break;
            default:
                Log.e(LOG_TAG, "Fetching fitness data failed: " + status.toString());
                break;
        }
    }
    private void doPostFitnessSyncChallengeActions(Fragment fragment) {
        try {
            ChallengeStatus status = this.fitnessChallengeViewModel.getChallengeStatus();
            this.doProcessPostFitnessSyncChallengeActions(
                    fragment,
                    status,
                    this.fitnessChallengeViewModel.isChallengeAchieved(today));
        } catch (ChallengeDoesNotExistsException e) {
            e.printStackTrace();
        } catch (FitnessException e) {
            e.printStackTrace();
        } catch (PersonDoesNotExistException e) {
            e.printStackTrace();
        }
    }

    private void doProcessPostFitnessSyncChallengeActions(
            Fragment fragment, ChallengeStatus status, boolean isChallengeAchieved)
            throws ChallengeDoesNotExistsException {
        switch (status) {
            case AVAILABLE:
                // doHandleChallengeAvailable(fragment);
                break;
            case UNSYNCED_RUN:
                // PASS to Running
            case RUNNING:
                doHandleRunningChallenge(fragment);
                break;
            case PASSED:
                doHandleChallengePassed(fragment, isChallengeAchieved);
                break;
            case CLOSED:
                doHandleChallengeClosed(fragment, isChallengeAchieved);
                break;
            default:
                break;
        }
    }

    private boolean isFitnessAndChallengeDataReady() {
        if (this.fitnessChallengeViewModel == null) {
            return false;
        } else {
            return isFitnessAndChallengeDataFetched();
        }
    }

    */

    private boolean isFitnessAndChallengeDataFetched() {
        // TODO Check this
        return fitnessChallengeViewModel.getFetchingStatus() == FetchingStatus.SUCCESS
                || this.fitnessSyncStatus == SyncStatus.NO_NEW_DATA;
    }

    private void doHandleChallengeAvailable(Fragment fragment) {
        this.showNoAvailableChallenges(fragment.getContext());
    }

    private void doHandleRunningChallenge(Fragment fragment)
            throws ChallengeDoesNotExistsException {
        this.progressAnimationStatus = ProgressAnimationStatus.READY;
        this.showControlForReady(fragment.getContext());
    }

    private void doHandleChallengePassed(Fragment fragment, boolean isChallengeAchieved)
            throws ChallengeDoesNotExistsException {
        this.progressAnimationStatus = ProgressAnimationStatus.READY;
        if (isChallengeAchieved) {
            this.fitnessChallengeViewModel.setChallengeClosed();
            this.showControlForAchieved(fragment.getContext());
        } else {
            this.showAlternateExit(fragment.getContext());
        }
    }

    private void doHandleChallengeClosed(Fragment fragment, boolean isChallengeAchieved) {
        progressAnimationStatus = ProgressAnimationStatus.READY;
        if (isChallengeAchieved) {
            showControlForAchieved(fragment.getContext());
        } else {
            showControlForMissed(fragment.getContext());
        }
    }

    /* FITNESS SYNC VIEWMODEL METHODS */
    @Override
    public boolean trySyncFitnessData(final Fragment fragment) {
        if (IS_DEMO_MODE) {
            return false;
        }

        if (this.fitnessSyncViewModel == null) {
            this.fitnessSyncViewModel = ViewModelProviders.of(fragment).get(FitnessSyncViewModel.class);
            this.fitnessSyncViewModel.getLiveStatus().observe(fragment, new Observer<SyncStatus>() {
                @Override
                public void onChanged(@Nullable SyncStatus syncStatus) {
                    onSyncStatusChanged(syncStatus, fragment);
                }
            });
        }

        if (this.isSyncronizingFitnessData) { // TODO Use this.fitnessSyncStatus instead
            return false;
        } else {
            Log.d(LOG_TAG, "Starting sync process...");
            this.isSyncronizingFitnessData = true;
            this.fitnessSyncViewModel.perform();
            return true;
        }
    }

    private void onSyncStatusChanged(SyncStatus syncStatus, Fragment fragment) {
        this.fitnessSyncStatus = syncStatus;

        switch (syncStatus) {
            case NO_NEW_DATA:
                Log.d(LOG_TAG, "No new data within interval.");
                break;
            case NEW_DATA_AVAILABLE:
                Log.d(LOG_TAG, "New data is available...");
                break;
            case INITIALIZING:
                Log.d(LOG_TAG, "Initializing sync...");
                break;
            case CONNECTING:
                Log.d(LOG_TAG, "Connecting to: " + getCurrentPersonString());
                View view = fragment.getView();
                this.showControlForSyncingThisPerson(view, fitnessSyncViewModel.getCurrentPerson());
                break;
            case DOWNLOADING:
                Log.d(LOG_TAG, "Downloading fitness data: " + getCurrentPersonString());
                break;
            case UPLOADING:
                Log.d(LOG_TAG, "Uploading fitness data: " + getCurrentPersonString());
                break;
            case IN_PROGRESS:
                Log.d(LOG_TAG, "Sync completed for: " + getCurrentPersonString());
                this.fitnessSyncViewModel.performNext();
                break;
            case COMPLETED:
                Log.d(LOG_TAG, "Successfully synchronizing all devices.");
                this.stopSyncFitnessData();
                this.recalculateChallengeData(fragment);
                break;
            case FAILED:
                Log.d(LOG_TAG, "Synchronization failed");
                this.stopSyncFitnessData();
                break;
        }
    }

    private boolean stopSyncFitnessData() {
        if (this.isSyncronizingFitnessData) {
            this.fitnessSyncViewModel.stop();
            this.isSyncronizingFitnessData = false;
            return true;
        } else {
            return false;
        }
    }

    private String getCurrentPersonString() {
        StorywellPerson person = fitnessSyncViewModel.getCurrentPerson();
        if (person != null) {
            return person.toString();
        } else {
            return "Null Person";
        }
    }

    /* STATIC SNACKBAR METHODS*/
    public static Snackbar getSnackbar(String text, Activity activity) {
        View gameView = activity.findViewById(R.id.layout_gameview);
        Snackbar snackbar = Snackbar.make(gameView, text, Snackbar.LENGTH_LONG);
        snackbar = setSnackBarTheme(snackbar, activity.getApplicationContext());
        return snackbar;
    }

    private static Snackbar setSnackBarTheme(Snackbar snackbar, Context context) {
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(ContextCompat.getColor(context, R.color.sea_foregroundDark));
        snackbarView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        return snackbar;
    }

    /* STATIC FACTORY METHODS for the GameController */
    public static MonitoringController getGameController(MonitoringView gameView, int heroId) {
        MonitoringController gameController = new MonitoringController(gameView);
        gameController.setLevelDesign(gameView.getResources(), getGameLevel(gameView.getContext()));
        gameController.setHeroSprite(getHero(gameView.getResources(), heroId));
        return gameController;
    }

    private static GameLevelInterface getGameLevel(Context context) {
        return MonitoringActivity.getGameLevelDesign(getGameFont(context));
    }

    private static Typeface getGameFont(Context context) {
        return ResourcesCompat.getFont(context, MonitoringActivity.FONT_FAMILY);
    }

    private static HeroSprite getHero(Resources resources, int heroId) {
        return new HeroSprite(resources, heroId,
                MonitoringActivity.getAdultBalloonDrawables(10),
                MonitoringActivity.getChildBalloonDrawables(10),
                R.color.colorPrimaryLight);
    }
}
