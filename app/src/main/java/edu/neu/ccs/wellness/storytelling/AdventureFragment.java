package edu.neu.ccs.wellness.storytelling;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
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

import org.json.JSONException;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import edu.neu.ccs.wellness.fitness.FitnessDataDoesNotExistException;
import edu.neu.ccs.wellness.fitness.challenges.ChallengeProgressCalculator;
import edu.neu.ccs.wellness.fitness.interfaces.ChallengeManagerInterface;
import edu.neu.ccs.wellness.fitness.interfaces.ChallengeStatus;
import edu.neu.ccs.wellness.fitness.interfaces.GroupFitnessInterface;
import edu.neu.ccs.wellness.fitness.interfaces.UnitChallengeInterface;
import edu.neu.ccs.wellness.people.Group;
import edu.neu.ccs.wellness.people.Person;
import edu.neu.ccs.wellness.people.PersonDoesNotExistException;
import edu.neu.ccs.wellness.server.RestServer;
import edu.neu.ccs.wellness.server.RestServer.ResponseType;
import edu.neu.ccs.wellness.storytelling.homeview.SevenDayFitnessViewModel;
import edu.neu.ccs.wellness.storywell.interfaces.GameLevelInterface;
import edu.neu.ccs.wellness.storywell.interfaces.GameMonitoringControllerInterface;
import edu.neu.ccs.wellness.storywell.interfaces.OnAnimationCompletedListener;
import edu.neu.ccs.wellness.storywell.monitoringview.HeroSprite;
import edu.neu.ccs.wellness.storywell.monitoringview.MonitoringController;
import edu.neu.ccs.wellness.storywell.monitoringview.MonitoringView;

public class AdventureFragment extends Fragment {

    /* PRIVATE VARIABLES */
    private Storywell storywell;
    private ChallengeManagerInterface challengeManager;
    private SevenDayFitnessViewModel sevenDayFitnessViewModel;
    private GroupFitnessInterface groupFitness = null;
    private UnitChallengeInterface unitChallenge;
    private ChallengeStatus challengeStatus = ChallengeStatus.UNINITIALIZED;
    private Group group;
    private Person adult = null;
    private Person child = null;
    private float adultProgress = 0.0f;
    private float childProgress = 0.0f;
    private float overallProgress = 0.0f;
    private Date today;
    private Date startDate;
    private Date endDate;

