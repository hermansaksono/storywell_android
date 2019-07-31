package edu.neu.ccs.wellness.storytelling;

import android.animation.ValueAnimator;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ViewAnimator;

import com.google.gson.Gson;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import edu.neu.ccs.wellness.fitness.interfaces.AvailableChallengesInterface;
import edu.neu.ccs.wellness.fitness.interfaces.ChallengeManagerInterface;
import edu.neu.ccs.wellness.fitness.interfaces.UnitChallengeInterface;
import edu.neu.ccs.wellness.reflection.TreasureItem;
import edu.neu.ccs.wellness.reflection.TreasureItemType;
import edu.neu.ccs.wellness.server.WellnessRestServer;
import edu.neu.ccs.wellness.story.CalmingReflectionSet;
import edu.neu.ccs.wellness.storytelling.homeview.ChallengeCompletedDialog;
import edu.neu.ccs.wellness.storytelling.homeview.CloseChallengeUnlockStoryAsync;
import edu.neu.ccs.wellness.storytelling.homeview.HomeAdventurePresenter;
import edu.neu.ccs.wellness.storytelling.resolutionview.BalloonRouletteState;
import edu.neu.ccs.wellness.storytelling.resolutionview.CalmingViewFragment;
import edu.neu.ccs.wellness.storytelling.resolutionview.IdeaResolutionFragment;
import edu.neu.ccs.wellness.storytelling.resolutionview.ResolutionStatus;
import edu.neu.ccs.wellness.storytelling.resolutionview.StoryUnlockListener;
import edu.neu.ccs.wellness.storytelling.settings.SynchronizedSetting;
import edu.neu.ccs.wellness.storytelling.settings.SynchronizedSettingRepository;
import edu.neu.ccs.wellness.storytelling.storyview.ChallengePickerFragment;
import edu.neu.ccs.wellness.storytelling.storyview.ReflectionFragment;
import edu.neu.ccs.wellness.storytelling.utils.OnGoToFragmentListener;
import edu.neu.ccs.wellness.storytelling.utils.ResolutionContentAdapter;
import edu.neu.ccs.wellness.storytelling.utils.StoryContentAdapter;
import edu.neu.ccs.wellness.storytelling.utils.UserLogging;
import edu.neu.ccs.wellness.storytelling.viewmodel.ChallengePickerViewModel;
import edu.neu.ccs.wellness.utils.WellnessIO;

/**
 * Created by hermansaksono on 2/27/19.
 */

