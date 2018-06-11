package edu.neu.ccs.wellness.storytelling;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewFlipper;

import java.util.Calendar;
import java.util.Date;

import edu.neu.ccs.wellness.fitness.FitnessDataDoesNotExistException;
import edu.neu.ccs.wellness.fitness.challenges.ChallengeDoesNotExistsException;
import edu.neu.ccs.wellness.fitness.challenges.ChallengeProgressCalculator;
import edu.neu.ccs.wellness.fitness.interfaces.ChallengeStatus;
import edu.neu.ccs.wellness.fitness.interfaces.GroupFitnessInterface;
import edu.neu.ccs.wellness.people.Person;
import edu.neu.ccs.wellness.server.RestServer.ResponseType;
import edu.neu.ccs.wellness.storytelling.viewmodel.FamilyFitnessChallengeViewModel;
import edu.neu.ccs.wellness.storytelling.monitoringview.interfaces.GameLevelInterface;
import edu.neu.ccs.wellness.storytelling.monitoringview.interfaces.GameMonitoringControllerInterface;
import edu.neu.ccs.wellness.storytelling.monitoringview.interfaces.OnAnimationCompletedListener;
import edu.neu.ccs.wellness.storytelling.monitoringview.HeroSprite;
import edu.neu.ccs.wellness.storytelling.monitoringview.MonitoringController;
import edu.neu.ccs.wellness.storytelling.monitoringview.MonitoringView;

public class AdventureFragment extends Fragment {

    /* PRIVATE VARIABLES */
    private FamilyFitnessChallengeViewModel familyFitnessChallengeViewModel;
    private Date today;
    private Date startDate;
    private Date endDate;
    private boolean isProgressAnimationCompleted = false;

    private GameMonitoringControllerInterface monitoringController;

    private MonitoringView gameView;
    private FloatingActionButton fabPlay;
    private View fabCalendarShow;
    private View fabCalendarHide;
    private ViewFlipper viewFlipper;
    private Snackbar currentSnackbar;

    private Typeface gameFont;

    /* CONSTRUCTOR */
    public AdventureFragment() { } // Required empty public constructor

    /* FACTORY METHOD */
    public static AdventureFragment newInstance() {
        return new AdventureFragment();
    }

    /* INTERFACE FUNCTIONS */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        /* Basic data */
        this.today = getStartDate();
        this.startDate = getStartDate();
        this.endDate = getEndDate(this.startDate);

        /* Prepare the UI views */
        View rootView = inflater.inflate(R.layout.fragment_adventure, container, false);
        this.viewFlipper = rootView.findViewById(R.id.view_flipper);
        this.gameView = rootView.findViewById(R.id.layout_monitoringView);
        this.gameFont = ResourcesCompat.getFont(getContext(), MonitoringActivity.FONT_FAMILY);

