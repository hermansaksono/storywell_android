package edu.neu.ccs.wellness.storytelling.storyview;

import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import java.util.Date;
import java.util.List;
import java.util.Map;

import edu.neu.ccs.wellness.fitness.challenges.IndividualizedChallenges;
import edu.neu.ccs.wellness.fitness.challenges.IndividualizedChallengesToPost;
import edu.neu.ccs.wellness.fitness.challenges.UnitChallenge;
import edu.neu.ccs.wellness.fitness.interfaces.AvailableChallengesInterface;
import edu.neu.ccs.wellness.fitness.interfaces.ChallengeManagerInterface;
import edu.neu.ccs.wellness.fitness.interfaces.ChallengeStatus;
import edu.neu.ccs.wellness.fitness.interfaces.UnitChallengeInterface;
import edu.neu.ccs.wellness.people.Person;
import edu.neu.ccs.wellness.server.RestServer;
import edu.neu.ccs.wellness.storytelling.HomeActivity;
import edu.neu.ccs.wellness.storytelling.R;
import edu.neu.ccs.wellness.storytelling.Storywell;
import edu.neu.ccs.wellness.storytelling.monitoringview.Constants;
import edu.neu.ccs.wellness.storytelling.settings.SynchronizedSettingRepository;
import edu.neu.ccs.wellness.storytelling.utils.StoryContentAdapter;
import edu.neu.ccs.wellness.storytelling.utils.UserLogging;
import edu.neu.ccs.wellness.utils.WellnessIO;
import edu.neu.ccs.wellness.utils.WellnessStringFormatter;


public class ChallengePickerFragment extends Fragment implements View.OnClickListener {
    public static final int CHALLENGE_STATUS_UNSTARTED = 0;
    public static final int CHALLENGE_STATUS_RUNNING = 1;
    public static final int CHALLENGE_STATUS_OTHER_IS_RUNNING = 2;
    public static final int CHALLENGE_STATUS_COMPLETED = 3;
    public static final int CHALLENGE_STATUS_LOAD_ERROR = 4;

    private static final int CHALLENGE_PICKER_VIEW_UNSTARTED = 0;
    private static final int CHALLENGE_PICKER_VIEW_RUNNING = 6;
    private static final int CHALLENGE_PICKER_VIEW_OTHER_IS_RUNNING = 7;
    private static final int CHALLENGE_PICKER_VIEW_COMPLETED = 8;
    private static final int CHALLENGE_PICKER_VIEW_LOAD_ERROR = 9;

    private static final int CHALLENGE_ADULT_RADIO_GROUP = R.id.adult_challenges_radio_group;
    private static final int CHALLENGE_CHILD_RADIO_GROUP = R.id.child_challenges_radio_group;

    // INTERFACES
    public interface ChallengePickerFragmentListener {
        void onChallengePicked(UnitChallengeInterface unitChallenge);
    }


    private Person adult;
    private Person child;
    private ChallengeManagerInterface challengeManager;
    private IndividualizedChallenges availableChallenges;
    private IndividualizedChallengesToPost challengeToPost;
    private ChallengeStatus challengeStatus = ChallengeStatus.UNINITIALIZED;

    private View view;
    private ViewAnimator viewAnimator;
    private ChallengePickerFragmentListener challengePickerFragmentListener;
    private AsyncPostChallenge asyncPostChallenge;
    private int challengePickerState = CHALLENGE_STATUS_UNSTARTED;

    private boolean isDemoMode;
    private LiveData<AvailableChallengesInterface> groupChallengeLiveData;

    public ChallengePickerFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Storywell storywell = new Storywell(getContext());
        this.adult = storywell.getCaregiver();
        this.child = storywell.getChild();

        this.view = inflater.inflate(
                R.layout.fragment_challenge_root_view, container, false);
        this.viewAnimator = view.findViewById(R.id.view_flipper);

        this.isDemoMode = SynchronizedSettingRepository.getLocalInstance(getContext()).isDemoMode();

        // Challenge Manager
        this.challengeManager = storywell.getChallengeManager();

        // Get the challenge picker's status
        this.challengePickerState = getArguments()
                .getInt(StoryContentAdapter.KEY_CHALLENGE_PICKER_STATE, CHALLENGE_STATUS_UNSTARTED);
        updateChallengePickerByState(this.challengePickerState, viewAnimator, getContext());

        // Update the text in the ChallengeInfo scene
        setChallengeInfoText(this.view, getArguments().getString("KEY_TEXT"),
                getArguments().getString("KEY_SUBTEXT"));

