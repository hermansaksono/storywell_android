package edu.neu.ccs.wellness.storytelling.homeview;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;
import android.widget.ViewAnimator;

import org.json.JSONException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.neu.ccs.wellness.fitness.challenges.ChallengeDoesNotExistsException;
import edu.neu.ccs.wellness.fitness.interfaces.ChallengeStatus;
import edu.neu.ccs.wellness.fitness.interfaces.FitnessException;
import edu.neu.ccs.wellness.people.Person;
import edu.neu.ccs.wellness.people.PersonDoesNotExistException;
import edu.neu.ccs.wellness.story.StoryChapterManager;
import edu.neu.ccs.wellness.storytelling.MonitoringActivity;
import edu.neu.ccs.wellness.storytelling.R;
import edu.neu.ccs.wellness.storytelling.ResolutionActivity;
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
import edu.neu.ccs.wellness.storytelling.utils.UserLogging;
import edu.neu.ccs.wellness.storytelling.viewmodel.FitnessSyncViewModel;
import edu.neu.ccs.wellness.storytelling.sync.SyncStatus;
import edu.neu.ccs.wellness.storytelling.sync.FetchingStatus;
import edu.neu.ccs.wellness.storytelling.viewmodel.FitnessChallengeViewModel;
import edu.neu.ccs.wellness.trackers.miband2.MiBandScanner;
import edu.neu.ccs.wellness.utils.WellnessDate;
import edu.neu.ccs.wellness.utils.WellnessStringFormatter;

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
    public static final int CONTROL_RUNNING_NOT_STARTED = 10;
    public static final int CONTROL_SYNCING_FAILED = 11;
    public static final String DATE_FORMAT_STRING = "EEE, MMM d";
    private static final String LOG_TAG = "SWELL-ADV";
    private static final String STRING_NO_DATA = "--";
    public static final int REQUEST_ENABLE_BT = 8100;
    private int heroId;
    private int heroResId;
    private int [] drawableHeroIdArray = new int[Constants.NUM_HERO_DRAWABLES];
    private boolean isDemoMode;

    private Calendar today;
    //private GregorianCalendar startDate;
    //private GregorianCalendar endDate;
    private ProgressAnimationStatus progressAnimationStatus = ProgressAnimationStatus.UNREADY;
    private SyncStatus fitnessSyncStatus = SyncStatus.UNINITIALIZED;
    private boolean isSyncronizingFitnessData = false;
    private Storywell storywell;

    private View rootView;
    private ViewAnimator gameviewViewAnimator;
    private ViewAnimator controlViewAnimator;

    private TextView adultStepsTextview;
    private TextView childStepsTextview;
    private TextView adultGoalTextview;
    private TextView childGoalTextview;
    private TextView dateTextView;
    private TextView dateLabelTextView;

    private final TextView challengeWillStartTV;

    private Integer adultInitialSteps = null;
    private Integer childInitialSteps = null;

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
        //this.startDate = WellnessDate.getFirstDayOfWeek(this.today);
        //this.endDate = WellnessDate.getEndDate(this.startDate);
        this.storywell = new Storywell(rootView.getContext());
        this.heroId = storywell.getSynchronizedSetting().getHeroCharacterId();
        this.drawableHeroIdArray = Constants.HERO_DRAWABLES[this.heroId];
        this.heroResId = this.drawableHeroIdArray[Constants.HERO_DRAWABLE_FLYING];

        /* Demo mode */
        this.isDemoMode = storywell.getSynchronizedSetting().isDemoMode();

        /* Views */
        this.rootView = rootView;
        this.gameviewViewAnimator = this.rootView.findViewById(R.id.gameview_view_animator);
        this.controlViewAnimator = this.rootView.findViewById(R.id.control_view_animator);
        this.gameView = rootView.findViewById(R.id.layout_monitoringView);


        /* Show family members' names */
        String adultName = storywell.getCaregiver().getName();
        String childName = storywell.getChild().getName();
        String template = rootView.getResources().getString(R.string.adventure_user_steps);
        TextView adultNameTv = this.rootView.findViewById(R.id.textview_progress_adult_label);
        TextView childNameTv = this.rootView.findViewById(R.id.textview_progress_child_label);
        adultNameTv.setText(String.format(template, adultName));
        childNameTv.setText(String.format(template, childName));

        /* Game visor views */
        this.adultStepsTextview = this.rootView.findViewById(R.id.textview_progress_adult);
        this.childStepsTextview = this.rootView.findViewById(R.id.textview_progress_child);
        this.adultGoalTextview = this.rootView.findViewById(R.id.textview_progress_adult_goal);
        this.childGoalTextview = this.rootView.findViewById(R.id.textview_progress_child_goal);

        /* Game control view */
        this.challengeWillStartTV = this.rootView.findViewById(R.id.text_challenge_will_start);

        /* Set the date */
        this.dateTextView = this.rootView.findViewById(R.id.textview_date);
        this.dateLabelTextView = this.rootView.findViewById(R.id.textview_date_label);

        /* Game's Controller */
        this.gameController = getGameController(this.gameView, this.drawableHeroIdArray);

        /* Setting */
        this.completedChallenges = storywell.getSynchronizedSetting()
                .getStoryListInfo().getUnlockedStoryPages();
    }

    /**
     * Sets the listener for communication between AdventureFragment and the HomeActivity.
     * @param listener
     */
    @Override
    public void setAdventureFragmentListener(AdventurePresenterListener listener) {
        this.adventureFragmentListener = listener;
    }

    /* ===================== CHALLENGE AND FITNESS DATA VIEWMODEL METHODS ====================== */
    /**
     * Download current challenge data from WellnessServer and fitness data from Firebase.
     * When completed, start synchronizing fitness data from the bands.
     * @param fragment
     */
    @Override
    public void tryFetchChallengeAndFitnessData(final Fragment fragment) {
        this.fitnessChallengeViewModel = ViewModelProviders.of(fragment)
                .get(FitnessChallengeViewModel.class);
        this.fitnessChallengeViewModel.getChallengeLiveData().observe(fragment, new Observer<FetchingStatus>() {
            @Override
            public void onChanged(@Nullable final FetchingStatus status) {
                onDataFetchingStatusChange(status, fragment);
            }
        });
        this.fitnessChallengeViewModel.refreshChallengeAndFitnessData();
    }

    @Override
    public void stopObservingChallengeData(Fragment fragment) {
        if (this.fitnessChallengeViewModel != null) {
            this.fitnessChallengeViewModel.getChallengeLiveData().removeObservers(fragment);
        }
    }

    private void onDataFetchingStatusChange(FetchingStatus status, Fragment fragment) {
        switch (status) {
            case FETCHING:
                break;
            case SUCCESS:
                Log.d(LOG_TAG, "Loading challenge and fitness data successful");
                doHandleFetchingSuccess(fragment);
                break;
            case NO_INTERNET:
                Log.e(LOG_TAG, "Failed to load challenge and fitness data: no internet");
                break;
            default:
                Log.e(LOG_TAG, "Failed to load challenge and fitness d data: "
                        + status.toString());
                break;
        }
    }

    private void doHandleFetchingSuccess(Fragment fragment) {
        try {
            this.doProcessFitnessChallenge(fragment);
            this.initializeFitnessSync(fragment);
            // this.trySyncFitnessData(fragment); Disable auto syncing
        } catch (ChallengeDoesNotExistsException e) {
            Log.e(LOG_TAG, "Fitness challenge does not exist");
        }
    }

    private void doProcessFitnessChallenge(Fragment fragment)
            throws ChallengeDoesNotExistsException {
        if (this.isDemoMode) {
            this.updateGroupStepsProgress();
            this.onChallengeIsRunning(fragment);
            return;
        }

        if (!this.isFitnessAndChallengeDataFetched()) {
            return;
        }

        ChallengeStatus status = this.fitnessChallengeViewModel.getChallengeStatus();
        Date todayDate = today.getTime();

        switch(status) {
            case AVAILABLE:
                this.dateLabelTextView.setVisibility(View.INVISIBLE);
                onChallengeAvailable(fragment);
                break;
            case UNSYNCED_RUN:
                // PASS to show Sync control
            case RUNNING:
                todayDate = fitnessChallengeViewModel.getDateToVisualize();
                this.dateLabelTextView.setVisibility(View.VISIBLE);
                this.updateChallengeDateText(todayDate);

                long timeFromChallengeStartToNow = fitnessChallengeViewModel
                        .getTimeElapsedFromStartToNow();

                if (timeFromChallengeStartToNow >= 0) {
                    this.onChallengeIsRunning(fragment);
                } else {
                    this.onChallengeWillStartRunningTomorrow(fragment, timeFromChallengeStartToNow);
                }
                break;
            case PASSED:
                todayDate = fitnessChallengeViewModel.getDateToVisualize();
                this.dateLabelTextView.setVisibility(View.VISIBLE);

                this.updateChallengeDateText(todayDate);

                this.updateGroupGoal();
                this.updateGroupStepsProgress();
                if (isLastSyncAfterEndDate()) {
                    this.onChallengeHasPassed(fragment);
                } else {
                    this.onChallengeIsRunning(fragment);
                }
                break;
            case CLOSED:
                this.dateLabelTextView.setVisibility(View.INVISIBLE);
                this.updateGroupGoal();
                this.updateGroupStepsProgress();
                this.doHandleChallengeClosed(fragment);
                break;
            default:
                break;
        }
    }

    private boolean isFitnessAndChallengeDataFetched() { // TODO Check this
        return fitnessChallengeViewModel.getFetchingStatus() == FetchingStatus.SUCCESS
                || this.fitnessSyncStatus == SyncStatus.NO_NEW_DATA;
    }

    private boolean isLastSyncAfterEndDate() {
        try {
            SynchronizedSetting setting = storywell.getSynchronizedSetting();
            long adultLastSyncTime = setting.getFitnessSyncInfo().getCaregiverDeviceInfo()
                    .getLastSyncTime();
            long childLastSyncTime = setting.getFitnessSyncInfo().getChildDeviceInfo()
                    .getLastSyncTime();
            long endTime = storywell.getChallengeManager().getRunningChallenge().getEndDate()
                    .getTime();
            return (adultLastSyncTime >= endTime) && (childLastSyncTime >= endTime);
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void onChallengeAvailable(Fragment fragment) {
        this.showNoRunningChallenges(fragment.getContext());
    }

    private void onChallengeIsRunning(Fragment fragment) throws ChallengeDoesNotExistsException {
        this.updateGroupGoal();
        this.updateGroupStepsProgress();
        this.setGameviewVisorIsVisible(true);
        this.setHeroIsVisible(true);

        if (this.fitnessSyncStatus == SyncStatus.COMPLETED) {
            this.progressAnimationStatus = ProgressAnimationStatus.READY;
            this.showControlForReady(fragment.getContext());
        } else {
            this.showControlForFirstCard(fragment.getContext());
        }
    }

    private void onChallengeWillStartRunningTomorrow(Fragment fragment, long startToNow) {
        long nowToStart = Math.abs(startToNow);
        int hoursToGo = (int) Math.ceil( (nowToStart + (4 * WellnessDate.MILLISEC_IN_HOUR))
                / WellnessDate.MILLISEC_IN_HOUR);
        String infoTextTemplate = fragment.getString(R.string.adventure_info_challenge_will_start);
        String infoText = String.format(infoTextTemplate, hoursToGo);

        this.updateGroupGoal();
        this.challengeWillStartTV.setText(infoText);
        this.setGameviewVisorIsVisible(true);
        this.setHeroIsVisible(true);
        this.showControlForChallengeWillStartTomorrow(fragment.getContext());
    }

    private void onChallengeHasPassed(Fragment fragment) throws ChallengeDoesNotExistsException {
        this.progressAnimationStatus = ProgressAnimationStatus.READY;
        this.showControlForReady(fragment.getContext());
    }

    private void doHandleChallengeClosed(Fragment fragment) {
        progressAnimationStatus = ProgressAnimationStatus.READY;
    }

    /* =========================== FITNESS SYNC METHODS ========================================*/

    /**
     * Intialize the fitnessSyncViewModel.
     * @param fragment
     */
    private void initializeFitnessSync(final Fragment fragment) {
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
    }

    /**
     * Start synchronizing fitness data and update the UI elements.
     * If the family is in the demo mode, then synchronization will not happen.
     * @param fragment
     * @return
     */
    @Override
    public boolean trySyncFitnessData(final Fragment fragment) {
        if (this.isDemoMode) {
            this.progressAnimationStatus = ProgressAnimationStatus.READY;
            return false;
        }

        if (!MiBandScanner.isEnabled()) {
            this.showControlForFirstCard(fragment.getContext());
            Activity activity = fragment.getActivity();
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return false;
        }

        this.initializeFitnessSync(fragment);

        if (this.isSyncronizingFitnessData) { // TODO Use this.fitnessSyncStatus instead
            return false;
        } else {
            Log.d(LOG_TAG, "Starting sync process...");
            this.showControlForSyncing(fragment.getContext());
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
                this.progressAnimationStatus = ProgressAnimationStatus.READY;
                this.showControlForReady(fragment.getContext());
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
                this.showControlForSyncing(fragment.getContext());
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
                this.showControlForSyncingFailed();
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
        // this.fitnessChallengeViewModel.refreshFitnessDataOnly(startDate, endDate);
        this.fitnessChallengeViewModel.refreshChallengeAndFitnessData();
    }

    private String getCurrentPersonString() {
        StorywellPerson person = fitnessSyncViewModel.getCurrentPerson();
        if (person != null) {
            return person.toString();
        } else {
            return "Null Person";
        }
    }

    /* ============================== PROGRESS ANIMATION METHODS ================================*/
    /**
     * Animate the hero to fly from the first island to the next island, respective to their
     * overall goal.
     */
    private void doStartProgressAnimation() {
        try {
            float adultProgress = this.fitnessChallengeViewModel.getAdultProgress();
            float childProgress = this.fitnessChallengeViewModel.getChildProgress();
            float overallProgress = this.fitnessChallengeViewModel.getOverallProgress();

            UserLogging.logProgressAnimation(adultProgress, childProgress, overallProgress);

            if (overallProgress >= Constants.MINIMUM_PROGRESS_FOR_ANIMATION) {
                this.gameController.setProgress(adultProgress, childProgress, overallProgress,
                        new OnAnimationCompletedListener() {
                            @Override
                            public void onAnimationCompleted() {
                                onProgressAnimationCompleted();
                            }
                        });

                this.progressAnimationStatus = ProgressAnimationStatus.PLAYING;
            } else {
                onProgressAnimationCompleted();
                this.progressAnimationStatus = ProgressAnimationStatus.ENDED;
            }

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

    /**
     * Upon animation completion, show the relevant control based on the {@link ChallengeStatus}.
     * For example, if the status is {@link ChallengeStatus#RUNNING}, then show the control when
     * a challenge is still running.
     */
    private void onProgressAnimationCompleted() {
        // Handle demo mode
        if (this.isDemoMode) {
            //this.updateGroupStepsProgress();
            this.doAnimateHeroUponAnimationCompletion();
        }

        // Set progress animation as ENDED
        this.progressAnimationStatus = ProgressAnimationStatus.ENDED;

        try {
            ChallengeStatus status = this.fitnessChallengeViewModel.getChallengeStatus();

            switch(status) {
                case AVAILABLE:
                    // PASS will not be called when status is AVAILABLE
                    break;
                case UNSYNCED_RUN:
                    // PASS to show Sync control
                case RUNNING:
                    this.doAnimateHeroUponAnimationCompletion();
                    this.showControlForRunning(this.rootView.getContext());
                    break;
                case PASSED:
                    this.doAnimateHeroUponAnimationCompletion();
                    this.showControlForPassed(this.rootView.getContext());
                    break;
                case CLOSED:
                    this.doAnimateHeroUponAnimationCompletion();
                    this.showControlForClosed(this.rootView.getContext());
                    break;
                default:
                    break;
            }
        } catch (ChallengeDoesNotExistsException e) {
            Log.e(LOG_TAG, "Challenge is not yet initialized.");
        }
    }

    private void doAnimateHeroUponAnimationCompletion() {
        if (this.fitnessChallengeViewModel.isChallengeAchieved()) {
            this.gameController.setHeroChallengeAsCompleted();
        }
    }

    private void updateChallengeDateText(Date date) {
        dateTextView.setText(new SimpleDateFormat(DATE_FORMAT_STRING, Locale.US)
                .format(date));
    }

    /* ================================ BUTTON AND TAP METHODS ================================ */
    /**
     * Given an MotionEvent and a View, try start start progress animation when ready.
     * @param event
     * @param view
     * @return
     */
    @Override
    public boolean onTouchOnGameView(MotionEvent event, View view) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return true; // Return true so the subsequent events can be processed.
            case MotionEvent.ACTION_UP:
                return onGameViewIsTapped(event, view);
            default:
                return false;
        }
    }

    /**
     * If the tap position in {@param event} is over the hero, then do something. Otherwise don't
     * do anything.
     * @param event
     * @param view
     * @return Return true if over the hero. Otherwise return false.
     */
    private boolean onGameViewIsTapped(MotionEvent event, View view) {
        if (this.gameView.isOverHero(event)) {
            return onHeroIsTapped(event, view);
        } else {
            return false;
        }
    }

    /**
     * If the user tapped over the hero, then do something based on the
     * {@link ProgressAnimationStatus}.
     * @param event
     * @param view
     * @return
     */
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
            case ENDED:
                this.showNextStepForTheHero(view);
                return false;
            default:
                return false;
        }
    }

    /**
     * If the challenge has been completed, show the completion prompt. Otherwise, start the
     * resolution activity.
     * @param view
     */
    private void showNextStepForTheHero(View view) {
        if (this.fitnessChallengeViewModel.isChallengeAchieved()) {
            this.showChallengeCompletionDialog(view);
        } else if (this.fitnessChallengeViewModel.hasChallengePassed()) {
            this.startResolutionActivity(view.getContext());
        } else {
            // Don't do anything because the Hero should not react to tap when
            // a challenge is still running
        }
    }

    /**
     * Show a {@link ChallengeCompletedDialog} which shows the user that they can unlock a story.
     * @param view
     */
    private void showChallengeCompletionDialog(final View view) {
        SynchronizedSetting setting = storywell.getSynchronizedSetting();
        if (setting.getStoryChallengeInfo().getIsSet()) {
            String title = setting.getStoryChallengeInfo().getStoryTitle();
            String coverImageUri = setting.getStoryChallengeInfo().getStoryCoverImageUri();
            AlertDialog dialog = ChallengeCompletedDialog.newInstance(
                    title, coverImageUri, view.getContext(),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            onStoryChallengeUnlocked(view);
                        }
                    });
            dialog.show();
        }
    }

    /**
     * Unlock story challenge, save it to Firebase, and go to the Stories tab.
     * @param view
     */
    private void onStoryChallengeUnlocked(View view) {
        /*
        try {
            SynchronizedSetting setting = storywell.getSynchronizedSetting();
            String storyId = setting.getStoryChallengeInfo().getStoryId();

            unlockCurrentStoryChallenge(view.getContext());
            closeChallengeInfo(view.getContext());
            //fitnessChallengeViewModel.setChallengeClosed();
            adventureFragmentListener.goToStoriesTab(storyId);
        } catch (ChallengeDoesNotExistsException e) {
            Log.e("SWELL", "Can't unlock story. Challenge does not exist.");
            e.printStackTrace();
        }
        */

        SynchronizedSetting setting = storywell.getSynchronizedSetting();
        final Context context = view.getContext();
        final String storyId = setting.getStoryChallengeInfo().getStoryId();

        new CloseChallengeUnlockStoryAsync(view.getContext(), this.rootView,
                new CloseChallengeUnlockStoryAsync.OnUnlockingEvent(){

                    @Override
                    public void onClosingSuccess() {
                        HomeAdventurePresenter.setStoryChallengeAsClosed(context);
                        adventureFragmentListener.goToStoriesTab(storyId);
                    }

                    @Override
                    public void onClosingFailed() {
                        // TODO Don't do anything for now
                    }

                }).execute();
    }

    /**
     * Start the {@link ResolutionActivity} that allows user to randomly pick what is going to
     * happen next.
     * @param context
     */
    private void startResolutionActivity(Context context) {
        Intent intent = new Intent(context, ResolutionActivity.class);
        context.startActivity(intent);
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

    private void doResetProgressAnimation() {
        this.progressAnimationStatus = ProgressAnimationStatus.READY;
        this.gameController.resetProgress();
    }

    /* ================================ BLUETOOTH METHODS ================================ */
    /**
     * Update the status bar with information about the fitness status.
     */
    @Override
    public void startPerformBluetoothSync(Fragment fragment) {
        if (this.isDemoMode) {
            this.showControlForReady(fragment.getContext());
            return;
        }

        UserLogging.logButtonPlayPressed();

        // TODO this switch is almost useless now. Consider merge with trySyncFitnessData.
        switch(this.fitnessSyncStatus) {
            case UNINITIALIZED:
                this.trySyncFitnessData(fragment);
                break;
            case NO_NEW_DATA:
                this.showControlForReady(fragment.getContext());
                break;
            case NEW_DATA_AVAILABLE:
                this.trySyncFitnessData(fragment);
                break;
            case COMPLETED:
                this.showControlForReady(fragment.getContext());
                break;
            case FAILED:
                this.trySyncFitnessData(fragment);
                break;
            default:
                this.showControlForSyncing(fragment.getContext());
                break;
        }
    }

    /* ================================ CONTROL AREA METHODS ================================ */
    /**
     * Update the status bar to show the first card.
     * @param context
     */
    @Override
    public void showControlForFirstCard(Context context) {
        if (controlViewAnimator.getDisplayedChild() != CONTROL_PLAY) {
            this.setContolChangeToMoveRight(context);
            controlViewAnimator.setDisplayedChild(CONTROL_PLAY);
        }
    }

    /**
     * Update status bar to show the syncing control.
     * @param context
     */
    private void showControlForSyncing(Context context) {
        this.setContolChangeToMoveLeft(context);
        controlViewAnimator.setDisplayedChild(CONTROL_SYNCING);
    }

    /**
     * Update status bar to show the person whose Bluetooth is currently synchronizing.
     * @param view
     * @param storywellPerson
     */
    private void showControlForSyncingThisPerson(View view, StorywellPerson storywellPerson) {
        try {
            ChallengeStatus status = fitnessChallengeViewModel.getChallengeStatus();
            this.showSyncingControlByChallengeStatus(view, storywellPerson, status);
        } catch (ChallengeDoesNotExistsException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "Challenge does not exist");
        }
    }

    /**
     * Show syncing control if there is a running challenge.
     * @param view
     * @param storywellPerson
     * @param status
     */
    private void showSyncingControlByChallengeStatus(
            View view, StorywellPerson storywellPerson, ChallengeStatus status) {
        switch(status) {
            case AVAILABLE:
                break;
            case UNSYNCED_RUN:
                // PASS to show Sync control
            case RUNNING:
                // PASS
            case PASSED:
                long timeFromChallengeStartToNow = fitnessChallengeViewModel
                        .getTimeElapsedFromStartToNow();

                if (timeFromChallengeStartToNow >= 0) {
                    this.showSyncingControlForRunningChallenge(view, storywellPerson);
                }
                break;
            case CLOSED:
                break;
            default:
                break;
        }
    }

    /**
     * Show syncing controll based on who is currently being synced.
     * @param view
     * @param storywellPerson
     */
    private void showSyncingControlForRunningChallenge(View view, StorywellPerson storywellPerson) {
        boolean isFirstControlShown = this.controlViewAnimator.getDisplayedChild() != CONTROL_PLAY;

        if (isFirstControlShown) {
            String name = storywellPerson.getPerson().getName();

            switch(storywellPerson.getPerson().getRole()) {
                case Person.ROLE_PARENT:
                    this.showControlForSyncingCaregiver(view, name);
                    break;
                case Person.ROLE_CHILD:
                    this.showControlForSyncingChild(view, name);
                    break;
            }
        }
    }

    private void showControlForSyncingCaregiver(View view, String name) {
        this.setContolChangeToMoveLeft(view.getContext());
        this.controlViewAnimator.setDisplayedChild(CONTROL_SYNCING_CAREGIVER);

        String text = view.getResources().getString(R.string.adventure_info_syncing_caregiver);
        TextView textView = view.findViewById(R.id.text_syncing_caregiver_info);

        textView.setText(String.format(text, name));
    }

    private void showControlForSyncingChild(View view, String name) {
        this.setContolChangeToMoveLeft(view.getContext());
        this.controlViewAnimator.setDisplayedChild(CONTROL_SYNCING_CHILD);

        String text = view.getResources().getString(R.string.adventure_info_syncing_child);
        TextView textView = view.findViewById(R.id.text_syncing_child_info);

        textView.setText(String.format(text, name));
    }

    private void showControlForSyncingFailed() {
        this.controlViewAnimator.setDisplayedChild(CONTROL_SYNCING_FAILED);
        UserLogging.logBleFailed();
    }

    /**
     * If there's no running challenge, then (1) hide the hero and the game visor; and (2) show
     * control no running challenge.
     * @param context
     */
    private void showNoRunningChallenges(Context context) {
        if (this.controlViewAnimator.getDisplayedChild() != CONTROL_NO_RUNNING) {
            this.setGameviewVisorIsVisible(false);
            this.setHeroIsVisible(false);
            this.setContolChangeToMoveRight(context);
            this.controlViewAnimator.setDisplayedChild(CONTROL_NO_RUNNING);
        }
    }

    private void showControlForChallengeWillStartTomorrow(Context context) {
        if (controlViewAnimator.getDisplayedChild() != CONTROL_RUNNING_NOT_STARTED) {
            this.setContolChangeToMoveLeft(context);
            controlViewAnimator.setDisplayedChild(CONTROL_RUNNING_NOT_STARTED);
        }
    }

    /**
     * Show control for when all fitness data has ben downloaded and ready for progress animation.
     * @param context
     */
    private void showControlForReady(Context context) {
        if (controlViewAnimator.getDisplayedChild() != CONTROL_READY) {
            this.setContolChangeToMoveLeft(context);
            controlViewAnimator.setDisplayedChild(CONTROL_READY);
        }
    }

    /** Show control when bluetooth synchronization is complete and there is a running challenge */
    private void showControlForRunning(Context context) {
        if (this.fitnessChallengeViewModel.isChallengeAchieved()) {
            this.showControlForAchieved(context);
        } else if (this.fitnessChallengeViewModel.hasChallengePassed()) {
            this.showControlForMissed(this.rootView.getContext());
        } else {
            this.showControlForProgressInfo(context);
        }
    }

    /** Show control when a challenge has passed */
    private void showControlForPassed(Context context) throws ChallengeDoesNotExistsException {
        if (this.fitnessChallengeViewModel.isChallengeAchieved()) {
            this.showControlForAchieved(context);
        } else {
            this.showControlForMissed(this.rootView.getContext());
        }
    }

    /** Show control when a challenge has been closed */
    private void showControlForClosed(Context context) throws ChallengeDoesNotExistsException {
        if (this.fitnessChallengeViewModel.isChallengeAchieved()) {
            this.showControlForAchieved(context);
        } else {
            this.showControlForMissed(context);
        }
    }

    /** Show control for progress info. */
    public void showControlForProgressInfo(Context context) {
        this.setContolChangeToMoveLeft(context);
        controlViewAnimator.setDisplayedChild(CONTROL_PROGRESS_INFO);
    }

    /** Show control for prev next screen. */
    @Override
    public void showControlForPrevNext(Context context) {
        this.setContolChangeToMoveRight(context);
        controlViewAnimator.setDisplayedChild(CONTROL_PREV_NEXT);
    }

    /** Show control when a user has achieved their challenge. */
    private void showControlForAchieved(Context context) {
        this.setContolChangeToMoveLeft(context);
        controlViewAnimator.setDisplayedChild(CONTROL_CLOSED);
    }

    /** Show control when a user has missed their challenge. */
    private void showControlForMissed(Context context) {
        this.setContolChangeToMoveLeft(context);
        controlViewAnimator.setDisplayedChild(CONTROL_MISSED);
    }

    /**
     * If {@param isVisible} is true, then display the top visor that shows the steps data. If false
     * then hide the visor.
     * @param isVisible
     */
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

    /**
     * Hide or unhide the hero.
     * @param isVisible If true, then show the hero. If alse, hide the hero.
     */
    private void setHeroIsVisible(boolean isVisible) {
        if (this.isDemoMode) {
            return;
        }
        this.gameController.setHeroIsVisible(isVisible);
    }

    /**
     * Update the {@link TextView}s that show the adult and the child's steps progress.
     */
    private void updateGroupStepsProgress() {
        try {
            int adultSteps = this.fitnessChallengeViewModel.getAdultSteps();
            int childSteps = this.fitnessChallengeViewModel.getChildSteps();

            if (this.adultInitialSteps == null || this.childInitialSteps == null) {
                String adultStepsStr = getFormattedSteps(fitnessChallengeViewModel.getAdultSteps());
                String childStepsStr = getFormattedSteps(fitnessChallengeViewModel.getChildSteps());

                this.adultStepsTextview.setText(adultStepsStr);
                this.childStepsTextview.setText(childStepsStr);
            } else {
                this.doAnimateStepsText(this.adultInitialSteps, adultSteps,
                        this.childInitialSteps, childSteps);
            }

            this.adultInitialSteps = Math.max(adultSteps, 0);
            this.childInitialSteps = Math.max(childSteps, 0);

        } catch (PersonDoesNotExistException e) {
            e.printStackTrace();
        }
    }

    private void doAnimateStepsText(final int adultInitialSteps, int adultSteps,
                                    final int childInitialSteps, int childSteps) {
        final int adultDiff = adultSteps - adultInitialSteps;
        final int childDiff = childSteps - childInitialSteps;

        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1.0f);
        valueAnimator.setDuration((int) (Constants.ANIM_BALLOON_UPDATE_PERIOD * Constants.MICROSECONDS));
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float ratio = (float) animation.getAnimatedValue();

                int adultStepsAnim = (int) ((ratio * adultDiff) + adultInitialSteps);
                int childStepsAnim = (int) ((ratio * childDiff) + childInitialSteps);
                adultStepsTextview.setText(getFormattedSteps(adultStepsAnim));
                childStepsTextview.setText(getFormattedSteps(childStepsAnim));
            }
        });
        valueAnimator.start();
    }

    /**
     * Update the {@link TextView}s that show the adult and the child's step goals.
     */
    private void updateGroupGoal() {
        String adultGoal = this.getGoalString(Person.ROLE_PARENT);
        String childGoal = this.getGoalString(Person.ROLE_CHILD);
        adultGoalTextview.setText(adultGoal);
        childGoalTextview.setText(childGoal);
    }

    private String getGoalString(String personRoleType) {
        try {
            int goal;
            switch (personRoleType) {
                case Person.ROLE_PARENT:
                    goal = this.fitnessChallengeViewModel.getAdultGoal();
                    break;
                case Person.ROLE_CHILD:
                    goal = this.fitnessChallengeViewModel.getChildGoal();
                    break;
                default:
                    goal = 0;
                    break;
            }
            return getFormattedSteps(goal);
        } catch (ChallengeDoesNotExistsException e) {
            e.printStackTrace();
            return STRING_NO_DATA;
        } catch (PersonDoesNotExistException e) {
            e.printStackTrace();
            return STRING_NO_DATA;
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

    /* CHALLENGE CHAPTER METHODS */
    @Override
    public boolean markCurrentChallengeAsUnlocked(Context context) {
        StoryChapterManager storyChapterManager = new StoryChapterManager(context);
        return storyChapterManager.setCurrentChallengeAsUnlocked(context);
    }

    /**
     * Get the locked story from Firebase's StoryChallengeInfo, and put it into the UnlockedStories,
     * UnreadStories, and UnlockedStoryPages. Then finally, reset StoryChallengeInfo.
     * @param context
     */
    public static void setStoryChallengeAsClosed(Context context) {
        SynchronizedSetting setting = SynchronizedSettingRepository.getLocalInstance(context);
        String storyIdToBeUnlocked = setting.getStoryChallengeInfo().getStoryId();
        String chapterIdToBeUnlocked = setting.getStoryChallengeInfo().getChapterIdToBeUnlocked();

        if (!setting.getStoryListInfo().getUnlockedStoryPages().contains(chapterIdToBeUnlocked)) {
            setting.getStoryListInfo().getUnlockedStoryPages().add(chapterIdToBeUnlocked);
        }

        if (!setting.getStoryListInfo().getUnlockedStories().contains(storyIdToBeUnlocked)) {
            setting.getStoryListInfo().getUnlockedStories().add(storyIdToBeUnlocked);
        }

        if (!setting.getStoryListInfo().getUnreadStories().contains(storyIdToBeUnlocked)) {
            setting.getStoryListInfo().getUnreadStories().add(storyIdToBeUnlocked);
        }

        if (!setting.isDemoMode()) {
            setting.resetStoryChallengeInfo();
        }

        setting.getChallengeInfo().setCurrentChallengeId("");
        setting.setResolutionInfo(new SynchronizedSetting.ResolutionInfo());

        SynchronizedSettingRepository.saveLocalAndRemoteInstance(setting, context);

        UserLogging.logStoryUnlocked(storyIdToBeUnlocked, chapterIdToBeUnlocked);
    }

    public static void resetResolution(Context context) {
        SynchronizedSetting setting = SynchronizedSettingRepository.getLocalInstance(context);
        setting.setResolutionInfo(new SynchronizedSetting.ResolutionInfo());
        SynchronizedSettingRepository.saveLocalAndRemoteInstance(setting, context);
    }

    public static void unlockCurrentStoryChallenge(Context context) {
        SynchronizedSetting setting = SynchronizedSettingRepository.getLocalInstance(context);
        String storyIdToBeUnlocked = setting.getStoryChallengeInfo().getStoryId();
        String chapterIdToBeUnlocked = setting.getStoryChallengeInfo().getChapterIdToBeUnlocked();

        if (!setting.getStoryListInfo().getUnlockedStoryPages().contains(chapterIdToBeUnlocked)) {
            setting.getStoryListInfo().getUnlockedStoryPages().add(chapterIdToBeUnlocked);
        }

        if (!setting.getStoryListInfo().getUnlockedStories().contains(storyIdToBeUnlocked)) {
            setting.getStoryListInfo().getUnlockedStories().add(storyIdToBeUnlocked);
        }

        if (!setting.getStoryListInfo().getUnreadStories().contains(storyIdToBeUnlocked)) {
            setting.getStoryListInfo().getUnreadStories().add(storyIdToBeUnlocked);
        }

        if (!setting.isDemoMode()) {
            setting.resetStoryChallengeInfo();
        }
        SynchronizedSettingRepository.saveLocalAndRemoteInstance(setting, context);
    }

    public static void closeChallengeInfo(Context context) {
        SynchronizedSetting setting = SynchronizedSettingRepository.getLocalInstance(context);
        setting.getChallengeInfo().setCurrentChallengeId("");
        SynchronizedSettingRepository.saveLocalAndRemoteInstance(setting, context);
    }

    /* FORMATTING METHODS */
    public static String getFormattedSteps(float steps) {
        if (steps == FitnessChallengeViewModel.NULL_STEPS) {
            return STRING_NO_DATA;
        } else {
            return WellnessStringFormatter.getFormattedSteps(Math.round(steps));
        }
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
