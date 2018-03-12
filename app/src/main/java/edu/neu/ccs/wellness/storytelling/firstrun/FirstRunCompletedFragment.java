package edu.neu.ccs.wellness.storytelling.firstrun;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import edu.neu.ccs.wellness.storytelling.HomeActivity;
import edu.neu.ccs.wellness.storytelling.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FirstRunCompletedFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FirstRunCompletedFragment extends Fragment {

    public interface FirstRunCompletedListener {
        void onFirstRunCompleted();
    }

    private FirstRunCompletedListener firstRunCompletedListener;

    public FirstRunCompletedFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FirstRunCompletedFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FirstRunCompletedFragment newInstance() {
        FirstRunCompletedFragment fragment = new FirstRunCompletedFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
            this.firstRunCompletedListener = (FirstRunCompletedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(((Activity) context).getLocalClassName()
                    + " must implement FirstRunCompletedListener");
        }
    }
}
