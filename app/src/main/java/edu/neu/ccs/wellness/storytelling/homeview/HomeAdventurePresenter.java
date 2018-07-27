package edu.neu.ccs.wellness.storytelling.homeview;

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ViewAnimator;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import edu.neu.ccs.wellness.fitness.challenges.ChallengeDoesNotExistsException;
import edu.neu.ccs.wellness.fitness.interfaces.ChallengeStatus;
import edu.neu.ccs.wellness.fitness.interfaces.FitnessException;
import edu.neu.ccs.wellness.fitness.interfaces.GroupFitnessInterface;
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

public class HomeAdventurePresenter {

    public static final int CONTROL_PLAY = 0;
    public static final int CONTROL_SYNCING = 1;
    public static final int CONTROL_READY = 2;
    public static final int CONTROL_PROGRESS_INFO = 3;
    public static final int CONTROL_PREV_NEXT = 4;

    private GregorianCalendar today;
    private GregorianCalendar startDate;
    private GregorianCalendar endDate;
    private ProgressAnimationStatus progressAnimationStatus = ProgressAnimationStatus.UNSTARTED;
    private boolean isSyncronizingFitnessData = false;
    //private boolean isProgressAnimationCompleted = false;

    private View rootView;
    private ViewAnimator gameviewViewAnimator;
    private ViewAnimator controlViewAnimator;

    //private FamilyFitnessChallengeViewModel familyFitnessChallengeViewModel;
    private FitnessChallengeViewModel familyFitnessChallengeViewModel;
    //private FitnessSyncViewModel fitnessSyncViewModel;
    private FitnessSyncViewModel fitnessSyncViewModel;
    private MonitoringController gameController;
    private MonitoringView gameView;

    private static final int FIRST_DAY_OF_WEEK = Calendar.SUNDAY;

    public HomeAdventurePresenter(View rootView) {
        /* Basic data */
        this.today = WellnessDate.getTodayDate();
        //this.today = getDummyDate(); // TODO REMOVE THIS FOR PRODUCTION
        this.startDate = WellnessDate.getFirstDayOfWeek(this.today);
        this.endDate = WellnessDate.getEndDate(this.startDate);

        /* Views */
        this.rootView = rootView;
        this.gameviewViewAnimator = this.rootView.findViewById(R.id.gameview_view_animator);
        this.controlViewAnimator = this.rootView.findViewById(R.id.control_view_animator);

        /* Game's Controller */
        Typeface gameFont = ResourcesCompat.getFont(rootView.getContext(), MonitoringActivity.FONT_FAMILY);
        GameLevelInterface gameLevel = MonitoringActivity.getGameLevelDesign(gameFont);
        HeroSprite hero = new HeroSprite(rootView.getResources(), R.drawable.hero_dora,
                MonitoringActivity.getAdultBalloonDrawables(10),
                MonitoringActivity.getChildBalloonDrawables(10),
                R.color.colorPrimaryLight);
        this.gameView = rootView.findViewById(R.id.layout_monitoringView);
        this.gameController = new MonitoringController(this.gameView);
        this.gameController.setLevelDesign(rootView.getResources(), gameLevel);
        this.gameController.setHeroSprite(hero);
    }

    /* BUTTON METHODS */
    public void onFabPlayClicked(Activity activity) {
        if (this.progressAnimationStatus == ProgressAnimationStatus.UNSTARTED) {
            tryStartProgressAnimation();
        } else if (this.progressAnimationStatus == ProgressAnimationStatus.PLAYING) {
            // do nothing
        } else if (this.progressAnimationStatus == ProgressAnimationStatus.COMPLETED) {
            resetProgressAnimation();
        }
    }

    public void onFabShowCalendarClicked(View view) {
        gameviewViewAnimator.setInAnimation(view.getContext(), R.anim.overlay_move_down);
        gameviewViewAnimator.setOutAnimation(view.getContext(), R.anim.basecard_move_down);
        gameviewViewAnimator.showNext();
    }

    public void onFabCalendarHideClicked(View view) {
        gameviewViewAnimator.setInAnimation(view.getContext(), R.anim.basecard_move_up);
        gameviewViewAnimator.setOutAnimation(view.getContext(), R.anim.overlay_move_up);
        gameviewViewAnimator.showPrevious();
    }

