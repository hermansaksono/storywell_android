package edu.neu.ccs.wellness.storytelling.minigame;

/**
 * Created by hermansaksono on 2/25/19.
 */

public class ResolutionInfo {

    public ResolutionInfo () {

    }

    /**
     * Determines the status of the resolution.
     * {@value ResolutionStatus#STATUS_UNSTARTED} = Game not started
     */
    private String resolutionStatus = ResolutionStatus.STATUS_UNSTARTED;


    public String getResolutionStatus() {
        return resolutionStatus;
    }

    public void setResolutionStatus(String resolutionStatus) {
        this.resolutionStatus = resolutionStatus;
    }

    /**
     * Stores the state of the Mini Game for the Resolution
     */
    private MiniGameState miniGameState = new NoState();

    public MiniGameState getMiniGameState() {
        return miniGameState;
    }

    public void setMiniGameState(MiniGameState miniGameState) {
        this.miniGameState = miniGameState;
    }
}
