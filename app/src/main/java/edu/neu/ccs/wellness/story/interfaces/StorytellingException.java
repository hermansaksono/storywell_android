package edu.neu.ccs.wellness.story.interfaces;

/**
 * Created by hermansaksono on 6/16/17.
 */

public class StorytellingException extends Exception {
    public StorytellingException() {}
    public StorytellingException(String msg) { super(msg); }
    public StorytellingException(Throwable cause) { super(cause); }
    public StorytellingException(String msg, Throwable cause) { super(msg, cause); }
}
