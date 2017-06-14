package edu.neu.ccs.wellness.storytelling.models;

import edu.neu.ccs.wellness.storytelling.interfaces.StoryContentInterface;

/**
 * Created by hermansaksono on 6/14/17.
 */

public class StoryReflection implements StoryContentInterface {
    private StoryPage page;

    // CONSTRUCTORS

    private StoryReflection(StoryPage page){
        this.page = page;
    }

    // FACTORY METHODS

    public StoryContentInterface create(int storyId, int pageId) {
        StoryPage page = StoryPage.create(storyId, pageId);
        return new StoryReflection(page);
    }

    // PUBLIC METHODS

    @Override
    public int getId() {
        return this.page.getId();
    }

    @Override
    public void downloadContent() {
        this.page.downloadContent();
    }

    @Override
    public ContentType getType() {
        return ContentType.REFLECTION;
    }

    @Override
    public void getImage() {
        this.page.getImage();
    }

    @Override
    public String getText() {
        return this.page.getText();
    }

    @Override
    public String getSubtext() {
        return this.page.getSubtext();
    }

    @Override
    public boolean isCurrent() {
        return this.page.isCurrent();
    }

    @Override
    public void setIsCurrent(boolean isCurrent) {
        this.page.setIsCurrent(isCurrent);
    }

    @Override
    public void respond() {

    }
}
