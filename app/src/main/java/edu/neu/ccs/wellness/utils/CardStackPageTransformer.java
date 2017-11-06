package edu.neu.ccs.wellness.utils;

import android.support.v4.view.ViewPager;
import android.view.View;

import edu.neu.ccs.wellness.storytelling.utils.OnGoToFragmentListener.TransitionType;

/**
 * Created by hermansaksono on 6/25/17.
 */

public class CardStackPageTransformer implements ViewPager.PageTransformer {
    private float minScale;
    private float maxScale;
    private TransitionType transitionType;
    private Integer resetOnPage = null;

    public CardStackPageTransformer(float minScale) {
        this.minScale = minScale;
        this.maxScale = 2 - minScale;
        this.transitionType = TransitionType.SLIDE_LEFT;
    }

    public void transformPage(View view, float position) {
        if (transitionType.equals(TransitionType.SLIDE_LEFT)) {
            transformPageSlide(view, position);
        } else if (transitionType.equals(TransitionType.ZOOM_OUT)) {
            transformPageZoom(view, position);
        }
    }

    public TransitionType getTransitionType() { return this.transitionType; }

    public void setTransitionType(TransitionType type) { this.transitionType = type; }

    private void transformPageSlide(View view, float position) {
        int pageWidth = view.getWidth();

        if (position < -1) { // [-Infinity,-1)
            // This page is way off-screen to the left.
            view.setAlpha(0);

        } else if (position <= 0) { // [-1,0]
            // Use the default slide transition when moving to the left page
            view.setAlpha(1);
            view.setTranslationX(0);
            view.setScaleX(1);
            view.setScaleY(1);

        } else if (position <= 1) { // (0,1]
            // Fade the page out.
            view.setAlpha(1 - position);

            // Counteract the default slide transition
            view.setTranslationX(pageWidth * -position);

            // Scale the page down (between minScale and 1)
            float scaleFactor = minScale
                    + (1 - minScale) * (1 - Math.abs(position));
            view.setScaleX(scaleFactor);
            view.setScaleY(scaleFactor);

        } else { // (1,+Infinity]
            // This page is way off-screen to the right.
            view.setAlpha(0);
        }
    }

    private void transformPageZoom(View view, float position) {
        int pageWidth = view.getWidth();

        if (position < -1) { // [-Infinity,-1)
            view.setAlpha(0);
            view.setScaleX(maxScale);
            view.setScaleY(maxScale);
            view.setTranslationX(0);

        } else if (position <= 0) { // [-1,0]
            float scaleFactor = 1 - position;
            view.setAlpha(1 + position);
            view.setTranslationX(pageWidth * -position);
            view.setScaleX(scaleFactor);
            view.setScaleY(scaleFactor);

        } else if (position <= 1) { // (0,1]
            float scaleFactor = minScale + (1 - minScale) * (1 - Math.abs(position));
            view.setAlpha(1 - position);
            view.setTranslationX(pageWidth * -position);
            view.setScaleX(scaleFactor);
            view.setScaleY(scaleFactor);

        } else { // (1,+Infinity]
            // This page is way off-screen to the right.
            view.setAlpha(0);
        }
    }
}
