package edu.neu.ccs.wellness.storytelling.firstrun;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.neu.ccs.wellness.storytelling.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FirstRunTutorial1#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FirstRunTutorial1 extends Fragment {

    public FirstRunTutorial1() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FirstRunTutorial1.
     */
    public static FirstRunTutorial1 newInstance() {
        return new FirstRunTutorial1();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first_run_tutorial1, container, false);
    }

 }
