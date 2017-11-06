package edu.neu.ccs.wellness.storytelling.utils;


public interface OnGoToFragmentListener {

    enum TransitionType {
        SLIDE_LEFT, ZOOM_OUT
    }

    void onGoToFragment(TransitionType transitionType, int direction);
}
