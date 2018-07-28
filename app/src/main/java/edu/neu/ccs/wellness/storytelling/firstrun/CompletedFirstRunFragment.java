package edu.neu.ccs.wellness.storytelling.firstrun;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import edu.neu.ccs.wellness.storytelling.R;
import edu.neu.ccs.wellness.storytelling.Storywell;
import edu.neu.ccs.wellness.tracking.Event;
import edu.neu.ccs.wellness.tracking.Param;
import edu.neu.ccs.wellness.tracking.WellnessUserTracking;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CompletedFirstRunFragment#newInstance} factory method to
 * newInstance an instance of this fragment.
 */
public class CompletedFirstRunFragment extends Fragment {

    public interface OnFirstRunCompletedListener {
        void onFirstRunCompleted();
    }

    private OnFirstRunCompletedListener firstRunCompletedListener;

    private Bundle eventParams;
    private WellnessUserTracking wellnessUserTracking;
    private Storywell storywell;

    public CompletedFirstRunFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to newInstance a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CompletedFirstRunFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CompletedFirstRunFragment newInstance() {
        CompletedFirstRunFragment fragment = new CompletedFirstRunFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        storywell = new Storywell(getActivity().getApplicationContext());
        wellnessUserTracking = storywell.getUserTracker("108");

        eventParams = new Bundle();
        eventParams.putString(Param.FRAGMENT_NAME, "CompletedFirstRunFragment");
        wellnessUserTracking.logEvent(Event.TUTORIAL_COMPLETE, eventParams);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_firstrun_completed, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button finishFirstRun = getView().findViewById(R.id.buttonGoToHome);
        finishFirstRun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firstRunCompletedListener.onFirstRunCompleted();
            }
        });

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            this.firstRunCompletedListener = (OnFirstRunCompletedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(((Activity) context).getLocalClassName()
                    + " must implement OnFirstRunCompletedListener");
        }
    }
}
