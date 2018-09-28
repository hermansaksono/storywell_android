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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import edu.neu.ccs.wellness.fitness.interfaces.FitnessException;
import edu.neu.ccs.wellness.fitness.interfaces.GroupFitnessInterface;
import edu.neu.ccs.wellness.logging.Param;
import edu.neu.ccs.wellness.logging.WellnessUserLogging;
import edu.neu.ccs.wellness.people.Person;
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

    public static final int CONTROL_PLAY = 0;
    public static final int CONTROL_SYNCING = 1;
    public static final int CONTROL_READY = 2;
    public static final int CONTROL_PROGRESS_INFO = 3;
    public static final int CONTROL_SYNCING_CAREGIVER = 4;
    public static final int CONTROL_SYNCING_CHILD = 5;
    public static final int CONTROL_PREV_NEXT = 6;
    public static final String DATE_FORMAT_STRING = "EEE, MMM d";
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

    /* VIEW ANIMATOR METHODS */
    private void setContolChangeToMoveLeft(Context context) {
        controlViewAnimator.setInAnimation(context, R.anim.view_move_left_next);
        controlViewAnimator.setOutAnimation(context, R.anim.view_move_left_current);
    }

    private void setContolChangeToMoveRight(Context context) {
        controlViewAnimator.setInAnimation(context, R.anim.view_move_right_prev);
        controlViewAnimator.setOutAnimation(context, R.anim.view_move_right_current);
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

    /* FITNESS SYNC VIEWMODEL METHODS */
    private boolean stopSyncFitnessData() {
        if (this.isSyncronizingFitnessData) {
            this.fitnessSyncViewModel.stop();
            this.isSyncronizingFitnessData = false;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean trySyncFitnessData(final Fragment fragment) {
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
            Log.d("SWELL", "Starting sync process...");
            this.isSyncronizingFitnessData = true;
            this.fitnessSyncViewModel.perform();
            return true;
        }
    }

    private void onSyncStatusChanged(SyncStatus syncStatus, Fragment fragment) {
        this.fitnessSyncStatus = syncStatus;

        if (SyncStatus.NO_NEW_DATA.equals(syncStatus)) {
            Log.d("SWELL", "No new data within interval.");
        } else if (SyncStatus.NEW_DATA_AVAILABLE.equals(syncStatus)) {
            Log.d("SWELL", "New data is available...");
        } else if (SyncStatus.INITIALIZING.equals(syncStatus)) {
            Log.d("SWELL", "Initializing sync...");
        } else if (SyncStatus.CONNECTING.equals(syncStatus)) {
            Log.d("SWELL", "Connecting to: " + getCurrentPersonString());
            this.showControlForSyncingThisPerson(fragment.getView(),
                    fitnessSyncViewModel.getCurrentPerson());
        } else if (SyncStatus.DOWNLOADING.equals(syncStatus)) {
            Log.d("SWELL", "Downloading fitness data: " + getCurrentPersonString());
        } else if (SyncStatus.UPLOADING.equals(syncStatus)) {
            Log.d("SWELL", "Uploading fitness data: " + getCurrentPersonString());
        } else if (SyncStatus.IN_PROGRESS.equals(syncStatus)) {
            Log.d("SWELL", "Sync completed for: " + getCurrentPersonString());
            this.fitnessSyncViewModel.performNext();
        } else if (SyncStatus.COMPLETED.equals(syncStatus)) {
            Log.d("SWELL", "Successfully synchronizing all devices.");
            this.stopSyncFitnessData();
            this.progressAnimationStatus = ProgressAnimationStatus.READY;
            this.showControlForReady(fragment.getContext());
        } else if (SyncStatus.FAILED.equals(syncStatus)) {
            Log.d("SWELL", "Synchronization failed");
            this.stopSyncFitnessData();
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
    @Override
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
        // TODO Check this
        return fitnessChallengeViewModel.getFetchingStatus() == FetchingStatus.SUCCESS
                || this.fitnessSyncStatus == SyncStatus.NO_NEW_DATA;
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