    public void doStartProgressAnimation() {
        if (this.progressAnimationStatus == ProgressAnimationStatus.UNSTARTED) {
            this.tryStartProgressAnimation();
        }
    }

    public void doResetProgressAnimation() {
        this.resetProgressAnimation();
    }

    public void showControlForFirstCard(View view) {
        this.setContolChangeToMoveRight(view);
        controlViewAnimator.setDisplayedChild(CONTROL_PLAY);
    }

    public void showControlForSyncing(View view) {
        this.setContolChangeToMoveLeft(view);
        controlViewAnimator.setDisplayedChild(CONTROL_SYNCING);
    }

    public void showControlForReady(View view) {
        this.setContolChangeToMoveLeft(view);
        controlViewAnimator.setDisplayedChild(CONTROL_READY);
    }

    public void showControlForProgressInfo(View view) {
        this.setContolChangeToMoveLeft(view);
        controlViewAnimator.setDisplayedChild(CONTROL_PROGRESS_INFO);
    }

    public void showControlForPrevNext(View view) {
        this.setContolChangeToMoveRight(view);
        controlViewAnimator.setDisplayedChild(CONTROL_PREV_NEXT);
    }

    private void setContolChangeToMoveLeft(View view) {
        controlViewAnimator.setInAnimation(view.getContext(), R.anim.view_move_left_next);
        controlViewAnimator.setOutAnimation(view.getContext(), R.anim.view_move_left_current);
    }

    private void setContolChangeToMoveRight(View view) {
        controlViewAnimator.setInAnimation(view.getContext(), R.anim.view_move_right_prev);
        controlViewAnimator.setOutAnimation(view.getContext(), R.anim.view_move_right_current);
    }

    /* GAMEVIEW METHODS */
    public void startGameView() {
        this.gameController.start();
    }

    public void resumeGameView() {
        this.gameController.resume();
    }

    public void pauseGameView() {
        this.gameController.pause();
    }

    public void stopGameView() {
        this.gameController.stop();
    }

