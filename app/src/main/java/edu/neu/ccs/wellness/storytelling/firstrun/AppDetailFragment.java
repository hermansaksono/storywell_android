package edu.neu.ccs.wellness.storytelling.firstrun;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.neu.ccs.wellness.storytelling.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AppDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AppDetailFragment extends Fragment {

    public AppDetailFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AppDetailFragment.
     */
    public static AppDetailFragment newInstance() {
        return new AppDetailFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_firstrun_appdetail, container, false);
    }

 }
