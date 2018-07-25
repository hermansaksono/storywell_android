package edu.neu.ccs.wellness.trackers.miband2;

public interface ActionCallback {
    public void onSuccess(Object data);

    public void onFail(int errorCode, String msg);
}
