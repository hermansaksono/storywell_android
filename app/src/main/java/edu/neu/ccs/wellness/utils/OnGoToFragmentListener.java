package edu.neu.ccs.wellness.utils;


public interface OnGoToFragmentListener {
    public enum TransitionType {
        SLIDE_LEFT, ZOOM_OUT;
    }
    public void onGoToFragment(TransitionType transitionType, int direction);
}
