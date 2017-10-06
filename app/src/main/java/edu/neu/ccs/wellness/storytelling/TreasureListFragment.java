package edu.neu.ccs.wellness.storytelling;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import edu.neu.ccs.wellness.storytelling.interfaces.StorytellingException;
import edu.neu.ccs.wellness.storytelling.models.StoryManager;
import edu.neu.ccs.wellness.storytelling.models.WellnessRestServer;
import edu.neu.ccs.wellness.storytelling.models.WellnessUser;

/**
 * Created by hermansaksono on 6/14/17.
 */

public class TreasureListFragment extends Fragment {

    public static TreasureListFragment newInstance(){
        return new TreasureListFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_treasure_list, container, false);

        return rootView;
    }

    // PRIVATE METHODS

    // PRIVATE ASYNCTASK CLASSES
}