        // Set up FAB for playing the animation
        this.fabPlay = rootView.findViewById(R.id.fab_action);
        this.fabPlay.setVisibility(View.INVISIBLE);
        this.fabPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isProgressAnimationCompleted == false) {
                    tryStartProgressAnimation();
                } else {
                    resetProgressAnimation();
                }

            }
        });

        // Set up FAB to show the calendar
        this.fabCalendarShow = rootView.findViewById(R.id.fab_show_calendar);
        this.fabCalendarShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            viewFlipper.setInAnimation(view.getContext(), R.anim.overlay_move_down);
            viewFlipper.setOutAnimation(view.getContext(), R.anim.basecard_move_down);
            viewFlipper.showNext();
            }
        });

        // Set up FAB to hide the calendar
        this.fabCalendarHide = rootView.findViewById(R.id.fab_seven_day_close);
        this.fabCalendarHide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            viewFlipper.setInAnimation(view.getContext(), R.anim.basecard_move_up);
            viewFlipper.setOutAnimation(view.getContext(), R.anim.overlay_move_up);
            viewFlipper.showPrevious();
            }
        });

        /* Prepare the Game's Controller */
        GameLevelInterface gameLevel = MonitoringActivity.getGameLevelDesign(this.gameFont);
        HeroSprite hero = new HeroSprite(getResources(), R.drawable.hero_dora,
                MonitoringActivity.getAdultBalloonDrawables(10),
                MonitoringActivity.getChildBalloonDrawables(10),
                R.color.colorPrimaryLight);

        this.monitoringController = new MonitoringController(gameView);
        this.monitoringController.setLevelDesign(getResources(), gameLevel);
        this.monitoringController.setHeroSprite(hero);

        this.gameView.setOnTouchListener (new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                processTap(event);
                return true;
            }
        });

        return rootView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUserVisibleHint(false);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (isFitnessAndChallengeDataReady() == false) {
            showCheckingAdventureMessage();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        this.monitoringController.start();

        if (isFitnessAndChallengeDataReady() == false){
            doFetchChallengeAndFitnessData();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        this.monitoringController.stop();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    /* SCREEN AND ANIMATIONS */
    private void processTap(MotionEvent event) {
        if (isFitnessAndChallengeDataReady()) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (gameView.isOverHero(event)) {
                    this.tryStartProgressAnimation();
                }
            }
        }
    }

    private void tryStartProgressAnimation() {
        if (isFitnessAndChallengeDataFetched()) {
            this.currentSnackbar.dismiss();
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
            this.monitoringController.setProgress(adultProgress, childProgress, overallProgress, new OnAnimationCompletedListener() {
                        @Override
                        public void onAnimationCompleted() {
                            showPostProgressAnimationMessage();
                            setFabPlayToRewind();
                            isProgressAnimationCompleted = true;
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resetProgressAnimation() {
        isProgressAnimationCompleted = false;

        this.monitoringController.resetProgress();
        showProgressAnimationInstructionSnackbar();
        setFabPlayToOriginal();
    }

    private void setRunningChallengeExists() {
        this.fabPlay.show();
    }

    private void setNoRunningChallenge() {
        this.fabPlay.hide();
    }

    private void doFetchChallengeAndFitnessData() {
        this.familyFitnessChallengeViewModel = getFamilyFitnessChallengeViewModel();
        showDownloadingFitnessDataMessage();
    }

    private FamilyFitnessChallengeViewModel getFamilyFitnessChallengeViewModel () {
        FamilyFitnessChallengeViewModel viewModel;
        viewModel = ViewModelProviders.of(this).get(FamilyFitnessChallengeViewModel.class);
        viewModel.fetchSevenDayFitness(startDate, endDate).observe(this, new Observer<ResponseType>() {
            @Override
            public void onChanged(@Nullable final ResponseType status) {
                if (status == ResponseType.SUCCESS_202) {
                    Log.d("SWELL", "Fitness data fetched");
                    doPrepareProgressAnimations();
                } else {
                    Log.e("SWELL", "Fetching fitness challenge failed: " + status.toString());
                    showSystemSideErrorMessage();
                }
            }
        });
        return viewModel;
    }

    private void doPrepareProgressAnimations() {
        try {
            if (isChallengeStatusReadyForAdventure()) {
                setRunningChallengeExists();
                showProgressAnimationInstructionSnackbar();
            }
        } catch (ChallengeDoesNotExistsException e) {
            e.printStackTrace();
            Log.e("SWELL", e.getMessage());
        }
    }

    /* STATIC PUBLIC SNACKBAR METHODS */
    public void showCheckingAdventureMessage() {
        String instruction = this.getString(R.string.tooltip_snackbar_downloading_challenge);
        this.currentSnackbar = getSnackbar(instruction, getActivity());
        this.currentSnackbar.setDuration(Snackbar.LENGTH_INDEFINITE);
        this.currentSnackbar.show();
    }

    public void showNoAdventureMessage() {
        String instruction = this.getString(R.string.tooltip_snackbar_no_running_challenge);
        this.currentSnackbar = getSnackbar(instruction, getActivity());
        this.currentSnackbar.setDuration(Snackbar.LENGTH_INDEFINITE);
        this.currentSnackbar.show();
    }

    public void showNoInternetMessage() {
        String instruction = this.getString(R.string.tooltip_snackbar_adventure_no_internet);
        this.currentSnackbar = getSnackbar(instruction, getActivity());
        this.currentSnackbar.setDuration(Snackbar.LENGTH_INDEFINITE);
        this.currentSnackbar.setAction(R.string.button_adventure_refresh, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentSnackbar.dismiss();
                doFetchChallengeAndFitnessData();
            }
        });
        this.currentSnackbar.show();
    }

    public void showSystemSideErrorMessage() {
        String instruction = this.getString(R.string.tooltip_snackbar_northeastern_error);
        this.currentSnackbar = getSnackbar(instruction, getActivity());
        this.currentSnackbar.setDuration(Snackbar.LENGTH_INDEFINITE);
        this.currentSnackbar.setAction(R.string.button_contact_us, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentSnackbar.dismiss();
                // TODO Do something when there is an error on Wellness' server side
            }
        });
        this.currentSnackbar.show();
    }

    public void showProgressAnimationInstructionSnackbar() {
        String instruction = this.getActivity().getString(R.string.tooltip_see_monitoring_progress);
        this.currentSnackbar = getSnackbar(instruction, getActivity());
        this.currentSnackbar.setDuration(Snackbar.LENGTH_INDEFINITE);
        this.currentSnackbar.show();
    }

    public void showPostProgressAnimationMessage() {
        String message = this.getActivity().getString(R.string.tooltip_snackbar_progress_ongoing);
        this.currentSnackbar = getSnackbar(message, getActivity());
        this.currentSnackbar.setDuration(Snackbar.LENGTH_INDEFINITE);
        this.currentSnackbar.setAction(R.string.button_adventure_refresh, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentSnackbar.dismiss();
                resetProgressAnimation();
            }
        });
        this.currentSnackbar.show();
    }

    public void showDownloadingFitnessDataMessage() {
        String message = this.getActivity().getString(R.string.tooltip_snackbar_downloading_fitness_data);
        this.currentSnackbar = getSnackbar(message, getActivity());
        this.currentSnackbar.show();
    }

    private static Snackbar getSnackbar(String text, Activity activity) {
        Snackbar snackbar = Snackbar.make(activity.findViewById(R.id.layout_gameview), text,
                Snackbar.LENGTH_LONG);
        snackbar = setSnackBarTheme(snackbar, activity.getApplicationContext());
        return snackbar;
    }

    private static Snackbar setSnackBarTheme(Snackbar snackbar, Context context) {
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(ContextCompat.getColor(context, R.color.sea_foregroundDark));
        snackbarView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        return snackbar;
    }

    private void setFabPlayToOriginal() {
        this.fabPlay.setImageResource(R.drawable.ic_round_play_arrow_full_24px);
    }

    private void setFabPlayToRewind() {
        this.fabPlay.setImageResource(R.drawable.ic_round_replay_24px);
    }

    /* HELPER METHODS */
    private boolean isFitnessAndChallengeDataReady() {
        if (this.familyFitnessChallengeViewModel == null) {
            return false;
        } else {
            return this.isFitnessAndChallengeDataFetched();
        }
    }
    private boolean isFitnessAndChallengeDataFetched() {
        return (familyFitnessChallengeViewModel.getFetchingStatus() == ResponseType.SUCCESS_202);
    }

    private boolean isChallengeStatusReadyForAdventure() throws ChallengeDoesNotExistsException {
        if (isFitnessAndChallengeDataReady()) {
            ChallengeStatus status = this.familyFitnessChallengeViewModel.getChallengeStatus();
            return status == ChallengeStatus.UNSYNCED_RUN || status == ChallengeStatus.RUNNING;
        } else {
            return false;
        }
    }

    private static Date getStartDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2017, Calendar.JUNE, 1);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 1); // TODO UPDATE THIS to reflect the current day
        calendar.set(Calendar.MONTH, Calendar.JUNE);
        calendar.set(Calendar.YEAR, 2017);

        return calendar.getTime();
    }

    private static Date getEndDate(Date startDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.add(Calendar.DAY_OF_YEAR, 7);
        return calendar.getTime();
    }

    /* MONITORING ACTIVITY */
    private void startMonitoringActivity() {
        Intent intent = new Intent(getContext(), MonitoringActivity.class);
        getContext().startActivity(intent);
    }
}
