package edu.neu.ccs.wellness.storytelling.resolutionview;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.List;

/**
 * Created by hermansaksono on 2/25/19.
 */

@IgnoreExtraProperties
public class BalloonRouletteState {

    public static final String GAME_NAME = "BALLOON_ROULETTE";
    public static final int SECTOR_DEFAULT = 0;
    public static final int SECTOR_ANSWER = 1;
    public static final int SECTOR_IDEA = 2;
    public static final int SECTOR_PASS = 3;
    public static final int[] SECTOR_TYPES = {
            SECTOR_DEFAULT,
            SECTOR_ANSWER,
            SECTOR_IDEA,
            SECTOR_PASS
    };


    private int pickedSectorId = -1;
    private List<Integer> sectorIds;

    public BalloonRouletteState() { }

    public BalloonRouletteState(int pickedSectorId, List<Integer> sectorIds) {
        this.pickedSectorId = pickedSectorId;
        this.sectorIds = sectorIds;
    }

    public String getGameName() {
        return GAME_NAME;
    }

    public int getPickedSectorId() {
        return pickedSectorId;
    }

    public void setPickedSectorId(int pickedSectorId) {
        this.pickedSectorId = pickedSectorId;
    }

    public List<Integer> getSectorIds() {
        return sectorIds;
    }

    public void setSectorIds(List<Integer> sectorIds) {
        this.sectorIds = sectorIds;
    }

    @Exclude
    public int getPickedSectorType() {
        if (pickedSectorId == -1 || sectorIds == null) {
            return -1;
        } else {
            return SECTOR_TYPES[this.sectorIds.get(this.pickedSectorId)];
        }
    }
}
