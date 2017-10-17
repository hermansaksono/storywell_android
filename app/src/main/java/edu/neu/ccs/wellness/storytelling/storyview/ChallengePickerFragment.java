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

import edu.neu.ccs.wellness.storytelling.R;
import edu.neu.ccs.wellness.storytelling.interfaces.GroupChallengeInterface;
import edu.neu.ccs.wellness.storytelling.interfaces.RestServer;
import edu.neu.ccs.wellness.storytelling.models.WellnessRestServer;
import edu.neu.ccs.wellness.storytelling.models.WellnessUser;
import edu.neu.ccs.wellness.storytelling.models.challenges.GroupChallenge;
import edu.neu.ccs.wellness.utils.OnGoToFragmentListener;
import edu.neu.ccs.wellness.utils.OnGoToFragmentListener.TransitionType;

/**
 * Created by hermansaksono on 6/25/17.
 */

public class ChallengePickerFragment extends Fragment {
    private static final String STORY_TEXT_FACE = "fonts/pangolin_regular.ttf";
    private View view;
    private GroupChallenge groupChallenge;

    private OnGoToFragmentListener mOnGoToFragmentListener;

    public ChallengePickerFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.groupChallenge = new GroupChallenge();
        this.view = inflater.inflate(R.layout.fragment_challenge_picker, container, false);
        View buttonNext = view.findViewById(R.id.buttonNext);

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnGoToFragmentListener.onGoToFragment(TransitionType.ZOOM_OUT, 1);
            }
        });
        new AsyncLoadChallenges().execute();
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mOnGoToFragmentListener = (OnGoToFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(((Activity) context).getLocalClassName()
                    + " must implement OnReflectionBeginListener");
        }
    }


    private class AsyncLoadChallenges extends AsyncTask<Void, Integer, RestServer.ResponseType> {

        protected RestServer.ResponseType doInBackground(Void... voids) {
            WellnessUser user = new WellnessUser(WellnessRestServer.DEFAULT_USER,
                    WellnessRestServer.DEFAULT_PASS);
            WellnessRestServer server = new WellnessRestServer(
                    WellnessRestServer.WELLNESS_SERVER_URL, 0,
                    WellnessRestServer.STORY_API_PATH, user);
            if (server.isOnline(getContext()) == false) {
                return RestServer.ResponseType.NO_INTERNET;
            }
            else {
                return groupChallenge.loadChallenges(getContext(), server);
            }
        }

        protected void onPostExecute(RestServer.ResponseType result) {
            if (result == RestServer.ResponseType.NO_INTERNET) {
                Log.d("WELL", result.toString());
            }
            else if (result == RestServer.ResponseType.NOT_FOUND_404) {
                Log.d("WELL", result.toString());
            }
            else if (result == RestServer.ResponseType.SUCCESS_202) {
                Log.d("WELL", groupChallenge.toString());
                updateView();
            }
        }

    }

    private void updateView(){
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), STORY_TEXT_FACE);
        TextView textView = (TextView) view.findViewById(R.id.text);
        TextView subtextView = (TextView) view.findViewById(R.id.subtext);

        textView.setText(groupChallenge.getText());
        textView.setTypeface(tf);
        subtextView.setText(groupChallenge.getSubtext());
        subtextView.setTypeface(tf);

        if (groupChallenge.getStatus() == GroupChallengeInterface.ChallengeStatus.AVAILABLE ) {
            RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.challengesRadioGroup);
            for (int i = 0; i < radioGroup.getChildCount();i ++) {
                RadioButton radioButton = (RadioButton) radioGroup.getChildAt(i);
                radioButton.setText(groupChallenge.getAvailableChallenges().get(i).getText());
                radioButton.setTypeface(tf);
            }
        }
    }
}
