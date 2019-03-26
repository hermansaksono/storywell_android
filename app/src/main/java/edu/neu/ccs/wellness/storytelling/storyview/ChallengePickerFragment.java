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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import org.json.JSONException;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import edu.neu.ccs.wellness.fitness.challenges.UnitChallenge;
import edu.neu.ccs.wellness.fitness.interfaces.AvailableChallengesInterface;
import edu.neu.ccs.wellness.fitness.interfaces.ChallengeManagerInterface;
import edu.neu.ccs.wellness.fitness.interfaces.ChallengeStatus;
import edu.neu.ccs.wellness.fitness.interfaces.UnitChallengeInterface;
import edu.neu.ccs.wellness.server.RestServer;
import edu.neu.ccs.wellness.storytelling.HomeActivity;
import edu.neu.ccs.wellness.storytelling.R;
import edu.neu.ccs.wellness.storytelling.Storywell;
import edu.neu.ccs.wellness.storytelling.monitoringview.Constants;
import edu.neu.ccs.wellness.storytelling.settings.SynchronizedSettingRepository;
import edu.neu.ccs.wellness.storytelling.utils.OnGoToFragmentListener;
import edu.neu.ccs.wellness.storytelling.utils.StoryContentAdapter;
import edu.neu.ccs.wellness.utils.WellnessIO;
import edu.neu.ccs.wellness.utils.WellnessStringFormatter;


public class ChallengePickerFragment extends Fragment {
    public static final int CHALLENGE_STATUS_UNSTARTED = 0;
    public static final int CHALLENGE_STATUS_RUNNING = 1;
    public static final int CHALLENGE_STATUS_OTHER_IS_RUNNING = 2;
    public static final int CHALLENGE_STATUS_COMPLETED = 3;

    private static final int CHALLENGE_PICKER_VIEW_UNSTARTED = 0;
    private static final int CHALLENGE_PICKER_VIEW_RUNNING = 5;
    private static final int CHALLENGE_PICKER_VIEW_OTHER_IS_RUNNING = 6;
    private static final int CHALLENGE_PICKER_VIEW_COMPLETED = 7;


    // INTERFACES
    public interface ChallengePickerFragmentListener {
        void onChallengePicked(UnitChallengeInterface unitChallenge);
    }

    private ChallengeStatus challengeStatus = ChallengeStatus.UNINITIALIZED;
    private View view;
    private ViewAnimator viewAnimator;
    private ChallengeManagerInterface challengeManager;
    private AvailableChallengesInterface groupChallenge;
    private UnitChallenge pickedAdultChallenge;
    private OnGoToFragmentListener onGoToFragmentListener;
    private ChallengePickerFragmentListener challengePickerFragmentListener;
    //private AsyncLoadChallenges asyncLoadChallenges = new AsyncLoadChallenges();
    private AsyncPostChallenge asyncPostChallenge = new AsyncPostChallenge();
    private int challengePickerState = CHALLENGE_STATUS_UNSTARTED;

    private boolean isDemoMode;
    private LiveData<AvailableChallengesInterface> groupChallengeLiveData;

    public ChallengePickerFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Storywell storywell = new Storywell(getContext());
        this.view = inflater.inflate(
                R.layout.fragment_challenge_root_view, container, false);
        this.viewAnimator = view.findViewById(R.id.view_flipper);;
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

