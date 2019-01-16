package edu.neu.ccs.wellness.reflection;

import java.util.Map;

/**
 * Created by hermansaksono on 12/7/18.
 */

public class ResponsePile {
    public static final String KEY_RESPONSE_PILE = "piles";
    private static final String TO_STRING_FORMAT = "story_%s_reflection:_%s";

    // private int incarnationId;
    private int storyId;
    // private String groupId;
    private Map<String, String> piles;
    //private long timestampUpdatedOn;

    /* CONSTRUCTORS */
    public ResponsePile() {

    }

    public ResponsePile(int storyId, Map<String, String> piles) {
        this.storyId = storyId;
        this.piles = piles;
    }

    /* GETTER AND SETTER */
    public int getStoryId() {
        return storyId;
    }

    public void setStoryId(int storyId) {
        this.storyId = storyId;
    }

    public Map<String, String> getPiles() {
        return this.piles;
    }

    public void setPiles(Map<String, String> piles) {
        this.piles = piles;
    }

    /*
    public int getIncarnationId() {
        return incarnationId;
    }

    public void setIncarnationId(int incarnationId) {
        this.incarnationId = incarnationId;
    }


    public int getStoryId() {
        return storyId;
    }

    public void setStoryId(int storyId) {
        this.storyId = storyId;
    }


    public String getGroupId() { return this.groupId; }

    public void setGroupId(String groupId) { this.groupId = groupId; }

    public long getTimestampUpdatedOn() {
        return timestampUpdatedOn;
    }

    public void setTimestampUpdatedOn(long timestamp) {
        this.timestampUpdatedOn = timestamp;
    }
    */

    /* DEFAULT METHODS */
    @Override
    public String toString () {
        return String.format(TO_STRING_FORMAT, this.storyId, this.piles.toString());
    }
}
