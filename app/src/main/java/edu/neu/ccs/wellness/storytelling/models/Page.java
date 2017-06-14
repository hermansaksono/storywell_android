package edu.neu.ccs.wellness.storytelling.models;

import edu.neu.ccs.wellness.storytelling.interfaces.PageInterface;

/**
 * Created by hermansaksono on 6/13/17.
 */

public class Page implements PageInterface {

    public int getPageStoryId() {
        return 0;
    }

    public void downloadContent() {

    }

    public PageType getType() {
        return PageType.STORY;
    }

    public void getImage() {

    }

    public String getText() {
        return "Hello";
    }

    public String getSubtext() {
        return "World";
    }

    public void respond() {

    }
}
