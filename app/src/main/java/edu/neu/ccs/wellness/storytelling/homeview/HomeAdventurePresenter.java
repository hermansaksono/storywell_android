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
    private SyncStatus fitnessSyncStatus = SyncStatus.UNINITIALIZED;
    private boolean isSyncronizingFitnessData = false;

    private View rootView;
    private ViewAnimator gameviewViewAnimator;
    private ViewAnimator controlViewAnimator;

    private FitnessChallengeViewModel fitnessChallengeViewModel;
    private FitnessSyncViewModel fitnessSyncViewModel;
    private MonitoringController gameController;
    private MonitoringView gameView;

    /* PROGRESS ANIMATION STATES */
    enum ProgressAnimationStatus {
        UNSTARTED, PLAYING, COMPLETED;
    }

    /* CONSTRUCTOR */
    public HomeAdventurePresenter(View rootView) {
        /* Basic data */
        this.today = WellnessDate.getTodayDate();
        //this.today = getDummyDate();
        this.startDate = WellnessDate.getFirstDayOfWeek(this.today);
        this.endDate = WellnessDate.getEndDate(this.startDate);

        /* Views */
        this.rootView = rootView;
        this.gameviewViewAnimator = this.rootView.findViewById(R.id.gameview_view_animator);
        this.controlViewAnimator = this.rootView.findViewById(R.id.control_view_animator);
        this.gameView = rootView.findViewById(R.id.layout_monitoringView);

        /* Game's Controller */
        Typeface gameFont = ResourcesCompat.getFont(rootView.getContext(), MonitoringActivity.FONT_FAMILY);
        GameLevelInterface gameLevel = MonitoringActivity.getGameLevelDesign(gameFont);
        HeroSprite hero = new HeroSprite(rootView.getResources(), R.drawable.hero_dora,
                MonitoringActivity.getAdultBalloonDrawables(10),
                MonitoringActivity.getChildBalloonDrawables(10),
                R.color.colorPrimaryLight);
        this.gameController = new MonitoringController(this.gameView);
        this.gameController.setLevelDesign(rootView.getResources(), gameLevel);
        this.gameController.setHeroSprite(hero);
    }

    /* BUTTON METHODS */
    public void startProgressAnimation() {
        if (this.progressAnimationStatus == ProgressAnimationStatus.UNSTARTED) {
            this.tryStartProgressAnimation();
        }
    }

    public void resetProgressAnimation() {
        if (this.progressAnimationStatus != ProgressAnimationStatus.UNSTARTED) {
            this.doResetProgressAnimation();
        }
    }

    public void startSyncAndShowProgressAnimation(View view) {
        if (SyncStatus.FAILED.equals(this.fitnessSyncStatus)) {
            // DO SOMETHING
        } else if (SyncStatus.SUCCESS.equals(this.fitnessSyncStatus)) {
            this.showControlForReady(view);
        } else {
            this.showControlForSyncing(view);
        }
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

    /* VIEW ANIMATOR METHODS */
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

    public boolean processTapOnGameView(MotionEvent event) {
        if (this.isFitnessAndChallengeDataReady()) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (this.gameView.isOverHero(event)) {
                    this.tryStartProgressAnimation();
                }
            }
        }
        return true;
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
                        onSyncStatusChanged(syncStatus, fragment);
                    }
                });
    }

    private void onSyncStatusChanged(SyncStatus syncStatus, Fragment fragment) {
        this.fitnessSyncStatus = syncStatus;

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
            Log.d("SWELL", "Successfully synchronizing all devices.");
            showControlForReady(fragment.getView());
        } else if (SyncStatus.FAILED.equals(syncStatus)) {
            this.isSyncronizingFitnessData = false;
            Log.d("SWELL", "Synchronization failed");
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
    public void tryFetchFitnessChallengeData(Fragment fragment) {
        if (this.isFitnessAndChallengeDataReady() == false){
            this.fetchChallengeAndFitnessData(fragment);
        }
    }

    public void fetchChallengeAndFitnessData(final Fragment fragment) {
        this.fitnessChallengeViewModel = ViewModelProviders.of(fragment).get(FitnessChallengeViewModel.class);
        this.fitnessChallengeViewModel.fetchSevenDayFitness(startDate, endDate).observe(fragment, new Observer<FetchingStatus>() {
            @Override
            public void onChanged(@Nullable final FetchingStatus status) {
                if (status == FetchingStatus.SUCCESS) {
                    Log.d("SWELL", "Fitness data fetched");
                } else if (status == FetchingStatus.NO_INTERNET) {
                    Log.e("SWELL", "Fetching fitness data failed: no internet");
                } else if (status == FetchingStatus.FETCHING) {
                    // DO NOTHING
                } else {
                    Log.e("SWELL", "Fetching fitness data failed: " + status.toString());
                }
            }
        });
    }

    private boolean isFitnessAndChallengeDataReady() {
        return this.fitnessChallengeViewModel != null && this.isFitnessAndChallengeDataFetched();
    }

    private boolean isFitnessAndChallengeDataFetched() {
        return fitnessChallengeViewModel.getFetchingStatus() == FetchingStatus.SUCCESS;
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
            GroupFitnessInterface groupFitness = fitnessChallengeViewModel.getSevenDayFitness();
            Log.d("SWELL", groupFitness.toString());
        } catch (FitnessException e) {
            e.printStackTrace();
        }
    }
}
