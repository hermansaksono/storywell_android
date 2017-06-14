package edu.neu.ccs.wellness.storytelling.interfaces;

import java.util.List;

/**
 * Created by hermansaksono on 6/13/17.
 */

public interface StoryInterface {

    public int getStoryId();

    public void downloadContent();

    public List<PageInterface> getPages();

    public int getCurrentPageId();

    public void goToNextPage();

    public void goToPrevPage();

    public void goToPageById(int pageIndex);

    public void save();

}
