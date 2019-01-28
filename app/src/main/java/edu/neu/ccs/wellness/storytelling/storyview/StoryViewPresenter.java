package edu.neu.ccs.wellness.storytelling.storyview;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import com.google.firebase.database.ValueEventListener;

import edu.neu.ccs.wellness.logging.WellnessUserLogging;
import edu.neu.ccs.wellness.reflection.ReflectionManager;
import edu.neu.ccs.wellness.server.RestServer;
import edu.neu.ccs.wellness.story.StoryChallenge;
import edu.neu.ccs.wellness.story.StoryChapterManager;
import edu.neu.ccs.wellness.story.interfaces.StoryContent;
import edu.neu.ccs.wellness.story.interfaces.StoryInterface;
import edu.neu.ccs.wellness.storytelling.Storywell;
import edu.neu.ccs.wellness.utils.WellnessIO;

/**
 * Created by hermansaksono on 1/23/19.
 */

public class StoryViewPresenter implements ReflectionFragment.ReflectionFragmentListener {
    private StoryInterface story;
    private Storywell storywell;
    private ReflectionManager reflectionManager;
    private StoryChapterManager storyChapterManager;

    private int currentPagePosition = 0;

    public StoryViewPresenter(final FragmentActivity activity, StoryInterface story) {
        this.storywell = new Storywell(activity);
        this.story = story;
        this.storyChapterManager = new StoryChapterManager(activity.getApplicationContext());
        this.reflectionManager = new ReflectionManager(
                this.storywell.getGroup().getName(),
                this.story.getId(),
                this.storywell.getReflectionIteration(),
                activity.getApplicationContext());
    }

    /* STORY DOWNLOAD METHODS */
    public RestServer.ResponseType asyncLoadStory(Context context) {
        return story.tryLoadStoryDef(context, storywell.getServer(), storywell.getGroup());
    }

    /* STORY STATE SAVINGS */
    public void doSaveStoryState(Context context) {
        SharedPreferences sharedPreferences = WellnessIO.getSharedPref(context);
        SharedPreferences.Editor putPositionInPref = sharedPreferences.edit();
        putPositionInPref.putInt("lastPagePositionSharedPref", this.currentPagePosition).apply();
        this.story.saveState(context, storywell.getGroup());
    }

    public void doRefreshStoryState(Context context) {
        SharedPreferences sharedPreferences = WellnessIO.getSharedPref(context);
        this.currentPagePosition = sharedPreferences.getInt("lastPagePositionSharedPref", 0);
    }

    /* REFLECTION DONWLOAD AND UPLOAD METHODS */
    public void loadReflectionUrls(ValueEventListener listener) {
        this.reflectionManager.getReflectionUrlsFromFirebase(listener);
    }

    public boolean uploadReflectionAudio() {
        if (this.reflectionManager.isUploadQueued()) {
            new AsyncUploadAudio().execute();
            return true;
        } else {
            return false;
        }
    }

    public class AsyncUploadAudio extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            reflectionManager.uploadReflectionAudioToFirebase();
            return null;
        }
    }

    /* REFLECTION METHODS */
    @Override
    public boolean isReflectionExists(int contentId) {
        return this.reflectionManager.isReflectionResponded(String.valueOf(contentId));
    }

    @Override
    public void doStartRecording(int contentId, String contentGroupId, String contentGroupName) {
        if (reflectionManager.getIsPlayingStatus() == true) {
            this.reflectionManager.stopPlayback();
        }

        if (reflectionManager.getIsRecordingStatus() == false) {
            this.reflectionManager.startRecording(
                    String.valueOf(contentId),
                    contentGroupId,
                    contentGroupName,
                    new MediaRecorder());
        }
    }

    @Override
    public void doStopRecording() {
        if (reflectionManager.getIsRecordingStatus() == true) {
            this.reflectionManager.stopRecording();
        }
    }

    @Override
    public void doStartPlay(int contentId, MediaPlayer.OnCompletionListener completionListener) {
        String reflectionUrl = this.reflectionManager.getRecordingURL(String.valueOf(contentId));
        if (this.reflectionManager.getIsPlayingStatus()) {
            // Don't do anything
        } else if (reflectionUrl != null) {
            this.reflectionManager.startPlayback(
                    reflectionUrl, new MediaPlayer(), completionListener);
        }
    }

    @Override
    public void doStopPlay() {
        this.reflectionManager.stopPlayback();
    }

    /* PAGE NAVIGATION METHODS */
    public void tryGoToThisPage(int position, ViewPager viewPager, StoryInterface story) {
        int allowedPosition = getAllowedPageToGo(position);
        story.getState().setCurrentPage(allowedPosition);
        viewPager.setCurrentItem(allowedPosition);
        this.currentPagePosition = allowedPosition;
    }

    private int getAllowedPageToGo(int goToPosition) {
        int preceedingPosition = goToPosition - 1;
        if (preceedingPosition < 0) {
            return goToPosition;
        } else {
            StoryContent precContent = this.story.getContentByIndex(preceedingPosition);
            if (canProceedToNextContent(precContent) == false) {
                return preceedingPosition;
            } else {
                return goToPosition;
            }
        }
    }
    private boolean canProceedToNextContent(StoryContent precContent) {
        switch (precContent.getType()) {
            case REFLECTION:
                return canProceedFromThisReflection(precContent);
            case CHALLENGE:
                return canProceedFromThisChallenge(precContent);
            default:
                return true;
        }
    }

    private boolean canProceedFromThisReflection(StoryContent precContent) {
        return this.isReflectionExists(precContent.getId());
    }

    private boolean canProceedFromThisChallenge(StoryContent precContent) {
        StoryChallenge storyChallenge = (StoryChallenge) precContent;
        return this.storyChapterManager.isThisChapterUnlocked(storyChallenge.getChallengeId());
    }

    /* LOGGING METHODS */
    public void logEvent() {
        WellnessUserLogging userLogging = new WellnessUserLogging(
                this.storywell.getGroup().getName());
        Bundle bundle = new Bundle();
        bundle.putString("STORY_ID", this.story.getId());
        userLogging.logEvent("READ_STORY", bundle);
    }
}