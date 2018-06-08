package edu.neu.ccs.wellness.storytelling.utils;

/**
 * Created by hermansaksono on 3/11/18.
 */

public interface OnFragmentLockListener {

    void lockFragmentPager();

    void unlockFragmentPager();

    boolean isFragmentLocked();
}
