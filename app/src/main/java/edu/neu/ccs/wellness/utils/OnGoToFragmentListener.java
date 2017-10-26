package edu.neu.ccs.wellness.utils;

/**
 * Created by hermansaksono on 6/27/17.
 */

public interface OnGoToFragmentListener {
    enum TransitionType {
        SLIDE_LEFT, ZOOM_OUT
    }
    void onGoToFragment(TransitionType transitionType, int direction);
}
