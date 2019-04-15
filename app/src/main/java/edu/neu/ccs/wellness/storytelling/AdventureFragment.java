package edu.neu.ccs.wellness.storytelling;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import edu.neu.ccs.wellness.storytelling.homeview.AdventurePresenter;
import edu.neu.ccs.wellness.storytelling.homeview.HomeAdventurePresenter;

import static android.app.Activity.RESULT_OK;

public class AdventureFragment extends Fragment {

    /* PRIVATE VARIABLES */
    private AdventurePresenter presenter;
    private AdventurePresenter.AdventurePresenterListener listener;

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
        final Fragment adventureFragment = this;
        View rootView = inflater.inflate(R.layout.fragment_adventure, container, false);

        /* Prepare the Presenter */
        this.presenter = new HomeAdventurePresenter(rootView);
        this.presenter.setAdventureFragmentListener(listener);
        // this.presenter = new DummyAdventurePresenter(rootView);

        // Set up GameView's OnTouch event
        rootView.findViewById(R.id.layout_monitoringView).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return presenter.onTouchOnGameView(event, getView());
            }
        });

        // Set up control button for starting animation
        rootView.findViewById(R.id.button_play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.startPerformBluetoothSync(adventureFragment);
            }
        });

        // Set up control button for playing vis animation
        /*
        rootView.findViewById(R.id.button_go).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.startProgressAnimation();
                presenter.showControlForProgressInfo(getContext());
            }
        });
        */

        // Set up control button to show first control card
        rootView.findViewById(R.id.button_refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.resetProgressAnimation();
                presenter.showControlForFirstCard(getContext());
            }
        });

        // Set up control button when sync failed
        rootView.findViewById(R.id.button_sync_failed_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.showControlForFirstCard(getContext());
            }
        });

        // Set up control button to show first prev/next card
        rootView.findViewById(R.id.button_go_prev_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // presenter.showControlForPrevNext(getContext());
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
        this.presenter.tryFetchChallengeAndFitnessData(this);
        // this.presenter.trySyncFitnessData(this);
    }

    @Override
    public void onResume() {
        super.onResume();
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
        //this.presenter.stopSyncFitnessData();
        this.presenter.stopObservingChallengeData(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            this.listener = (AdventurePresenter.AdventurePresenterListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(((Activity) context).getLocalClassName()
                    + " must implement AdventurePresenter.AdventurePresenterListener");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == HomeAdventurePresenter.REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                presenter.trySyncFitnessData(this);
            }
        }
    }

    public void updateChallengeAndFitnessData() {
        this.presenter.stopObservingChallengeData(this);
        this.presenter.tryFetchChallengeAndFitnessData(this);
    }

    /* MONITORING ACTIVITY */
    private void startMonitoringActivity() {
        Intent intent = new Intent(getContext(), MonitoringActivity.class);
        getContext().startActivity(intent);
    }
}
