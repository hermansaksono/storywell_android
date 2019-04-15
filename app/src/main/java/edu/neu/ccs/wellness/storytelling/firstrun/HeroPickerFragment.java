package edu.neu.ccs.wellness.storytelling.firstrun;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import edu.neu.ccs.wellness.storytelling.R;

import static edu.neu.ccs.wellness.storytelling.monitoringview.Constants.DEFAULT_FEMALE_HERO;
import static edu.neu.ccs.wellness.storytelling.monitoringview.Constants.DEFAULT_MALE_HERO;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HeroPickerFragment#newInstance} factory method to
 * newInstance an instance of this fragment.
 */
public class HeroPickerFragment extends Fragment {
    public static final String KEY_HERO_ID = "KEY_HERO_ID";

    private OnHeroPickedListener onHeroPickedListener;

    public interface OnHeroPickedListener {
        void onHeroPicked(int heroId);
    }

    public HeroPickerFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to newInstance a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AppIntroductionFragment.
     */
    public static HeroPickerFragment newInstance() {
        return new HeroPickerFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting_choose_hero, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getView().findViewById(R.id.button_save_hero).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tryStoreHero();
            }
        });
    }

    private void tryStoreHero() {
        RadioGroup radioGroup = getActivity().findViewById(R.id.hero_options);

        int radioButtonViewId = radioGroup.getCheckedRadioButtonId();
        int heroId;

        if (radioButtonViewId != -1) {
            switch (radioButtonViewId) {
                case R.id.hero_choose_mira:
                    heroId = DEFAULT_FEMALE_HERO;
                    break;
                case R.id.hero_choose_diego:
                    heroId = DEFAULT_MALE_HERO;
                    break;
                default:
                    heroId = DEFAULT_FEMALE_HERO;
                    break;
            }
            this.onHeroPickedListener.onHeroPicked(heroId);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            this.onHeroPickedListener = (OnHeroPickedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(((Activity) context).getLocalClassName()
                    + " must implement OnHeroPickedListener");
        }
    }

 }