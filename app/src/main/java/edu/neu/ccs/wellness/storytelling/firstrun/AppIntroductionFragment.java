package edu.neu.ccs.wellness.storytelling.firstrun;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.neu.ccs.wellness.storytelling.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AppIntroductionFragment#newInstance} factory method to
 * newInstance an instance of this fragment.
 */
public class AppIntroductionFragment extends Fragment {

    public AppIntroductionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to newInstance a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AppIntroductionFragment.
     */
    public static AppIntroductionFragment newInstance() {
        return new AppIntroductionFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_firstrun_appintro, container, false);
    }

 }