        // Update the hero
        if (this.challengePickerState == CHALLENGE_STATUS_COMPLETED) {
            int heroCharacterId = storywell.getSynchronizedSetting().getHeroCharacterId();

            if (heroCharacterId == Constants.DEFAULT_FEMALE_HERO) {
                view.findViewById(R.id.hero_diego_imageview).setVisibility(View.GONE);
            }
            if (heroCharacterId == Constants.DEFAULT_MALE_HERO) {
                view.findViewById(R.id.hero_mira_imageview).setVisibility(View.GONE);
            }
        }

        // Assign the onClick method
        view.findViewById(R.id.info_button_next).setOnClickListener(this);
        view.findViewById(R.id.adult_picker_button_next).setOnClickListener(this);
        view.findViewById(R.id.child_picker_button_next).setOnClickListener(this);
        view.findViewById(R.id.date_start_picker_button_next).setOnClickListener(this);
        view.findViewById(R.id.summary_buttonNext).setOnClickListener(this);

        // doTryExecuteAsyncLoadChallenges();
        if (this.groupChallengeLiveData != null) {
            this.groupChallengeLiveData.observe(this,
                    new Observer<AvailableChallengesInterface>() {
                @Override
                public void onChanged(@Nullable AvailableChallengesInterface individualizedChallenges) {
                    if (isAvailableChallengesExists(individualizedChallenges)) {
                        availableChallenges = (IndividualizedChallenges) individualizedChallenges;
                        challengeToPost = availableChallenges.getPostingInstance();
                        challengeStatus = ChallengeStatus.AVAILABLE;

                        updateChallengePickerView(
                                view, availableChallenges, adult, child, challengeStatus,
                                getString(R.string.challenge_steps_title_template));
                    } else {
                        updateChallengePickerByState(
                                CHALLENGE_STATUS_LOAD_ERROR, viewAnimator, getContext());
                    }
                }
            });
        }

