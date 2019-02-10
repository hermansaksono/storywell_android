package edu.neu.ccs.wellness.storytelling.storyview;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import org.json.JSONException;

import java.io.IOException;

import edu.neu.ccs.wellness.fitness.challenges.UnitChallenge;
import edu.neu.ccs.wellness.fitness.challenges.ChallengeManager;
import edu.neu.ccs.wellness.fitness.interfaces.AvailableChallengesInterface;
import edu.neu.ccs.wellness.fitness.interfaces.ChallengeManagerInterface;
import edu.neu.ccs.wellness.fitness.interfaces.ChallengeStatus;
import edu.neu.ccs.wellness.storytelling.HomeActivity;
import edu.neu.ccs.wellness.storytelling.R;
import edu.neu.ccs.wellness.server.RestServer;
import edu.neu.ccs.wellness.server.RestServer.ResponseType;
import edu.neu.ccs.wellness.server.WellnessRestServer;
import edu.neu.ccs.wellness.server.WellnessUser;
import edu.neu.ccs.wellness.storytelling.Storywell;
import edu.neu.ccs.wellness.storytelling.homeview.HomeAdventurePresenter;
import edu.neu.ccs.wellness.storytelling.settings.SynchronizedSettingRepository;
import edu.neu.ccs.wellness.storytelling.utils.OnGoToFragmentListener;
import edu.neu.ccs.wellness.utils.WellnessIO;


public class ChallengePickerFragment extends Fragment {
    private static final String STORY_TEXT_FACE = "fonts/pangolin_regular.ttf";
    private ChallengeStatus challengeStatus = ChallengeStatus.UNINITIALIZED;
    private View view;
    private ViewFlipper viewFlipper;
    private ChallengeManagerInterface challengeManager;
    private OnGoToFragmentListener onGoToFragmentListener;
    private AvailableChallengesInterface groupChallenge;
    private AsyncLoadChallenges asyncLoadChallenges = new AsyncLoadChallenges();
    private AsyncPostChallenge asyncPostChallenge = new AsyncPostChallenge();

    private boolean isDemoMode;

    public ChallengePickerFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.view = inflater.inflate(
                R.layout.fragment_challenge_root_view, container, false);
        this.viewFlipper = getViewFlipper(this.view);
        this.isDemoMode = SynchronizedSettingRepository
                .getLocalInstance(this.getContext()).isDemoMode();

        // Update the text in the ChallengeInfo scene
        setChallengeInfoText(this.view, getArguments().getString("KEY_TEXT"),
                getArguments().getString("KEY_SUBTEXT"));

