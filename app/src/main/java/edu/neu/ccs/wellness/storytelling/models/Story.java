package edu.neu.ccs.wellness.storytelling.models;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import edu.neu.ccs.wellness.storytelling.interfaces.StoryContentInterface;
import edu.neu.ccs.wellness.storytelling.interfaces.StoryInterface;

/**
 * Created by hermansaksono on 6/13/17.
 */

public class Story implements StoryInterface {
    public static final String FILENAME_STORYDEF = StoryManager.DIR_STORIES.concat("story_%d");
    public static final String FILENAME_IMAGE = StoryManager.DIR_CONTENTS.concat("story_%d__page_%d.png");

    private int storyId;
    private ArrayList<StoryContentInterface> contents;
    private StoryContentInterface currentContent;
    private String lastRefreshDateTime;
    private boolean isCurrent = false;

    private Story(int storyId) {
        this.storyId = storyId;
        this.contents = null;
        this.currentContent = null;
        this.lastRefreshDateTime = null;
        this.isCurrent = false;
    }

    private Story(int storyId,
                 ArrayList<StoryContentInterface> contents, String lastRefreshDateTime) {
        this.storyId = storyId;
        this.contents = contents;
        this.currentContent = getCurrentPage(contents);
        this.lastRefreshDateTime = lastRefreshDateTime;
        this.isCurrent = false;
    }

    /***
     * Factory method to create a new StoryObject
     * If there exists one JSON file of the Story, then read and recreate the object.
     * Otherwise,
     * @param storyId the id of the Story
     * @return Story object that contains the definition
     */
    public static Story create(int storyId) {
        // TODO implement
        return new Story(storyId);
    }

    public static Story create(JSONObject jsonStory) {
        return null; //TODO
    }

    public String getRefreshDateTime() {
        return this.lastRefreshDateTime;
    }

    public int getStoryId() {
        return this.storyId;
    }

    /***
     * Download Story contents
     */
    public void refreshContent() {
        // TODO
    }

    @Override
    public boolean isContentSet() {
        return contents.size() != 0;
    }

    public List<StoryContentInterface> getContents() {
        return this.contents;
    }

    public int getCurrentPageId() {
        return this.currentContent.getId();
    }

    public boolean isCurrent() {
        return this.isCurrent;
    }

    public void setIsCurrent(boolean isCurrent) {
        this.isCurrent = isCurrent;
    }

    public void goToNextPage() {
        int newId = this.currentContent.getId() + 1;
        this.goToPageById(newId);
    }

    public void goToPrevPage() {
        int newId = this.currentContent.getId() - 1;
        this.goToPageById(newId);
    }

    public void goToPageById(int pageIndex) {
        this.currentContent = this.contents.get(pageIndex);
    }

    public void save() {

    }

    // HELPER METHODS

    private static StoryContentInterface getCurrentPage(ArrayList<StoryContentInterface> contents) {
        for (StoryContentInterface content: contents) {
            if (content.isCurrent()) {
                return content;
            }
        }
        return null;
    }
}
