package edu.neu.ccs.wellness.storytelling.models;

import java.util.ArrayList;

import edu.neu.ccs.wellness.storytelling.interfaces.PageInterface;
import edu.neu.ccs.wellness.storytelling.interfaces.StoryInterface;

/**
 * Created by hermansaksono on 6/13/17.
 */

public class Story implements StoryInterface {

    public int getStoryId() {
        return 0;
    }

    public void downloadContent() {

    }

    public ArrayList<PageInterface> getPages() {
        return null;
    }

    public int getCurrentPageId() {
        return 0;
    }

    public void goToNextPage() {

    }

    public void goToPrevPage() {

    }

    public void goToPageById(int pageIndex) {

    }

    public void save() {

    }
}
