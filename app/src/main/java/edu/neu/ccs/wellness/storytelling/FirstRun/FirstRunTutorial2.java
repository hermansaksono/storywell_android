package edu.neu.ccs.wellness.storytelling.FirstRun;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.neu.ccs.wellness.storytelling.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FirstRunTutorial2#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FirstRunTutorial2 extends Fragment {


    public FirstRunTutorial2() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FirstRunTutorial2.
     */
    // TODO: Rename and change types and number of parameters
    public static FirstRunTutorial2 newInstance() {
        FirstRunTutorial2 fragment = new FirstRunTutorial2();
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
        return inflater.inflate(R.layout.fragment_first_run_tutorial2, container, false);

    }

}
