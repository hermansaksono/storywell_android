package edu.neu.ccs.wellness;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import edu.neu.ccs.wellness.storytelling.R;

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
