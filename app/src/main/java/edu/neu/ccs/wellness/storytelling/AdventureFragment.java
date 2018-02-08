package edu.neu.ccs.wellness.storytelling;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AdventureFragment extends Fragment {

    public AdventureFragment() {
        // Required empty public constructor
    }

    public static AdventureFragment newInstance() {
        return new AdventureFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_flying, container, false);
        return rootView;
    }
}
