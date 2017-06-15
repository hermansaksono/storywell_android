package edu.neu.ccs.wellness.storytelling.models;

import edu.neu.ccs.wellness.storytelling.interfaces.StoryContentInterface;

/**
 * Created by hermansaksono on 6/13/17.
 */

public class StoryPage implements StoryContentInterface {

    private int pageId;
    private boolean isCurrent;

    // PRIVATE CONSTRUCTORS
    private StoryPage(int storyId, int pageId){
        this.pageId = storyId;
    }

    // STATIC FACTORY METHODS
    public static StoryPage create(int storyId, int pageId) {
        return null;
    }

    // PUBLIC METHODS
    @Override
    public int getId() {
        return this.pageId;
    }

    @Override
    public void downloadContent() {

    }

    @Override
    public ContentType getType() {
        return ContentType.STORY;
    }

    @Override
    public void getImage() {

    }

    @Override
    public String getText() {
        return "Hello";
    }

    @Override
    public String getSubtext() {
        return "World";
    }

    @Override
    public boolean isCurrent() {
        return this.isCurrent;
    }

    @Override
    public void setIsCurrent(boolean isCurrent) {
        this.isCurrent = isCurrent;
    }

    @Override
    public void respond() {

    }
}