        // Set the OnClick event when a user clicked on the Next button in ChallengeInfo
        this.view.findViewById(R.id.info_button_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewAnimator.showNext();
            }
        });

        // Set the OnClick event when a user clicked on the Next button in ChallengePicker
        this.view.findViewById(R.id.adult_picker_button_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isChallengeOptionSelected()) {
                    doChooseSelectedAdultChallenge();
                    viewAnimator.showNext();
                }
            }
        });

        // Set the OnClick event when a user clicked on the Next button in Challenge start
        this.view.findViewById(R.id.date_start_picker_button_next).setOnClickListener(
                new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isStartDateTimeOptionSelected()) {
                    doChooseSelectedStartDate();
                    doActivateThisChallenge();
                    viewAnimator.showNext();
                }
            }
        });

        // Set the OnClick event when a user clicked on the Next button in ChallengeSummary
        this.view.findViewById(R.id.summary_buttonNext).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishActivityThenGoToAdventure();
            }
        });

        //doTryExecuteAsyncLoadChallenges();
        this.groupChallengeLiveData.observe(this, new Observer<AvailableChallengesInterface>() {
            @Override
            public void onChanged(@Nullable AvailableChallengesInterface availableChallenges) {
                groupChallenge = availableChallenges;
                challengeStatus = ChallengeStatus.AVAILABLE;
                updateChallengePickerView(view, groupChallenge, challengeStatus);
            }
        });

        return view;
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
        }
    }

    /**
     * Update the text in the ChallengePicker.
     * @param view
     * @param groupChallenge
     * @param challengeStatus
     */
    private static void updateChallengePickerView(
            View view, AvailableChallengesInterface groupChallenge, ChallengeStatus challengeStatus){
        TextView textView = view.findViewById(R.id.adult_picker_text);
        TextView subtextView = view.findViewById(R.id.adult_picker_subtext);

        if (challengeStatus == ChallengeStatus.AVAILABLE ) {
            //textView.setText(groupChallenge.getText());
            subtextView.setText(groupChallenge.getSubtext());

            RadioGroup radioGroup = view.findViewById(R.id.adult_challenges_radio_group);
            for (int i = 0; i < radioGroup.getChildCount();i ++) {
                RadioButton radioButton = (RadioButton) radioGroup.getChildAt(i);
                radioButton.setText(groupChallenge.getChallenges().get(i).getText());
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            onGoToFragmentListener = (OnGoToFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(((Activity) context).getLocalClassName()
                    + " must implement OnGoToFragmentListener");
        }

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

    private boolean isChallengeOptionSelected() {
        RadioGroup radioGroup = view.findViewById(R.id.adult_challenges_radio_group);
        return radioGroup.getCheckedRadioButtonId() >= 0;
    }

    private boolean doChooseSelectedAdultChallenge() {
        RadioGroup radioGroup = view.findViewById(R.id.adult_challenges_radio_group);
        UnitChallenge unitChallenge = getUnitChallengeFromRadioGroup(
                radioGroup, this.groupChallenge.getChallenges(), getContext());

        if (unitChallenge != null) {
            this.pickedAdultChallenge = unitChallenge;
            return true;
        } else {
            Toast.makeText(getContext(), "Please pick one adventure first",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

    }

    private static UnitChallenge getUnitChallengeFromRadioGroup(
            RadioGroup radioGroup, List<UnitChallenge> challenges, Context context) {
        int radioButtonId = radioGroup.getCheckedRadioButtonId();
        if (radioButtonId >= 0) {
            RadioButton radioButton = radioGroup.findViewById(radioButtonId);
            int index = radioGroup.indexOfChild(radioButton);
            if (index < challenges.size()) {
                return challenges.get(index);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private boolean isStartDateTimeOptionSelected() {
        RadioGroup radioGroup = view.findViewById(R.id.challenge_start_date_radio_group);
        return radioGroup.getCheckedRadioButtonId() >= 0;
    }

    private void doChooseSelectedStartDate() {
        RadioGroup radioGroup = view.findViewById(R.id.challenge_start_date_radio_group);
        switch (radioGroup.getCheckedRadioButtonId()) {
            case R.id.start_now:
                // setChallengeToStartNow();
                // Don't do anything
                break;
            case R.id.start_tomorrow:
                setChallengeToStartTomorrow();
                break;
        }
    }

    private void setChallengeToStartTomorrow() {
        Date startDate = this.pickedAdultChallenge.getStartDate();

        Calendar startCalendar = Calendar.getInstance(Locale.US);
        startCalendar.setTimeZone(TimeZone.getDefault());
        startCalendar.setTimeInMillis(startDate.getTime());
        startCalendar.set(Calendar.HOUR_OF_DAY, 0);
        startCalendar.set(Calendar.MINUTE, 0);
        startCalendar.set(Calendar.SECOND, 0);
        startCalendar.set(Calendar.MILLISECOND, 0);
        startCalendar.add(Calendar.DATE, 1);
        startCalendar.setTimeZone(TimeZone.getTimeZone("GMT"));
        this.pickedAdultChallenge.setStartDate(startCalendar.getTime());
    }

    private void doActivateThisChallenge() {
        if (this.isDemoMode) {
            return;
        }
        try {
            this.challengeManager.setRunningChallenge(this.pickedAdultChallenge);
            this.asyncPostChallenge.execute();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Class to post the selected pickedAdultChallenge to the Wellness server.
     */
    private class AsyncPostChallenge extends AsyncTask<Void, Integer, RestServer.ResponseType> {

        protected RestServer.ResponseType doInBackground(Void... voids) {
            return challengeManager.syncRunningChallenge();
        }

        /**
         * Handle the result only. Do not update UI.
         * @param result
         */
        protected void onPostExecute(RestServer.ResponseType result) {
            if (result == RestServer.ResponseType.NO_INTERNET) {
                Log.e("SWELL", "UnitChallenge failed: " + result.toString());
            }
            else if (result == RestServer.ResponseType.NOT_FOUND_404) {
                Log.e("SWELL", "UnitChallenge failed: " + result.toString());
            }
            else if (result == RestServer.ResponseType.SUCCESS_202) {
                Log.d("SWELL", "UnitChallenge posting successful: " + result.toString());
                setTheStoryForTheChallenge();
                updateChallengeSummary();
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
            UnitChallengeInterface challenge = challengeManager.getUnsyncedOrRunningChallenge();
            String steps = WellnessStringFormatter.getFormattedSteps((int) challenge.getGoal());
            String template = getString(R.string.challenge_summary_title);
            String challengeSummary = String.format(template, steps);
            TextView summaryTextView = view.findViewById(R.id.summary_text);
            summaryTextView.setText(challengeSummary);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void finishActivityThenGoToAdventure() {
        WellnessIO.getSharedPref(this.getContext()).edit()
                .putInt(HomeActivity.KEY_DEFAULT_TAB, HomeActivity.TAB_ADVENTURE)
                .apply();
        Intent data = new Intent();

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
