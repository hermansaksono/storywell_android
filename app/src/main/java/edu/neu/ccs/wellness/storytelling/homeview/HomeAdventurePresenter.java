package edu.neu.ccs.wellness.storytelling.homeview;

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.ViewAnimator;

import org.json.JSONException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import edu.neu.ccs.wellness.fitness.challenges.ChallengeDoesNotExistsException;
import edu.neu.ccs.wellness.fitness.interfaces.ChallengeStatus;
import edu.neu.ccs.wellness.fitness.interfaces.FitnessException;
import edu.neu.ccs.wellness.logging.Param;
import edu.neu.ccs.wellness.logging.WellnessUserLogging;
import edu.neu.ccs.wellness.people.Person;
import edu.neu.ccs.wellness.people.PersonDoesNotExistException;
import edu.neu.ccs.wellness.story.StoryChapterManager;
import edu.neu.ccs.wellness.storytelling.MonitoringActivity;
import edu.neu.ccs.wellness.storytelling.R;
import edu.neu.ccs.wellness.storytelling.Storywell;
import edu.neu.ccs.wellness.storytelling.monitoringview.Constants;
import edu.neu.ccs.wellness.storytelling.monitoringview.HeroSprite;
import edu.neu.ccs.wellness.storytelling.monitoringview.MonitoringController;
import edu.neu.ccs.wellness.storytelling.monitoringview.MonitoringView;
import edu.neu.ccs.wellness.storytelling.monitoringview.interfaces.GameLevelInterface;
import edu.neu.ccs.wellness.storytelling.monitoringview.interfaces.OnAnimationCompletedListener;
import edu.neu.ccs.wellness.storytelling.settings.SynchronizedSetting;
import edu.neu.ccs.wellness.storytelling.settings.SynchronizedSettingRepository;
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
    private int heroId;
    private int heroResId;
    private int [] drawableHeroIdArray = new int[Constants.NUM_HERO_DRAWABLES];
    private boolean isDemoMode;

    private GregorianCalendar today;
    private GregorianCalendar startDate;
    private GregorianCalendar endDate;
    private ProgressAnimationStatus progressAnimationStatus = ProgressAnimationStatus.UNREADY;
    private SyncStatus fitnessSyncStatus = SyncStatus.UNINITIALIZED;
    private boolean isSyncronizingFitnessData = false;
    private Storywell storywell;

    private View rootView;
    private ViewAnimator gameviewViewAnimator;
    private ViewAnimator controlViewAnimator;

    private FitnessChallengeViewModel fitnessChallengeViewModel;
    private FitnessSyncViewModel fitnessSyncViewModel;
    private MonitoringController gameController;
    private MonitoringView gameView;

    private AdventurePresenterListener adventureFragmentListener = null;

    private List<String> completedChallenges;

    /* CONSTRUCTOR */
    public HomeAdventurePresenter(View rootView) {
        /* Basic data */
        this.today = WellnessDate.getTodayDate();
        //this.today = getDummyDate();
        this.startDate = WellnessDate.getFirstDayOfWeek(this.today);
        this.endDate = WellnessDate.getEndDate(this.startDate);
        this.storywell = new Storywell(rootView.getContext());
        this.heroId = storywell.getSynchronizedSetting().getHeroCharacterId();
        this.drawableHeroIdArray = Constants.HERO_DRAWABLES[this.heroId];
        this.heroResId = this.drawableHeroIdArray[Constants.HERO_DRAWABLE_FLYING];

        /* Demo mode */
        this.isDemoMode = SynchronizedSettingRepository.getLocalInstance(rootView.getContext()).isDemoMode();

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
        this.gameController = getGameController(this.gameView, this.drawableHeroIdArray);

        /* Setting */

        SynchronizedSetting setting = SynchronizedSettingRepository
                .getLocalInstance(rootView.getContext());
        this.completedChallenges = setting.getStoryListInfo().getUnlockedStoryPages();
    }

    @Override
    public void setAdventureFragmentListener(AdventurePresenterListener listener) {
        this.adventureFragmentListener = listener;
    }

    /* BUTTON AND TAP METHODS */
    /**
     * Given an MotionEvent and a View, try start start progress animation when ready.
     * @param event
     * @param view
     * @return
     */
    @Override
    public boolean processTapOnGameView(MotionEvent event, View view) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return true; // Return true so the subsequent events can be processed.
            case MotionEvent.ACTION_UP:
                return onGameViewIsTapped(event, view);
            default:
                return false;
        }
    }

    private boolean onGameViewIsTapped(MotionEvent event, View view) {
        if (this.gameView.isOverHero(event)) {
            return onHeroIsTapped(event, view);
        } else {
            return false;
        }
    }

    private boolean onHeroIsTapped(MotionEvent event, View view) {
        switch (this.progressAnimationStatus) {
            case UNREADY:
                // Do nothing
                return false;
            case READY:
                this.doStartProgressAnimation();
                return true;
            case PLAYING:
                // Do nothing;
                return false;
            case COMPLETED:
                // TODO do something
                this.showCompletionPrompt(view);
                return false;
            default:
                return false;
        }
    }

    private void showCompletionPrompt(final View view) {
        SynchronizedSetting setting = storywell.getSynchronizedSetting();
        if (setting.getStoryChallengeInfo().getIsSet()) {
            final String storyId = setting.getStoryChallengeInfo().getStoryId();
            String title = setting.getStoryChallengeInfo().getStoryTitle();
            String coverImageUri = setting.getStoryChallengeInfo().getStoryCoverImageUri();
            AlertDialog dialog = ChallengeCompletedDialog.newInstance(title, coverImageUri, view.getContext(),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            unlockCurrentStoryChallenge(view.getContext());
                            adventureFragmentListener.goToStoriesTab(storyId);
                        }
                    });
            dialog.show();
        }
    }

    /**
     * Tell the status bar to show progress animation information
     */
    @Override
    public void startProgressAnimation() {
        if (this.progressAnimationStatus == ProgressAnimationStatus.READY) {
            this.doStartProgressAnimation();
        }
    }

    /**
     * Tell the status bar to reset progress animation information
     */
    @Override
    public void resetProgressAnimation() {
        if (this.progressAnimationStatus != ProgressAnimationStatus.READY) {
            this.doResetProgressAnimation();
        }
    }

    /**
     * Update the status bar with information about the fitness status.
     */
    @Override
    public void startPerformProgressAnimation(Fragment fragment) {
        if (this.isDemoMode) {
            this.showControlForReady(fragment.getContext());
            return;
        }
        WellnessUserLogging userLogging = new WellnessUserLogging(storywell.getGroup().getName());
        Bundle bundle = new Bundle();
        bundle.putString(Param.BUTTON_NAME, "PLAY_ANIMATION");
        userLogging.logEvent("PLAY_ANIMATION_BUTTON_CLICK", bundle);

        switch(this.fitnessSyncStatus) {
            case NO_NEW_DATA:
                this.showControlForReady(fragment.getContext());
                break;
            case NEW_DATA_AVAILABLE:
                this.trySyncFitnessData(fragment);
                this.showControlForSyncing(fragment.getContext());
                break;
            case COMPLETED:
                this.showControlForReady(fragment.getContext());
                break;
            case FAILED:
                this.showControlForFailure(fragment.getContext());
                break;
            default:
                this.showControlForSyncing(fragment.getContext());
                break;
        }
    }

    /**
     * Update the status bar to show the first card.
     * @param context
     */
    @Override
    public void showControlForFirstCard(Context context) {
        this.setContolChangeToMoveRight(context);
        controlViewAnimator.setDisplayedChild(CONTROL_PLAY);
    }

    private void showControlForSyncing(Context context) {
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

    private void showControlForSyncingByChallengeStatus(
            View view, StorywellPerson storywellPerson, ChallengeStatus status) {
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

    private void showControlForSyncingCaregiver(View view, String name) {
        this.setContolChangeToMoveLeft(view.getContext());
        this.controlViewAnimator.setDisplayedChild(CONTROL_SYNCING_CAREGIVER);

        String text = (String) view.getResources().getText(R.string.adventure_info_syncing_caregiver);
        TextView textView = view.findViewById(R.id.text_syncing_caregiver_info);

        textView.setText(String.format(text, name));
    }

    private void showControlForSyncingChild(View view, String name) {
        this.setContolChangeToMoveLeft(view.getContext());
        this.controlViewAnimator.setDisplayedChild(CONTROL_SYNCING_CHILD);

        String text = (String) view.getResources().getText(R.string.adventure_info_syncing_child);
        TextView textView = view.findViewById(R.id.text_syncing_child_info);

        textView.setText(String.format(text, name));
    }

    private void showControlForReady(Context context) {
        this.setContolChangeToMoveLeft(context);
        controlViewAnimator.setDisplayedChild(CONTROL_READY);
    }

    private void showControlForProgressAnimation() {
        try {
            ChallengeStatus status = this.fitnessChallengeViewModel.getChallengeStatus();

            switch(status) {
                case AVAILABLE:
                    // PASS will not be called when status is AVAILABLE
                    break;
                case UNSYNCED_RUN:
                    // PASS to show Sync control
                case RUNNING:
                    this.showControlForRunning(this.rootView.getContext());
                    break;
                case PASSED:
                    this.showControlForPassed(this.rootView.getContext());
                    break;
                case CLOSED:
                    this.showControlForClosed(this.rootView.getContext());
                    break;
                default:
                    break;
            }
        } catch (ChallengeDoesNotExistsException e) {
            Log.e(LOG_TAG, "Challenge does not exist.");
        }
    }

    private void showControlForRunning(Context context) {
        try {
            if (this.fitnessChallengeViewModel.isChallengeAchieved(today)) {
                this.showControlForAchieved(context);
                Log.d(LOG_TAG, "Challenge achieved!");
            } else {
                this.showControlForProgressInfo(context);
                Log.d(LOG_TAG, "Challenge is not yet achieved");
            }
        } catch (ChallengeDoesNotExistsException e) {
            e.printStackTrace();
        } catch (PersonDoesNotExistException e) {
            e.printStackTrace();
        } catch (FitnessException e) {
            e.printStackTrace();
        }
    }


    private void showControlForPassed(Context context) throws ChallengeDoesNotExistsException {
        try {
            if (this.fitnessChallengeViewModel.isChallengeAchieved(today)) {
                this.showControlForAchieved(context);
            } else {
                this.showControlForMissed(context);
            }
        } catch (PersonDoesNotExistException e) {
            e.printStackTrace();
        } catch (FitnessException e) {
            e.printStackTrace();
        }
    }

    private void showControlForClosed(Context context) throws ChallengeDoesNotExistsException {
        try {
            if (this.fitnessChallengeViewModel.isChallengeAchieved(today)) {
                this.showControlForAchieved(context);
            } else {
                this.showControlForFailure(context);
            }
        } catch (PersonDoesNotExistException e) {
            e.printStackTrace();
        } catch (FitnessException e) {
            e.printStackTrace();
        }
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

    private void showControlForAchieved(Context context) {
        this.setContolChangeToMoveLeft(context);
        controlViewAnimator.setDisplayedChild(CONTROL_CLOSED);
    }

    private void showControlForFailure(Context context) {
        this.setContolChangeToMoveLeft(context);
        controlViewAnimator.setDisplayedChild(CONTROL_MISSED);
    }

    private void showControlForMissed(Context context) {
        this.setContolChangeToMoveLeft(context);
        controlViewAnimator.setDisplayedChild(CONTROL_MISSED);
    }

    private void showAlternateExit(Context context) {
        this.setContolChangeToMoveLeft(context);
        // TODO HS: controlViewAnimator.setDisplayedChild(CONTROL_MISSED);
    }

    private void showNoAvailableChallenges(Context context) {
        if (this.controlViewAnimator.getDisplayedChild() != CONTROL_NO_RUNNING) {
            this.setGameviewVisorIsVisible(false);
            this.setHeroIsVisible(false);
            this.setContolChangeToMoveRight(context);
            this.controlViewAnimator.setDisplayedChild(CONTROL_NO_RUNNING);
        }
    }

    private void setGameviewVisorIsVisible(boolean isVisible) {
        if (this.isDemoMode) {
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
        if (this.isDemoMode) {
            return;
        }
        this.gameController.setHeroIsVisible(isVisible);
    }

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
    private void doStartProgressAnimation() {
        try {
            float adultProgress = this.fitnessChallengeViewModel.getAdultProgress(today);
            float childProgress = this.fitnessChallengeViewModel.getChildProgress(today);
            final float overallProgress = this.fitnessChallengeViewModel.getOverallProgress(today);
            this.gameController.setProgress(adultProgress, childProgress, overallProgress, new OnAnimationCompletedListener() {
                @Override
                public void onAnimationCompleted() {
                    onProgressAnimationCompleted(overallProgress);
                }
            });
            this.progressAnimationStatus = ProgressAnimationStatus.PLAYING;
        } catch (ChallengeDoesNotExistsException e) {
            Log.e(LOG_TAG, "Challenge does not exist.");
            e.printStackTrace();
        } catch (PersonDoesNotExistException e) {
            Log.e(LOG_TAG, "Person does not exist.");
            e.printStackTrace();
        } catch (FitnessException e) {
            Log.e(LOG_TAG, "Fitness exception: " + e.toString());
            e.printStackTrace();
        }
    }

    private void onProgressAnimationCompleted(float overallProgress) {
        this.showControlForProgressAnimation();
        this.progressAnimationStatus = ProgressAnimationStatus.COMPLETED;

        if (overallProgress >= 1) {
            this.onChallengeCompleted();
        }

        if (this.isDemoMode) {
            this.updateGroupStepsProgress();
        }
    }

    private void onChallengeCompleted() {
        this.gameController.setHeroChallengeAsCompleted();
    }

    private void doResetProgressAnimation() {
        this.progressAnimationStatus = ProgressAnimationStatus.READY;
        this.gameController.resetProgress();
    }


    /* FITNESS CHALLENGE VIEW MODEL METHODS */
    @Override
    public void tryFetchChallengeData(final Fragment fragment) {
        this.fitnessChallengeViewModel = ViewModelProviders.of(fragment)
                .get(FitnessChallengeViewModel.class);
        this.fitnessChallengeViewModel.refreshFitnessChallengeData(startDate, endDate);
        this.fitnessChallengeViewModel.getChallengeLiveData()
                .observe(fragment, new Observer<FetchingStatus>() {
                    @Override
                    public void onChanged(@Nullable final FetchingStatus status) {
                        doHandleFetchingStatusChanged(status, fragment);
                    }
                });
    }

    @Override
    public void stopObservingChallengeData(Fragment fragment) {
        if (this.fitnessChallengeViewModel != null) {
            this.fitnessChallengeViewModel.getChallengeLiveData().removeObservers(fragment);
        }
    }

    private void doHandleFetchingStatusChanged(FetchingStatus status, Fragment fragment) {
        switch (status) {
            case FETCHING:
                break;
            case SUCCESS:
                Log.d(LOG_TAG, "Fitness challenge data fetched");
                doHandleFetchingSuccess(fragment);
                break;
            case NO_INTERNET:
                Log.e(LOG_TAG, "Failed to fetch fitness challenge data: no internet");
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
        } catch (FitnessException e) {
            Log.e(LOG_TAG, "Fitness data exception: " + e.toString());
        } catch (PersonDoesNotExistException e) {
            Log.e(LOG_TAG, "Fitness calculation failed because person does not exist.");
        }
    }

    private void doProcessFitnessChallenge(
            Fragment fragment, ChallengeStatus status, boolean isChallengeAchieved)
            throws ChallengeDoesNotExistsException {
        if (this.isDemoMode) {
            this.updateGroupStepsProgress();
            this.doHandleRunningChallenge(fragment);
            return;
        }
        if (!this.isFitnessAndChallengeDataFetched()) {
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
                this.updateGroupStepsProgress();
                if (this.fitnessSyncStatus == SyncStatus.COMPLETED) {
                    this.doHandleRunningChallenge(fragment);
                } else {
                    this.showControlForSyncing(fragment.getContext());
                }
                break;
            case PASSED:
                this.updateGroupGoal();
                this.updateGroupStepsProgress();
                this.doHandleChallengePassed(fragment, isChallengeAchieved);
                break;
            case CLOSED:
                this.updateGroupGoal();
                this.updateGroupStepsProgress();
                this.doHandleChallengeClosed(fragment, isChallengeAchieved);
                break;
            default:
                break;
        }
    }

    private boolean isFitnessAndChallengeDataFetched() { // TODO Check this
        return fitnessChallengeViewModel.getFetchingStatus() == FetchingStatus.SUCCESS
                || this.fitnessSyncStatus == SyncStatus.NO_NEW_DATA;
    }

    private void doHandleChallengeAvailable(Fragment fragment) {
        this.showNoAvailableChallenges(fragment.getContext());
    }

    private void doHandleRunningChallenge(Fragment fragment)
            throws ChallengeDoesNotExistsException {
        this.fitnessChallengeViewModel.setChallengeClosedIfAchieved(today);
        this.progressAnimationStatus = ProgressAnimationStatus.READY;
        this.showControlForReady(fragment.getContext());
    }

    private void doHandleChallengePassed(Fragment fragment, boolean isChallengeAchieved)
            throws ChallengeDoesNotExistsException {
        this.progressAnimationStatus = ProgressAnimationStatus.READY;
        /*
        if (isChallengeAchieved) {
            this.showControlForReady(fragment.getContext());
            this.fitnessChallengeViewModel.setChallengeClosedIfAchieved(today);
        } else {
            this.showAlternateExit(fragment.getContext());
        }
        */
    }

    public void unlockCurrentStoryChallenge(Context context) {
        SynchronizedSetting setting = this.storywell.getSynchronizedSetting();
        String storyIdToBeUnlocked = setting.getStoryChallengeInfo().getStoryId();
        String chapterIdToBeUnlocked = setting.getStoryChallengeInfo().getChapterIdToBeUnlocked();

        setting.resetStoryChallengeInfo();
        setting.getStoryListInfo().getUnlockedStoryPages().add(chapterIdToBeUnlocked);
        setting.getStoryListInfo().getUnlockedStories().add(storyIdToBeUnlocked);
        setting.getStoryListInfo().getUnreadStories().add(storyIdToBeUnlocked);
        setting.getStoryListInfo().setHighlightedStoryId(storyIdToBeUnlocked);
        SynchronizedSettingRepository.saveLocalAndRemoteInstance(setting, context);
    }

    private void doHandleChallengeClosed(Fragment fragment, boolean isChallengeAchieved) {
        progressAnimationStatus = ProgressAnimationStatus.READY;
        /*
        if (isChallengeAchieved) {
            showControlForAchieved(fragment.getContext());
        } else {
            showControlForMissed(fragment.getContext());
        }
        */
    }

    /* FITNESS SYNC VIEW MODEL METHODS */
    @Override
    public boolean trySyncFitnessData(final Fragment fragment) {
        if (this.isDemoMode) {
            this.progressAnimationStatus = ProgressAnimationStatus.READY;
            return false;
        }

        if (this.fitnessSyncViewModel == null) {
            this.fitnessSyncViewModel = ViewModelProviders.of(fragment)
                    .get(FitnessSyncViewModel.class);
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
                this.recalculateChallengeData();
                break;
            case FAILED:
                Log.d(LOG_TAG, "Synchronization failed");
                this.stopSyncFitnessData();
                break;
        }
    }

    private void stopSyncFitnessData() {
        if (this.isSyncronizingFitnessData) {
            this.fitnessSyncViewModel.stop();
            this.isSyncronizingFitnessData = false;
        }
    }

    private void recalculateChallengeData() {
        this.fitnessChallengeViewModel.refreshFitnessDataOnly(startDate, endDate);
    }

    private String getCurrentPersonString() {
        StorywellPerson person = fitnessSyncViewModel.getCurrentPerson();
        if (person != null) {
            return person.toString();
        } else {
            return "Null Person";
        }
    }

    /* CHALLENGE CHAPTER METHODS */
    @Override
    public boolean markCurrentChallengeAsUnlocked(Context context) {
        StoryChapterManager storyChapterManager = new StoryChapterManager(context);
        return storyChapterManager.setCurrentChallengeAsUnlocked(context);
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
    public static MonitoringController getGameController(MonitoringView gameView, int[] heroIds) {
        MonitoringController gameController = new MonitoringController(gameView);
        gameController.setLevelDesign(gameView.getResources(), getGameLevel(gameView.getContext()));
        gameController.setHeroSprite(getHero(gameView.getResources(), heroIds));
        return gameController;
    }

    private static GameLevelInterface getGameLevel(Context context) {
        return MonitoringActivity.getGameLevelDesign(getGameFont(context));
    }

    private static Typeface getGameFont(Context context) {
        return ResourcesCompat.getFont(context, MonitoringActivity.FONT_FAMILY);
    }

    private static HeroSprite getHero(Resources resources, int[] heroIds) {
        return new HeroSprite(resources, heroIds,
                MonitoringActivity.getAdultBalloonDrawables(10),
                MonitoringActivity.getChildBalloonDrawables(10),
                R.color.colorPrimaryLight);
    }
}
