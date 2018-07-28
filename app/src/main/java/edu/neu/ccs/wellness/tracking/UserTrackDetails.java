package edu.neu.ccs.wellness.tracking;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.Date;
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

    protected String eventName;
    protected String date;
    protected String timestamp;
    protected Bundle eventParameters;

    public UserTrackDetails(String eventName, Bundle eventParameters){
            this.eventName = eventName;
            this.eventParameters = eventParameters;
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
        return eventParameters;
    }

    public String getTimestamp() {
        return timestamp;
    }

//    private List<String> getEventParametersList(Map<EventParameters, String> eventParametersMap){
//        if(eventParametersMap.size() == 0) return null;
//        ArrayList<String> eventParameterReturnList = new ArrayList<>();
//        for(EventParameters eventParameter : eventParametersMap.keySet()){
//            if(eventParameter == EventParameters.CUSTOM_PARAMETER){
//                eventParameterReturnList.add(eventParametersMap.get(eventParameter));
//            }else{
//                eventParameterReturnList.add(eventParameter.toString());
//            }
//        }
//        return eventParameterReturnList;
//    }
}
