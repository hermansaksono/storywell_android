package edu.neu.ccs.wellness.tracking;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by RAJ on 7/15/2018.
 */

public class UserTrackDetails implements UserTrackingInfoInterface {


    public enum EventName{
        ACTIVITY_OPENED, FRAGMENT_OPENED, USER_LOGIN, BUTTON_CLICK, CONTENT_SLEECT, CUSTOM_EVENT
    }

    public enum EventParameters{
        ATTEMPT, SUCCESS, FAIL, STORY, HOME_ACTIVITY, STORY_LIST_FRAGMENT, ADVENTURE_FRAGMENT,
        STORY_VIEW_ACTIVITY, LOGIN_ACTIVITY, ABOUT_ACTIVITY, CUSTOM_PARAMETER
    }

    private String eventName;
    private String date;
    private String timestamp;
    private Map<String, Object> eventParameters;

    public UserTrackDetails(String eventName, Bundle eventParameters){
            this.eventName = eventName;
            this.eventParameters = getEventParamsMapFromBundle(eventParameters);
            // this.eventParameters = eventParameters;
            this.date = new Date().toString();
            this.timestamp = String.valueOf(new Date().getTime());
    }

    public String getEventName() {
        return eventName;
    }

    public String getDate() {
        return date;
    }

    public Bundle getEventParameters() {
        return getBundleFromParamsMap(eventParameters);
    }

    public String getTimestamp() {
        return timestamp;
    }

    public Map<String, Object> getEventParametersMap() { return eventParameters;}

    /* HELPER METHODS */
    private static Map<String, Object> getEventParamsMapFromBundle(Bundle eventParametersBundle) {
        Map<String, Object> eventParametersMap = new HashMap<>();
        for (String key : eventParametersBundle.keySet()) {
            eventParametersMap.put(key, eventParametersBundle.get(key));
        }
        return eventParametersMap;
    }

    private static Bundle getBundleFromParamsMap(Map<String, Object> eventParametersMap) {
        Bundle bundle = new Bundle();
        for (Map.Entry<String, Object> entry : eventParametersMap.entrySet()) {
            bundle.putString(entry.getKey(), (String) entry.getValue());
        }
        return bundle;
    }

}
