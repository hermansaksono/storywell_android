package edu.neu.ccs.wellness.application.state;

import android.content.Context;

import edu.neu.ccs.wellness.storytelling.Storywell;

/**
 * Created by RAJ on 3/5/2018.
 */

public class ApplicationState {

    private Context context;
    private static ApplicationState instance;
    private Storywell storywell;

    private ApplicationState(Context context){
        this.context = context;
    }

    public static ApplicationState getInstance(Context context){
        if(instance == null){
            instance = new ApplicationState(context);
        }
        return instance;
    }

    public void setStorywellInstance(Storywell storywell){
        this.storywell = storywell;
    }

    public Storywell getStorywellInstance(){
        return this.storywell;
    }

}
