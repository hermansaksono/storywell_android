package edu.neu.ccs.wellness.storytelling.firstrun;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import edu.neu.ccs.wellness.storytelling.R;
import edu.neu.ccs.wellness.storytelling.utils.OnFragmentLockListener;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GooglePlayFragment#newInstance} factory method to
 * newInstance an instance of this fragment.
 */
public class GooglePlayFragment extends Fragment {

    private OnFragmentLockListener fragmentLockListener;

    public GooglePlayFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to newInstance a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment GooglePlayFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GooglePlayFragment newInstance() {
        GooglePlayFragment fragment = new GooglePlayFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_firstrun_google_play, container, false);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (isGoogleApiInstalled(getContext())) {
            view.findViewById(R.id.google_play_needed).setVisibility(View.GONE);
            view.findViewById(R.id.google_play_installed).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.google_play_needed).setVisibility(View.VISIBLE);
            view.findViewById(R.id.google_play_installed).setVisibility(View.GONE);
        }

        view.findViewById(R.id.button_install_google_play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(getActivity());
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            this.fragmentLockListener = (OnFragmentLockListener) context;
            if (!isGoogleApiInstalled(getContext())) {
                this.fragmentLockListener.lockFragmentPager();
            }
        } catch (ClassCastException e) {
            throw new ClassCastException(((Activity) context).getLocalClassName()
                    + " must implement OnFragmentLockListener");
        }
    }

    private static boolean isGoogleApiInstalled(Context context) {
        return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
                == ConnectionResult.SUCCESS;
    }

}