        // Set the OnClick event when a user clicked on the Next button in ChallengeInfo
        this.view.findViewById(R.id.info_buttonNext).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewFlipper.showNext();
            }
        });

        // Set the OnClick event when a user clicked on the Next button in ChallengePicker
        this.view.findViewById(R.id.picker_buttonNext).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewFlipper.showNext();
                doChooseSelectedChallenge();
            }
        });

        // Set the OnClick event when a user clicked on the Next button in ChallengeSummary
        this.view.findViewById(R.id.summary_buttonNext).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishActivityThenGoToAdventure();
            }
        });

        doTryExecuteAsyncLoadChallenges();

        return view;
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
    }


    // PRIVATE ASYNCTASK SUBCLASSES
    private class AsyncLoadChallenges extends AsyncTask<Void, Integer, RestServer.ResponseType> {

        protected RestServer.ResponseType doInBackground(Void... voids) {
            WellnessUser user = new WellnessUser(Storywell.DEFAULT_USER, Storywell.DEFAULT_PASS);
            WellnessRestServer server = new WellnessRestServer(Storywell.SERVER_URL, 0, Storywell.API_PATH, user);

            if (server.isOnline(getActivity()) == false) {
                return RestServer.ResponseType.NO_INTERNET;
            }

            try {
                challengeManager = ChallengeManager.create(server, getContext());
                groupChallenge = challengeManager.getAvailableChallenges();
                challengeStatus = challengeManager.getStatus();
                return RestServer.ResponseType.SUCCESS_202;
            } catch (JSONException e) {
                e.printStackTrace();
                return ResponseType.BAD_JSON;
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseType.BAD_REQUEST_400;
            }
        }

        protected void onPostExecute(RestServer.ResponseType result) {
            Log.d("WELL Challenges d/l", result.toString());
            if (result == RestServer.ResponseType.SUCCESS_202) {
                Log.d("WELL Challenges d/l", result.toString());
                updateView();
            } else if (result == RestServer.ResponseType.BAD_REQUEST_400) {
                // DO SOMETHING
            } else if (result == RestServer.ResponseType.BAD_JSON) {
                // DO SOMETHING
            } else if (result == RestServer.ResponseType.NO_INTERNET) {
                // DO SOMETHING
            } else {
                // DO SOMETHING
            }
        }

    }

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
            }
        }

    }

    private void updateView(){
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), STORY_TEXT_FACE);
        TextView textView = view.findViewById(R.id.picker_text);
        TextView subtextView = view.findViewById(R.id.picker_subtext);

        if (challengeStatus == ChallengeStatus.AVAILABLE ) {

            textView.setText(groupChallenge.getText());
            textView.setTypeface(tf);
            subtextView.setText(groupChallenge.getSubtext());
            subtextView.setTypeface(tf);

            RadioGroup radioGroup = view.findViewById(R.id.challengesRadioGroup);
            for (int i = 0; i < radioGroup.getChildCount();i ++) {
                RadioButton radioButton = (RadioButton) radioGroup.getChildAt(i);
                radioButton.setText(groupChallenge.getChallenges().get(i).getText());
                radioButton.setTypeface(tf);
            }
        }
    }

    private void doChooseSelectedChallenge() {
        RadioGroup radioGroup = view.findViewById(R.id.challengesRadioGroup);
        int radioButtonId = radioGroup.getCheckedRadioButtonId();
        if (radioButtonId >= 0) {
            RadioButton radioButton = radioGroup.findViewById(radioButtonId);
            this.doChooseThisChallengeByIndex(radioGroup.indexOfChild(radioButton));
        } else {
            Toast.makeText(getContext(), "Please pick one adventure first",
                    Toast.LENGTH_SHORT).show();
        }

    }

    private void doChooseThisChallengeByIndex(int index) {
        if (this.isDemoMode) {
            return;
        }
        try {
            AvailableChallengesInterface groupChallenge = challengeManager.getAvailableChallenges();
            UnitChallenge challenge = groupChallenge.getChallenges().get(index);
            this.challengeManager.setRunningChallenge(challenge);
            this.asyncPostChallenge.execute();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void doTryExecuteAsyncLoadChallenges() {
        if (this.asyncLoadChallenges.getStatus() == AsyncTask.Status.PENDING) {
            this.asyncLoadChallenges.execute();
        }
    }

    private void finishActivityThenGoToAdventure() {
        this.asyncLoadChallenges.cancel(true);
        //this.asyncPostChallenge.cancel(true);
        WellnessIO.getSharedPref(this.getContext()).edit()
                .putInt(HomeActivity.KEY_DEFAULT_TAB, HomeActivity.TAB_ADVENTURE)
                .apply();
        this.getActivity().finish();
    }

    private static ViewFlipper getViewFlipper(View view) {
        ViewFlipper viewFlipper = view.findViewById(R.id.view_flipper);
        viewFlipper.setInAnimation(view.getContext(), R.anim.reflection_fade_in);
        viewFlipper.setOutAnimation(view.getContext(), R.anim.reflection_fade_out);
        return viewFlipper;
    }

    /***
     * Set View to show the ChallengeInfo's content
     * @param view The View in which the content will be displayed
     * @param text The Story content's text
     */
    private void setChallengeInfoText(View view, String text, String subtext) {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), STORY_TEXT_FACE);
        TextView tv = view.findViewById(R.id.info_text);
        TextView stv = view.findViewById(R.id.info_subtext);

        tv.setTypeface(tf);
        tv.setText(text);

        stv.setTypeface(tf);
        stv.setText(subtext);
    }
}
