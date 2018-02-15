package edu.neu.ccs.wellness.storytelling;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.neu.ccs.wellness.storywell.interfaces.GameLevelInterface;
import edu.neu.ccs.wellness.storywell.interfaces.GameViewInterface;
import edu.neu.ccs.wellness.storywell.monitoringview.GameLevel;
import edu.neu.ccs.wellness.storywell.monitoringview.HeroSprite;

public class AdventureFragment extends Fragment {
    private GameViewInterface gameView;
    private HeroSprite hero;

    public AdventureFragment() { } // Required empty public constructor

    public static AdventureFragment newInstance() {
        return new AdventureFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_flying, container, false);


        this.hero = new HeroSprite(getResources(), R.drawable.hero_girl_base);
        GameLevelInterface gameLevel = new GameLevel(R.color.flying_sky,
                R.drawable.gameview_sea_fg_lv01,
                R.drawable.gameview_island_lv01,
                R.drawable.gameview_clouds_fg1_lv01,
                R.drawable.gameview_clouds_bg1_lv01,
                R.drawable.gameview_clouds_fg2_lv01,
                R.drawable.gameview_clouds_bg2_lv01);
        gameLevel.setHero(this.hero);

        this.gameView = rootView.findViewById(R.id.monitoringView);
        this.gameView.setLevelDesign(getResources(), gameLevel);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        this.gameView.start();
        this.hero.setToMoving(150);
    }

    @Override
    public void onPause() {
        super.onPause();
        this.gameView.stop();
    }
}
