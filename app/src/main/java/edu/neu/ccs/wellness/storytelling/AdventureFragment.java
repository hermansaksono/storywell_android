package edu.neu.ccs.wellness.storytelling;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import edu.neu.ccs.wellness.storytelling.homeview.HomeAdventurePresenter;

public class AdventureFragment extends Fragment {

    /* PRIVATE VARIABLES */
    private HomeAdventurePresenter presenter;

    /* CONSTRUCTOR */
    public AdventureFragment() {
        // Required empty public constructor
    }

    /* FACTORY METHOD */
    public static AdventureFragment newInstance() {
        return new AdventureFragment();
    }

    /* INTERFACE FUNCTIONS */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        /* Prepare the UI views */
        View rootView = inflater.inflate(R.layout.fragment_adventure, container, false);
        this.presenter = new HomeAdventurePresenter(rootView);

        // Set up GameView's OnTouch event
        rootView.findViewById(R.id.layout_monitoringView).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return presenter.processTapOnGameView(getActivity(), event);
            }
        });

        // Set up FAB for playing the animation
        rootView.findViewById(R.id.fab_action).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.onFabPlayClicked(getActivity());
            }
        });

        // Set up FAB to show the calendar
        rootView.findViewById(R.id.fab_show_calendar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.onFabShowCalendarClicked(view);
            }
        });

        // Set up FAB to hide the calendar
        rootView.findViewById(R.id.fab_seven_day_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.onFabCalendarHideClicked(view);
            }
        });

        return rootView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUserVisibleHint(false);
    }

    @Override
    public void onStart() {
        super.onStart();
        this.presenter.startGameView();
        this.presenter.tryShowCheckingAdventureMessage(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        this.presenter.tryFetchChallengeAndFitnessData(this);
        this.presenter.resumeGameView();
    }

    @Override
    public void onPause() {
        super.onPause();
        this.presenter.pauseGameView();
    }

    @Override
    public void onStop() {
        super.onStop();
        this.presenter.stopGameView();
    }

    /* MONITORING ACTIVITY */
    private void startMonitoringActivity() {
        Intent intent = new Intent(getContext(), MonitoringActivity.class);
        getContext().startActivity(intent);
    }
}
