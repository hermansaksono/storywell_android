package edu.neu.ccs.wellness.storytelling.models;

import edu.neu.ccs.wellness.storytelling.interfaces.StoryContentInterface;
import edu.neu.ccs.wellness.storytelling.interfaces.StoryContentInterface.ContentType;
import edu.neu.ccs.wellness.storytelling.interfaces.StoryInterface;

/**
 * Created by hermansaksono on 6/13/17.
 */

public class StoryPage implements StoryContentInterface {

    private int id;
    private StoryInterface story;
    private String imgUrl;
    private String text;
    private String subText;
    private boolean isCurrent;

    // PRIVATE CONSTRUCTORS
    public StoryPage(int pageId, StoryInterface story,
                     String imgUrl, String text, String subText, boolean isCurrentPage) {
        this.id = pageId;
        this.story = story;
        this.imgUrl = imgUrl;
        this.text = text;
        this.subText = subText;
        this.isCurrent = isCurrentPage;
    }

    // PUBLIC METHODS
    @Override
    public int getId() {
        return this.id;
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
