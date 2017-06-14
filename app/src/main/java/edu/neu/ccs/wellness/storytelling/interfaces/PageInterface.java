package edu.neu.ccs.wellness.storytelling.interfaces;

/**
 * Created by hermansaksono on 6/13/17.
 */

public interface PageInterface {

    public enum PageType {
        STORY, REFLECTION, OTHER;
    }

    public int getPageStoryId();

    public void downloadContent();

    public PageType getType();

    public void getImage();

    public String getText();

    public String getSubtext();

    public void respond();
}