    private ChallengeProgressCalculator calculator;
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
        this.storywell = new Storywell(this.getActivity());
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
                tryStartProgressAnimation();
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
        showCheckingAdventureMessage();
    }

    @Override
    public void onResume() {
        super.onResume();
        this.monitoringController.start();

        if (isFitnessAndChallengeDataDownloaded() == false){
            Log.d("SWELL", "Fitness data not downloaded");
            doFetchFitnessAndChallengeData();
            showDownloadingFitnessDataMessage();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        this.monitoringController.stop();
        if (this.currentSnackbar != null) {
            this.currentSnackbar.dismiss();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    /* SCREEN AND ANIMATIONS */
    private void processTap(MotionEvent event) {
        if (isProgressAnimationsAllowed()) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (gameView.isOverHero(event)) {
                    this.tryStartProgressAnimation();
                }
            }
        }
    }

    private void tryStartProgressAnimation() {
        try {
            this.calculator = new ChallengeProgressCalculator(this.unitChallenge, this.groupFitness);
            this.adultProgress = calculator.getPersonProgressByDate(adult, today);
            this.childProgress = calculator.getPersonProgressByDate(child, today);
            this.overallProgress = calculator.getGroupProgressByDate(today);
            this.currentSnackbar.dismiss();
            startProgressAnimation();
        } catch (PersonDoesNotExistException e) {
            e.printStackTrace();
        } catch (FitnessDataDoesNotExistException e) {
            e.printStackTrace();
        }
    }

    private void startProgressAnimation() {
        this.monitoringController.setProgress(adultProgress, childProgress, overallProgress,
                new OnAnimationCompletedListener() {
                    @Override
                    public void onAnimationCompleted() {
                        showPostAnimationMessage();
                    }
                });
    }

    private void setRunningChallengeExists() {
        this.fabPlay.show();
    }

    private void setNoRunningChallenge() {
        this.fabPlay.hide();
    }

    private void doFetchFitnessAndChallengeData() {
        new DownloadChallengeAsync().execute();
    }

    /* ASYNCTASK For CHALLENGES, GROUP, AND FITNESS*/
    private class DownloadChallengeAsync extends AsyncTask<Void, Integer, RestServer.ResponseType> {
        protected RestServer.ResponseType doInBackground(Void... voids) {
            if (storywell.isServerOnline() == false) {
                return ResponseType.NO_INTERNET;
            }

            try {
                challengeManager = storywell.getChallengeManager();
                challengeStatus = challengeManager.getStatus();
                if (isChallengeStatusReadyForAdventure(challengeStatus)) {
                    unitChallenge = getUnitChallenge(challengeManager);
                }
                return ResponseType.SUCCESS_202;
            } catch (JSONException e) {
                e.printStackTrace();
                return ResponseType.BAD_JSON;
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseType.BAD_REQUEST_400;
            }
        }

        protected void onPostExecute(RestServer.ResponseType result) {
            Log.d("SWELL", "Challenge download status: " + result.toString());
            if (result == RestServer.ResponseType.SUCCESS_202) {
                if (isChallengeStatusReadyForAdventure(challengeStatus) == false){
                    showNoAdventureMessage();
                }
                new DownloadGroupAsync().execute();
            } else if (result == ResponseType.NO_INTERNET) {
                showNoInternetMessage();
            } else {
                showSystemSideErrorMessage();
            }
        }
    }

    private class DownloadGroupAsync extends AsyncTask<Void, Integer, RestServer.ResponseType> {
        protected RestServer.ResponseType doInBackground(Void... voids) {
            if (storywell.isServerOnline()) {
                group = storywell.getGroup();
                for (Person person : group.getMembers()) {
                    if (person.isRole(Person.ROLE_PARENT)) {
                        adult = person;
                    } else if (person.isRole(Person.ROLE_CHILD)) {
                        child = person;
                    }
                }
                return RestServer.ResponseType.SUCCESS_202;
            } else {
                return RestServer.ResponseType.NO_INTERNET;
            }
        }

        protected void onPostExecute(RestServer.ResponseType result) {
            Log.d("SWELL", "Group download status: " + result.toString());
            if (result == RestServer.ResponseType.SUCCESS_202) {
                sevenDayFitnessViewModel = getSevenDayGroupFitnessViewModel();
            } else if (result == RestServer.ResponseType.NO_INTERNET) {
                showNoInternetMessage();
            } else {
                showSystemSideErrorMessage();
            }
        }
    }

    private UnitChallengeInterface getUnitChallenge(ChallengeManagerInterface challengeManager)
            throws IOException, JSONException {
        if (challengeManager.getStatus() == ChallengeStatus.UNSYNCED_RUN) {
            return challengeManager.getUnsyncedChallenge();
        } else if (challengeManager.getStatus() == ChallengeStatus.RUNNING) {
            return challengeManager.getRunningChallenge();
        } else {
            return null;
        }
    }

    private SevenDayFitnessViewModel getSevenDayGroupFitnessViewModel() {
        Log.d("SWELL", "Downloading seven-day fitness data on " + startDate.toString());
        SevenDayFitnessViewModel viewModel;
        viewModel = ViewModelProviders.of(this).get(SevenDayFitnessViewModel.class);
        viewModel.fetchSevenDayFitness(startDate, endDate).observe(this, new Observer<ResponseType>() {
            @Override
            public void onChanged(@Nullable final ResponseType status) {
                if (status == ResponseType.SUCCESS_202) {
                    Log.d("SWELL", "Fitness data downloaded");
                    doPrepareProgressAnimations();
                } else {
                    Log.e("SWELL", "Fetching fitness data failed: " + status.toString());
                    showSystemSideErrorMessage();
                }
            }
        });
        return viewModel;
    }

    private void doPrepareProgressAnimations() {
        try {
            if (isChallengeStatusReadyForAdventure(challengeStatus)) {
                groupFitness = sevenDayFitnessViewModel.getSevenDayFitness();
                setRunningChallengeExists();
                showPreAdventureRefreshSnackbar();
            }
        } catch (FitnessDataDoesNotExistException e) {
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
                doFetchFitnessAndChallengeData();
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
                // TODO Do something when there is an error on Northeastern's side
            }
        });
        this.currentSnackbar.show();
    }

    public void showPreAdventureRefreshSnackbar() {
        String instruction = this.getActivity().getString(R.string.tooltip_see_monitoring_progress);
        this.currentSnackbar = getSnackbar(instruction, getActivity());
        this.currentSnackbar.setDuration(Snackbar.LENGTH_INDEFINITE);
        this.currentSnackbar.show();
    }

    public void showPostAnimationMessage() {
        final Snackbar snackbar = getPostAdventureRefreshSnackbar(getActivity());
        snackbar.show();
    }

    public void showDownloadingFitnessDataMessage() {
        final Snackbar snackbar = getWaitingForDownloadSnackbar(getActivity());
        snackbar.show();
    }

    public static Snackbar getPreAdventureRefreshSnackbar(Activity activity) {
        String instruction = activity.getString(R.string.tooltip_see_monitoring_progress);
        return getSnackbar(instruction, activity);
    }

    public static Snackbar getPostAdventureRefreshSnackbar(Activity activity) {
        String message = activity.getString(R.string.tooltip_snackbar_progress_ongoing);
        return getSnackbar(message, activity).setDuration(Snackbar.LENGTH_INDEFINITE);
    }

    public static Snackbar getWaitingForDownloadSnackbar(Activity activity) {
        String message = activity.getString(R.string.tooltip_snackbar_downloading_fitness_data);
        return getSnackbar(message, activity).setDuration(Snackbar.LENGTH_INDEFINITE);
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

    /* HELPER METHODS */
    private boolean isProgressAnimationsAllowed() {
        return (this.challengeManager != null) && (this.groupFitness != null);
    }

    private boolean isChallengeStatusReadyForAdventure(ChallengeStatus status) {
        return  status == ChallengeStatus.UNSYNCED_RUN || status == ChallengeStatus.RUNNING;
    }

    private boolean isFitnessAndChallengeDataDownloaded() {
        return this.groupFitness != null && this.unitChallenge != null;
    }

    private boolean isAdultAndChildSet() {
        return this.adult != null && this.child != null;
    }

    private static Date getStartDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2017, Calendar.JUNE, 1);
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