public class ResolutionActivity extends AppCompatActivity implements
        View.OnClickListener,
        OnGoToFragmentListener,
        StoryUnlockListener,
        ChallengePickerFragment.ChallengePickerFragmentListener,
        ReflectionFragment.ReflectionFragmentListener {

    private static final int NUM_SECTORS = 12;
    private static final int[] SECTOR_FREQUENCIES = {2, 0, 7, 3};
    private static final Random RANDOM = new Random();

    private static final int[] SECTOR_DRAWABLES = {
            R.drawable.art_roulette_baloon_generic,
            R.drawable.art_roulette_baloon_answer,
            R.drawable.art_roulette_baloon_idea,
            R.drawable.art_roulette_baloon_pass
    };
    private static final int[] SECTOR_IMAGEVIEWS = {
            R.id.roulette_balloon_00,
            R.id.roulette_balloon_01,
            R.id.roulette_balloon_02,
            R.id.roulette_balloon_03,
            R.id.roulette_balloon_04,
            R.id.roulette_balloon_05,
            R.id.roulette_balloon_06,
            R.id.roulette_balloon_07,
            R.id.roulette_balloon_08,
            R.id.roulette_balloon_09,
            R.id.roulette_balloon_10,
            R.id.roulette_balloon_11,
    };
    private static final int ONE_ROTATION_DEGREE = 360;
    private static final int ONE_SECTOR_ANGLE = ONE_ROTATION_DEGREE / NUM_SECTORS;
    private static final int HALF_SECTOR_ANGLE = ONE_SECTOR_ANGLE / 2;
    private static final int QUARTER_SECTOR_ANGLE = ONE_SECTOR_ANGLE / 4;
    private static final int EXTRA_SPIN_DEGREE = ONE_ROTATION_DEGREE * 9;
    private static final int SPIN_DURATION_MILLIS = 7 * 1000;

    private static final int VIEW_INTRO = 0;
    private static final int VIEW_ROULETTE = 1;
    private static final int VIEW_OUTCOME_PASS = 2;
    private static final int VIEW_OUTCOME_ANSWER = 3;
    private static final int VIEW_OUTCOME_IDEA = 4;
    private static final int VIEW_OUTCOME_REGULAR = 5;
    private static final int VIEW_REFLECTION_FRAGMENT = 6;
    private static final int VIEW_CHALLENGE_PICKER_FRAGMENT = 7;


    private ViewAnimator resolutionViewAnimator;
    private ImageView rouletteArrowImg;
    private ImageView rouletteHighlightImg;
    private BalloonRouletteState rouletteState;
    private Storywell storywell;
    private SynchronizedSetting setting;
    private ChallengePickerFragment challengePickerFragment;
    private CalmingViewFragment reflectionViewFragment;
    private LiveData<AvailableChallengesInterface> groupChallengesLiveData;
    private IdeaResolutionFragment ideaResolutionFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WellnessRestServer.configureDefaultImageLoader(this);
        setContentView(R.layout.activity_resolution);

        this.storywell = new Storywell(this);
        this.setting = this.storywell.getSynchronizedSetting();

        this.resolutionViewAnimator = findViewById(R.id.resolution_view_animator);
        ViewGroup introLayout = findViewById(R.id.resolution_intro);
        ViewGroup rouletteLayout = findViewById(R.id.roulette_layout);
        this.rouletteArrowImg = findViewById(R.id.roulette_arrow);
        this.rouletteHighlightImg = findViewById(R.id.roulette_highlight);

        this.rouletteState = getResolution();
        setBaloonImageViews(rouletteLayout, this.rouletteState.getSectorIds());
        prepareViewsForResolutionOutcome(rouletteState);

        introLayout.findViewById(R.id.resolution_next_button).setOnClickListener(this);
        rouletteLayout.findViewById(R.id.spin_button).setOnClickListener(this);
        findViewById(R.id.outcome_balloon_pass_image).setOnClickListener(this);
        findViewById(R.id.outcome_balloon_answer_image).setOnClickListener(this);
        findViewById(R.id.outcome_balloon_regular_image).setOnClickListener(this);

        showScreenBasedOnResolutionStatus(this.resolutionViewAnimator, getApplicationContext());

        logBaloonRouletteState(this.rouletteState);
    }

    private BalloonRouletteState getResolution() {
        SynchronizedSetting setting = SynchronizedSettingRepository.getLocalInstance(this);

        if (setting.getResolutionInfo().getGameState() == null) {
            return new BalloonRouletteState(
                    RANDOM.nextInt(NUM_SECTORS),
                    getRandomizedSectors());
        } else {
            return setting.getResolutionInfo().getGameState();
        }
    }

    private void logBaloonRouletteState(BalloonRouletteState state) {
        String stateString = new Gson().toJson(state, BalloonRouletteState.class);
        UserLogging.logResolutionState(stateString);
    }

    /**
     * Handle click events in the ResolutionActivity.
     * @param view
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.resolution_next_button:
                doShowRouletteScreen(view);
                break;
            case R.id.spin_button:
                doSpinNeedleAndShowOutcome(view);
                break;
            case R.id.outcome_balloon_pass_image:
                showUnlockStoryDialog(view);
                break;
            case R.id.outcome_balloon_answer_image:
                doShowCalmingView(view);
                break;
            case R.id.outcome_balloon_regular_image:
                doShowChallengePicker(view);
                break;
        }
    }

    /**
     * Show the Roulette screen.
     * @param view
     */
    private void doShowRouletteScreen(View view) {
        view.setEnabled(false);
        resolutionViewAnimator.setInAnimation(this, R.anim.view_move_left_next);
        resolutionViewAnimator.setOutAnimation(this, R.anim.view_move_left_current);
        resolutionViewAnimator.showNext();
    }

    /**
     * Show a set of calming prompts. The {@link CalmingReflectionSet}. must be initialized
     * beforehand.
     * @param view
     */
    private void doShowCalmingView(View view) {
        if (this.reflectionViewFragment !=  null) {
            resolutionViewAnimator.setInAnimation(this, R.anim.view_in_static);
            resolutionViewAnimator.setOutAnimation(this, R.anim.view_out_zoom_out);
            resolutionViewAnimator.setDisplayedChild(VIEW_REFLECTION_FRAGMENT);
        }
    }

    /**
     * Show a set of calming prompts. The {@link CalmingReflectionSet}. must be initialized
     * beforehand.
     * @param view
     */
    private void doShowIdeasView(View view) {
        if (this.reflectionViewFragment !=  null) {
            resolutionViewAnimator.setInAnimation(this, R.anim.view_in_static);
            resolutionViewAnimator.setOutAnimation(this, R.anim.view_out_zoom_out);
            resolutionViewAnimator.setDisplayedChild(VIEW_OUTCOME_IDEA);
        }
    }

    /**
     * Show the challenge picker. The {@link ChallengePickerFragment} must be initialized
     * beforehand.
     * @param view
     */
    private void doShowChallengePicker(View view) {
        if (this.challengePickerFragment !=  null) {
            resolutionViewAnimator.setInAnimation(this, R.anim.view_in_static);
            resolutionViewAnimator.setOutAnimation(this, R.anim.view_out_zoom_out);
            resolutionViewAnimator.setDisplayedChild(VIEW_CHALLENGE_PICKER_FRAGMENT);
        }
    }

    /**
     * Hide the Spin button, spin the needle, then show the outcome.
     * @param view
     */
    private void doSpinNeedleAndShowOutcome(View view) {
        view.setEnabled(false);
        saveResolution(rouletteState);
        spinRoulette(rouletteArrowImg, rouletteState.getPickedSectorId());
        view.animate().alpha(0).setDuration(1000).start();
        rouletteHighlightImg.animate().alpha(1).setDuration(500).start();
    }

    private void saveResolution(BalloonRouletteState rouletteState) {
        SynchronizedSetting setting = SynchronizedSettingRepository.getLocalInstance(this);
        setting.getResolutionInfo().setResolutionStatus(ResolutionStatus.DETERMINED);
        setting.getResolutionInfo().setGameState(rouletteState);

        SynchronizedSettingRepository.saveLocalAndRemoteInstance(setting, this);
    }

    /**
     * Show the screen based on the user's previously saved states.
     * @param resolutionViewAnimator
     * @param applicationContext
     */
    private static void showScreenBasedOnResolutionStatus(
            ViewAnimator resolutionViewAnimator, Context applicationContext) {
        SynchronizedSetting setting = SynchronizedSettingRepository
                .getLocalInstance(applicationContext);
        if (setting.isDemoMode()) {
            return;
        }
        switch (setting.getResolutionInfo().getResolutionStatus()) {
            case ResolutionStatus.UNSTARTED:
                break;
            case ResolutionStatus.DETERMINED:
                resolutionViewAnimator.setDisplayedChild(VIEW_ROULETTE);
            case ResolutionStatus.EXECUTED:
                // TODO
                break;
        }
    }

    /**
     * Spin the roulette and the highlighter.
     * @param pickedSectorid
     */
    private void spinRoulette(ImageView rouletteArrowImg, final int pickedSectorid) {
        int baseDegree = (pickedSectorid * ONE_SECTOR_ANGLE);
        int shiftDegree = RANDOM.nextInt(QUARTER_SECTOR_ANGLE);
        final int totalDegree = baseDegree + shiftDegree + EXTRA_SPIN_DEGREE;
        Log.d("SWELL", "Balloon roulette sector: " + pickedSectorid);

        RotateAnimation rotateAnim = new RotateAnimation(0, totalDegree,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        rotateAnim.setDuration(SPIN_DURATION_MILLIS);
        rotateAnim.setFillAfter(true);
        rotateAnim.setInterpolator(new DecelerateInterpolator());
        rotateAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                spinHighlighter(0, totalDegree, rouletteHighlightImg);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                showOutcome(rouletteState.getPickedSectorType());
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        rouletteArrowImg.startAnimation(rotateAnim);
    }

    /**
     * Place the highlighter circle to move to the balloon being pointed by the needle.
     * @param startDegree
     * @param endDegree
     * @param highlighterView
     */
    private void spinHighlighter(int startDegree, int endDegree, final View highlighterView) {
        ValueAnimator highlightAnimatorAngle = ValueAnimator.ofInt(startDegree, endDegree);
        highlightAnimatorAngle.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams)
                        highlighterView.getLayoutParams();

                int degree = (int) animation.getAnimatedValue();
                int animatedDegree = Math.abs((degree + HALF_SECTOR_ANGLE) / ONE_SECTOR_ANGLE)
                        * ONE_SECTOR_ANGLE;
                layoutParams.circleAngle = animatedDegree;
                highlighterView.setLayoutParams(layoutParams);
                highlighterView.requestLayout();
            }
        });
        highlightAnimatorAngle.setInterpolator(new DecelerateInterpolator());
        highlightAnimatorAngle.setDuration(SPIN_DURATION_MILLIS);
        highlightAnimatorAngle.start();
    }

    /**
     * Show the balloon that the user win
     * @param sectorType
     */
    private void showOutcome(int sectorType) {
        resolutionViewAnimator.setInAnimation(this, R.anim.view_in_zoom_in_delayed);
        resolutionViewAnimator.setOutAnimation(this, R.anim.view_out_zoom_in_delayed);

        //switch (SECTOR_PASS) {
        switch (sectorType) {
            case BalloonRouletteState.SECTOR_PASS:
                resolutionViewAnimator.setDisplayedChild(VIEW_OUTCOME_PASS);
                animateBalloonOutcome(R.id.outcome_balloon_pass_image);
                break;
            case BalloonRouletteState.SECTOR_ANSWER:
                resolutionViewAnimator.setDisplayedChild(VIEW_OUTCOME_ANSWER);
                animateBalloonOutcome(R.id.outcome_balloon_answer_image);
                break;
            case BalloonRouletteState.SECTOR_IDEA:
                resolutionViewAnimator.setDisplayedChild(VIEW_OUTCOME_IDEA);
                break;
            case BalloonRouletteState.SECTOR_DEFAULT:
                resolutionViewAnimator.setDisplayedChild(VIEW_OUTCOME_REGULAR);
                animateBalloonOutcome(R.id.outcome_balloon_regular_image);
                break;
            default:
                resolutionViewAnimator.setDisplayedChild(VIEW_OUTCOME_REGULAR);
                break;
        }
    }

    private void animateBalloonOutcome(int imageViewResId) {
        ImageView ballonView = findViewById(imageViewResId);
        ballonView.animate()
                .translationY(30)
                .setInterpolator(new CycleInterpolator(100))
                .setDuration(2 * 60 * 1000)
                .start();
    }

    private void showUnlockStoryDialog(final View view) {
        final SynchronizedSetting setting = this.storywell.getSynchronizedSetting();
        if (setting.getStoryChallengeInfo().getIsSet()) {
            String title = setting.getStoryChallengeInfo().getStoryTitle();
            String coverImageUri = setting.getStoryChallengeInfo().getStoryCoverImageUri();
            AlertDialog dialog = ChallengeCompletedDialog.newInstance(title, coverImageUri, view.getContext(),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            /*
                            HomeAdventurePresenter.unlockCurrentStoryChallenge(
                                    getApplicationContext());
                            HomeAdventurePresenter.closeChallengeInfo(getApplicationContext());
                            setChallengeAsClosed();
                            setResolutionAsClosed(getApplicationContext());
                            finishActivityAndGoToStories();
                            new CloseChallengeUnlockStoryAsync().execute();
                            */
                            doCloseChallengeUnlockStory();
                            dialog.dismiss();
                        }
                    });
            dialog.show();
        }
    }

    private void doCloseChallengeUnlockStory() {
        View rootView = getWindow().getDecorView().getRootView();
        new CloseChallengeUnlockStoryAsync(getApplicationContext(), rootView,
                new CloseChallengeUnlockStoryAsync.OnUnlockingEvent(){

                    @Override
                    public void onClosingSuccess() {
                        HomeAdventurePresenter.setStoryChallengeAsClosed(getApplicationContext());
                        finishActivityAndGoToStories();
                    }

                    @Override
                    public void onClosingFailed() {
                    }
                }).execute();
    }

    private void finishActivityAndGoToStories() {
        WellnessIO.getSharedPref(this).edit()
                .putInt(HomeActivity.KEY_DEFAULT_TAB, HomeActivity.TAB_STORYBOOKS)
                .apply();
        this.finish();
    }


    /**
     * Randomize the baloons inside the roulette view group;
     * @param rouletteViewGroup
     * @param sectors
     */
    private static void setBaloonImageViews(ViewGroup rouletteViewGroup, List<Integer> sectors) {
        for (int i = 0; i < SECTOR_IMAGEVIEWS.length; i++) {
            ImageView imageView = rouletteViewGroup.findViewById(SECTOR_IMAGEVIEWS[i]);
            imageView.setImageResource(SECTOR_DRAWABLES[sectors.get(i)]);
        }
    }

    /**
     * Prepare the relevant view for the outcome of the resolution
     * @param rouletteState
     */
    private void prepareViewsForResolutionOutcome(BalloonRouletteState rouletteState) {
        switch (rouletteState.getPickedSectorType()) {
            case BalloonRouletteState.SECTOR_PASS:
                // Don't do anything
                break;
            case BalloonRouletteState.SECTOR_ANSWER:
                prepareCalmingFragment();
                break;
            case BalloonRouletteState.SECTOR_IDEA:
                prepareIdeasFragment();
                break;
            case BalloonRouletteState.SECTOR_DEFAULT:
                prepareChallengeFragment();
        }
    }

    private void prepareCalmingFragment() {
        if (findViewById(R.id.reflection_fragment_container) != null) {
            this.reflectionViewFragment = new CalmingViewFragment();

            String calmingReflectionSetId = setting.getResolutionInfo().getLastCalmingPromptSetId();
            int startingCalmingReflectionId = setting.getResolutionInfo().getLastCalmingPromptId();
            Bundle bundle = new Bundle();
            bundle.putInt(TreasureItem.KEY_TYPE, TreasureItemType.CALMING_PROMPT);
            bundle.putString(TreasureItem.KEY_PARENT_ID, calmingReflectionSetId);
            bundle.putIntegerArrayList(TreasureItem.KEY_CONTENTS,
                    getListOfContents(startingCalmingReflectionId));
            bundle.putLong(TreasureItem.KEY_LAST_UPDATE_TIMESTAMP, 0);
            bundle.putBoolean(StoryContentAdapter.KEY_CONTENT_ALLOW_EDIT, true);

            this.reflectionViewFragment.setArguments(bundle);

            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.reflection_fragment_container, this.reflectionViewFragment)
                    .commit();
        }

    }

    private ArrayList<Integer> getListOfContents(int startingCalmingReflectionId) {
        ArrayList<Integer> output = new ArrayList<>();
        for (int i = 0; i < CalmingReflectionSet.SET_LENGTH; i++) {
            output.add(i, startingCalmingReflectionId + i);
        }
        return output;
    }

    private void prepareIdeasFragment() {
        this.ideaResolutionFragment = new IdeaResolutionFragment();

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.resolution_outcome_ideas, this.ideaResolutionFragment)
                .commit();
    }

    private void prepareChallengeFragment() {
        if (findViewById(R.id.challenge_picker_container) != null) {
            this.challengePickerFragment = ResolutionContentAdapter.getChallengePickerInstance(
                    getString(R.string.resolution_challenge_text),
                    getString(R.string.resolution_challenge_subtext));
            new CloseThenLoadChallengesAsync().execute();
        }
    }

    /**
     * AsyncTask to close the currently running fitness challenge then create available challenges.
     */
    private class CloseThenLoadChallengesAsync extends AsyncTask<Void, Void, Boolean> {
        protected Boolean doInBackground(Void... voids) {
            try {
                ChallengeManagerInterface challengeManager = storywell.getChallengeManager();
                // challengeManager.closeChallenge(); // don't need to change the local JSON
                challengeManager.syncCompletedChallenge();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return false;
        }

        protected void onPostExecute(Boolean result) {
            if (result) {
                createLiveDataThenAttachChallengePicker();
            }
        }
    }

    private void createLiveDataThenAttachChallengePicker() {
        this.groupChallengesLiveData = ViewModelProviders.of(this)
                .get(ChallengePickerViewModel.class)
                .getGroupChallenges();

        this.challengePickerFragment.setGroupChallengeLiveData(groupChallengesLiveData);

        // Add the fragment to the 'fragment_container' FrameLayout
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.challenge_picker_container, this.challengePickerFragment)
                .commit();
    }

    /**
     * Implementation of {@link OnGoToFragmentListener} method to handle page swipe in
     * {@link ChallengePickerFragment} and {@link ReflectionFragment}.
     * @param transitionType
     * @param direction
     */
    @Override
    public void onGoToFragment(OnGoToFragmentListener.TransitionType transitionType, int direction) {
        if (this.challengePickerFragment != null) {
            // Don't do anything
        } else if (this.reflectionViewFragment != null) {
            this.reflectionViewFragment.onGoToFragment(transitionType, direction);
        }
    }

    private static List<Integer> getRandomizedSectors() {
        List<Integer> randoms = new ArrayList<>();

        for (int i=0; i < SECTOR_FREQUENCIES.length; i++) {
            int numSectors = SECTOR_FREQUENCIES[i];
            if (numSectors != 0) {
                int value = BalloonRouletteState.SECTOR_TYPES[i];
                List<Integer> subsequence = Collections.nCopies(numSectors, value);
                randoms.addAll(subsequence);
            }
        }
        Collections.shuffle(randoms);
        return randoms;
    }

    @Override
    public void onChallengePicked(UnitChallengeInterface unitChallenge) {
        HomeAdventurePresenter.resetResolution(getApplicationContext());
    }

    @Override
    public boolean isReflectionExists(int contentId) {
        if (this.reflectionViewFragment != null) {
            return this.reflectionViewFragment.isReflectionExists(contentId);
        } else {
            return false;
        }
    }

    @Override
    public void doStartRecording(int contentId, String contentGroupId, String contentGroupName) {
        if (this.reflectionViewFragment != null) {
            this.reflectionViewFragment
                    .doStartRecording(contentId, contentGroupId, contentGroupName);
        }
    }

    @Override
    public void doStopRecording() {
        if (this.reflectionViewFragment != null) {
            this.reflectionViewFragment.doStopRecording();
        }
    }

    @Override
    public void doStartPlay(int contentId, MediaPlayer.OnCompletionListener completionListener) {
        if (this.reflectionViewFragment != null) {
            this.reflectionViewFragment.doStartPlay(contentId, completionListener);
        }
    }

    @Override
    public void doStopPlay() {
        if (this.reflectionViewFragment != null) {
            this.reflectionViewFragment.doStopPlay();
        }
    }

    @Override
    public void unlockStory(View view) {
        this.showUnlockStoryDialog(view);
    }

}