        return view;
    }

    public void onClick(View view) {
        switch(view.getId()) {
            // When a user clicked on the Next button in ChallengeInfo
            case R.id.info_button_next:
                viewAnimator.showNext();
                break;

            // When a user clicked on the Next button in the adult's ChallengePicker
            case R.id.adult_picker_button_next:
                if (isChallengesLoaded()) {
                    if (doChooseSelectedChallenge(adult, CHALLENGE_ADULT_RADIO_GROUP)) {
                        viewAnimator.showNext();
                    }
                }
                break;

            // When a user clicked on the Next button in the child's ChallengePicker
            case R.id.child_picker_button_next:
                if (isChallengesLoaded()) {
                    if (doChooseSelectedChallenge(child, CHALLENGE_CHILD_RADIO_GROUP)) {
                        viewAnimator.showNext();
                    }
                }
                break;

            // When a user clicked on the Next button in Challenge start date
            case R.id.date_start_picker_button_next:
                if (isStartDateTimeOptionSelected()) {
                    doChooseSelectedStartDate();
                    doActivateThisChallenge();
                    viewAnimator.showNext();
                }
                break;

            // When a user clicked on the Next button in ChallengeSummary
            case R.id.summary_buttonNext:
                finishActivityThenGoToAdventure();
                break;
        }
    }

    private static boolean isAvailableChallengesExists(AvailableChallengesInterface challenges) {
        return challenges != null && challenges instanceof IndividualizedChallenges;
    }

    /**
     * Update challenge picker's screen to show the releveant information given the
     * {@param challengePickerState}.
     * @param challengePickerState
     * @param viewAnimator
     * @param context
     */
    private static void updateChallengePickerByState(
            int challengePickerState, ViewAnimator viewAnimator, Context context) {
        switch (challengePickerState) {
            case CHALLENGE_STATUS_UNSTARTED:
                viewAnimator.setDisplayedChild(CHALLENGE_PICKER_VIEW_UNSTARTED);
                viewAnimator.setInAnimation(context, R.anim.reflection_fade_in);
                viewAnimator.setOutAnimation(context, R.anim.reflection_fade_out);
                UserLogging.logChallengeViewed();
                break;
            case CHALLENGE_STATUS_RUNNING:
                viewAnimator.setDisplayedChild(CHALLENGE_PICKER_VIEW_RUNNING);
                break;
            case CHALLENGE_STATUS_OTHER_IS_RUNNING:
                viewAnimator.setDisplayedChild(CHALLENGE_PICKER_VIEW_OTHER_IS_RUNNING);
                break;
            case CHALLENGE_STATUS_COMPLETED:
                viewAnimator.setDisplayedChild(CHALLENGE_PICKER_VIEW_COMPLETED);
                break;
            case CHALLENGE_STATUS_LOAD_ERROR:
                viewAnimator.setDisplayedChild(CHALLENGE_PICKER_VIEW_LOAD_ERROR);
                break;
        }
    }

    /**
     * Update the text in the ChallengePicker.
     */
    private static void updateChallengePickerView(
            View view, IndividualizedChallenges individualizedChallenges,
            Person adult, Person child,
            ChallengeStatus status, String format){
        if (status == ChallengeStatus.AVAILABLE ) {
            Map<String, List<UnitChallenge>> challengesByPerson =
                    individualizedChallenges.getChallengesByPerson();
            List<UnitChallenge> adultChallenges = challengesByPerson.get(
                    String.valueOf(adult.getId()));
            List<UnitChallenge> childChallenges = challengesByPerson.get(
                    String.valueOf(child.getId()));

            TextView adultTextView = view.findViewById(R.id.adult_picker_text);
            TextView adultSubtextView = view.findViewById(R.id.adult_picker_subtext);
            TextView childTextView = view.findViewById(R.id.child_picker_text);
            TextView childSubtextView = view.findViewById(R.id.child_picker_subtext);

            String adultText = String.format(format, adult.getName());
            String childText = String.format(format, child.getName());

            String challengeOptionTempl = view.getResources().getString(R.string.challenge_item);

            adultTextView.setText(adultText);
            // adultSubtextView.setText(individualizedChallenges.getSubtext());
            Integer adultStepsLastWeek = individualizedChallenges.getStepsAverage().get(adult);
            if (adultStepsLastWeek != null) {
                String stepsString = WellnessStringFormatter.getFormattedSteps(
                        adultStepsLastWeek.intValue());
                String adultStepsGuideline = view.getResources().getString(
                        R.string.challenge_guideline, stepsString);
                adultSubtextView.setText(adultStepsGuideline);
            }

            RadioGroup adultRadioGroup = view.findViewById(R.id.adult_challenges_radio_group);
            for (int i = 0; i < adultRadioGroup.getChildCount(); i ++) {
                RadioButton radioButton = (RadioButton) adultRadioGroup.getChildAt(i);
                int steps = Math.round(adultChallenges.get(i).getGoal());
                String stepsString = WellnessStringFormatter.getFormattedSteps(steps);
                String itemString = String.format(challengeOptionTempl, stepsString);
                radioButton.setText(itemString);
            }

            childTextView.setText(childText);
            // childSubtextView.setText(individualizedChallenges.getSubtext());
            Integer childStepsLastWeek = individualizedChallenges.getStepsAverage().get(child);
            if (childStepsLastWeek != null) {
                String stepsString = WellnessStringFormatter.getFormattedSteps(
                        childStepsLastWeek.intValue());
                String childStepsGuideline = view.getResources().getString(
                        R.string.challenge_guideline, stepsString);
                childSubtextView.setText(childStepsGuideline);
            }

            RadioGroup childRadioGroup = view.findViewById(R.id.child_challenges_radio_group);
            for (int i = 0; i < childRadioGroup.getChildCount(); i ++) {
                RadioButton radioButton = (RadioButton) childRadioGroup.getChildAt(i);
                int steps = Math.round(childChallenges.get(i).getGoal());
                String stepsString = WellnessStringFormatter.getFormattedSteps(steps);
                String itemString = String.format(challengeOptionTempl, stepsString);
                radioButton.setText(itemString);
            }

        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            challengePickerFragmentListener = (ChallengePickerFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(((Activity) context).getLocalClassName()
                    + " must implement ChallengePickerFragmentListener");
        }
    }

    public void setGroupChallengeLiveData(LiveData<AvailableChallengesInterface> groupChallengeLiveData) {
        this.groupChallengeLiveData = groupChallengeLiveData;
    }

    private boolean isChallengesLoaded() {
        return this.availableChallenges != null;
    }

    private boolean doChooseSelectedChallenge(Person person, int radioGroupId) {
        RadioGroup radioGroup = view.findViewById(radioGroupId);
        String personId = String.valueOf(person.getId());
        int radioButtonId = radioGroup.getCheckedRadioButtonId();
        if (radioButtonId >= 0) {
            RadioButton radioButton = radioGroup.findViewById(radioButtonId);
            int index = radioGroup.indexOfChild(radioButton);
            this.challengeToPost.put(
                    person.getId(),
                    this.availableChallenges.getChallengesByPerson().get(personId).get(index));
            return true;
        } else {
            showPickChallengeFirstToast();
            return false;
        }
    }

    private void showPickChallengeFirstToast() {
        int actionBarHeight = getResources().getDimensionPixelOffset(R.dimen.actionbar_size) +
                getResources().getDimensionPixelOffset(R.dimen.actionbar_small_padding);
        Toast toast = Toast.makeText(getContext(), "Please pick one challenge first",
                Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL,
                0, actionBarHeight);
        toast.show();
    }

    private boolean isStartDateTimeOptionSelected() {
        RadioGroup radioGroup = view.findViewById(R.id.challenge_start_date_radio_group);
        return radioGroup.getCheckedRadioButtonId() != -1;
    }

    private void doChooseSelectedStartDate() {
        RadioGroup radioGroup = view.findViewById(R.id.challenge_start_date_radio_group);
        switch (radioGroup.getCheckedRadioButtonId()) {
            case R.id.start_now:
                // setChallengeToStartNow();
                // Don't do anything
                break;
            case R.id.start_tomorrow:
                Date startDate = this.challengeToPost.getStartDateUtc();
                this.challengeToPost.setChallengeToStartTomorrow(startDate);
                break;
        }
    }

    private void doActivateThisChallenge() {
        if (this.isDemoMode) {
            return;
        }

        this.updateChallengeSummary();
        this.asyncPostChallenge = new AsyncPostChallenge(this.challengeToPost);
        this.asyncPostChallenge.execute();
    }

    /**
     * Class to post the selected challenge to the Wellness server.
     */
    private class AsyncPostChallenge extends AsyncTask<Void, Integer, RestServer.ResponseType> {

        IndividualizedChallengesToPost challengesToPost;

        AsyncPostChallenge(IndividualizedChallengesToPost challengesToPost) {
            this.challengesToPost = challengesToPost;
        }

        protected RestServer.ResponseType doInBackground(Void... voids) {
            // return challengeManager.syncRunningChallenge();
            return challengeManager.postIndividualizedChallenge(this.challengesToPost);
        }

        /**
         * Handle the result only. Do not update UI.
         * @param result
         */
        protected void onPostExecute(RestServer.ResponseType result) {
            switch (result) {
                case NO_INTERNET:
                    Log.e("SWELL", "UnitChallenge failed: " + result.toString());
                    break;
                case NOT_FOUND_404:
                    Log.e("SWELL", "UnitChallenge failed: " + result.toString());
                    break;
                case SUCCESS_202:
                    Log.d("SWELL", "UnitChallenge posting successful: "
                            + result.toString());

                    UserLogging.logChallengePicked(this.challengesToPost.getJsonString());
                    setTheStoryForTheChallenge();
                    viewAnimator.showNext();
            }
        }

        private void setTheStoryForTheChallenge() {
            try {
                UnitChallengeInterface challenge = challengeManager.getUnsyncedOrRunningChallenge();
                challengePickerFragmentListener.onChallengePicked(challenge);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void updateChallengeSummary() {
        try {
            UnitChallenge adultChallenge = challengeToPost.get(adult.getId());
            UnitChallenge childChallenge = challengeToPost.get(child.getId());
            int adultGoal = (int) adultChallenge.getGoal();
            int childGoal = (int) childChallenge.getGoal();
            String adultGoalString = WellnessStringFormatter.getFormattedSteps(adultGoal);
            String childGoalString = WellnessStringFormatter.getFormattedSteps(childGoal);

            String template = getString(R.string.challenge_summary_person);

            String adultText = String.format(template, adult.getName(), adultGoalString);
            String childText = String.format(template, child.getName(), childGoalString);

            TextView adultSummaryTextView = view.findViewById(R.id.adult_goal);
            TextView childSummaryTextView = view.findViewById(R.id.child_goal);

            adultSummaryTextView.setText(adultText);
            childSummaryTextView.setText(childText);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void finishActivityThenGoToAdventure() {
        WellnessIO.getSharedPref(this.getContext()).edit()
                .putInt(HomeActivity.KEY_DEFAULT_TAB, HomeActivity.TAB_ADVENTURE)
                .apply();
        Intent data = new Intent();

        data.putExtra(HomeActivity.RESULT_CODE, HomeActivity.RESULT_CHALLENGE_PICKED);

        getActivity().setResult(Activity.RESULT_OK, data);
        getActivity().finish();
    }

    /***
     * Set View to show the ChallengeInfo's content
     * @param view The View in which the content will be displayed
     * @param text The Story content's text
     */
    private void setChallengeInfoText(View view, String text, String subtext) {
        TextView tv = view.findViewById(R.id.info_text);
        TextView stv = view.findViewById(R.id.info_subtext);

        tv.setText(text);

        stv.setText(subtext);
    }
}