    public boolean processTapOnGameView(Activity activity, MotionEvent event) {
        if (this.isFitnessAndChallengeDataReady()) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (this.gameView.isOverHero(event)) {
                    this.tryStartProgressAnimation();
                }
            }
        }
        return true;
    }

    /* PROGRESS ANIMATION STATES */
    enum ProgressAnimationStatus {
        UNSTARTED, PLAYING, COMPLETED;
    }

    /* PROGRESS ANIMATION METHODS */
    private void doPrepareProgressAnimations(Activity activity) {
        try {
            if (this.isChallengeStatusReadyForAdventure()) {

            } else {
                /*
                this.showNoAdventureMessage(activity);
                this.gameController.setHeroIsVisible(false); // TODO Uncomment on production
                */
            }
        } catch (ChallengeDoesNotExistsException e) {
            e.printStackTrace();
            Log.e("SWELL", e.getMessage());
        }
    }

    private void tryStartProgressAnimation() {
        if (isFitnessAndChallengeDataFetched()) {
            startProgressAnimation();
        } else {
            // DON'T DO ANYTHING
        }
    }

    private void startProgressAnimation() {
        try {
            float adultProgress = this.familyFitnessChallengeViewModel.getAdultProgress(today);
            float childProgress = this.familyFitnessChallengeViewModel.getChildProgress(today);
            float overallProgress = this.familyFitnessChallengeViewModel.getOverallProgress(today);
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

    private void resetProgressAnimation() {
        this.progressAnimationStatus = ProgressAnimationStatus.UNSTARTED;
        this.gameController.resetProgress();
    }

    /* FITNESS SYNC VIEWMODEL METHODS */
    public boolean trySyncFitnessData(Fragment fragment) {
        if (isSyncronizingFitnessData) {
            return false;
        } else {
            this.isSyncronizingFitnessData = true;
            this.syncFitnessData(fragment);
            return true;
        }
    }

    private void syncFitnessData(final Fragment fragment) {
        Storywell storywell = new Storywell(fragment.getContext());
        this.fitnessSyncViewModel = ViewModelProviders.of(fragment).get(FitnessSyncViewModel.class);
        this.fitnessSyncViewModel
                .perform(storywell.getGroup())
                .observe(fragment, new Observer<SyncStatus>(){

                    @Override
                    public void onChanged(@Nullable SyncStatus syncStatus) {
                        onSyncStatusChanged(syncStatus);
                    }
                });
    }

    private void onSyncStatusChanged(SyncStatus syncStatus) {
        if (SyncStatus.CONNECTING.equals(syncStatus)) {
            Log.d("SWELL", "Connecting: " + getCurrentPersonString());
        } else if (SyncStatus.DOWNLOADING.equals(syncStatus)) {
            Log.d("SWELL", "Downloading fitness data: " + getCurrentPersonString());
        } else if (SyncStatus.UPLOADING.equals(syncStatus)) {
            Log.d("SWELL", "Uploading fitness data: " + getCurrentPersonString());
        } else if (SyncStatus.IN_PROGRESS.equals(syncStatus)) {
            Log.d("SWELL", "Sync completed for: " + getCurrentPersonString());
            this.fitnessSyncViewModel.performNext();
        } else if (SyncStatus.SUCCESS.equals(syncStatus)) {
            this.isSyncronizingFitnessData = false;
            Log.d("SWELL", "All sync successful!");
        } else if (SyncStatus.FAILED.equals(syncStatus)) {
            this.isSyncronizingFitnessData = false;
            Log.d("SWELL", "Sync failed");
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

    /* FITNESS CHALLENGE VIEWMODEL METHODS */
    public void tryFetchChallengeAndFitnessData(Fragment fragment) {
        if (this.isFitnessAndChallengeDataReady() == false){
            this.fetchChallengeAndFitnessData(fragment);
        }
    }

    public void fetchChallengeAndFitnessData(Fragment fragment) {
        this.familyFitnessChallengeViewModel = getFamilyFitnessChallengeViewModel(fragment);
    }

    private FitnessChallengeViewModel getFamilyFitnessChallengeViewModel (final Fragment fragment) {
        FitnessChallengeViewModel viewModel;
        viewModel = ViewModelProviders.of(fragment).get(FitnessChallengeViewModel.class);
        viewModel.fetchSevenDayFitness(startDate, endDate).observe(fragment, new Observer<FetchingStatus>() {
            @Override
            public void onChanged(@Nullable final FetchingStatus status) {
                if (status == FetchingStatus.SUCCESS) {
                    Log.d("SWELL", "Fitness data fetched");
                    doPrepareProgressAnimations(fragment.getActivity());
                    printFitnessData();
                } else if (status == FetchingStatus.NO_INTERNET) {
                    Log.e("SWELL", "No internet connection to fetch fitness challenges.");
                } else if (status == FetchingStatus.FETCHING) {
                    // DO NOTHING
                } else {
                    Log.e("SWELL", "Fetching fitness challenge failed: " + status.toString());
                }
            }
        });
        return viewModel;
    }

    public boolean isFitnessAndChallengeDataReady() {
        if (this.familyFitnessChallengeViewModel == null) {
            return false;
        } else {
            return this.isFitnessAndChallengeDataFetched();
        }
    }
    public boolean isFitnessAndChallengeDataFetched() {
        return (familyFitnessChallengeViewModel.getFetchingStatus() == FetchingStatus.SUCCESS);
    }

    public boolean isChallengeStatusReadyForAdventure() throws ChallengeDoesNotExistsException {
        if (isFitnessAndChallengeDataReady()) {
            ChallengeStatus status = this.familyFitnessChallengeViewModel.getChallengeStatus();
            return status == ChallengeStatus.UNSYNCED_RUN || status == ChallengeStatus.RUNNING;
        } else {
            return false;
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

    /* DUMMY DATA */
    private static GregorianCalendar getDummyDate() {
        GregorianCalendar calendar = (GregorianCalendar) Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getDefault());
        calendar.set(Calendar.YEAR, 2018);
        calendar.set(Calendar.MONTH, Calendar.JULY);
        calendar.set(Calendar.DAY_OF_MONTH, 20);
        calendar.set(Calendar.HOUR_OF_DAY, 16);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    private void printFitnessData() {
        try {
            GroupFitnessInterface groupFitness = familyFitnessChallengeViewModel.getSevenDayFitness();
            Log.d("SWELL", groupFitness.toString());
        } catch (FitnessException e) {
            e.printStackTrace();
        }
    }
}
