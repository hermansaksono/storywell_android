package edu.neu.ccs.wellness.storytelling.utils;

import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;

import edu.neu.ccs.wellness.logging.Param;
import edu.neu.ccs.wellness.logging.WellnessUserLogging;
import edu.neu.ccs.wellness.story.Story;
import edu.neu.ccs.wellness.story.interfaces.StoryInterface;

/**
 * Created by hermansaksono on 3/12/19.
 */

public class UserLogging {

    public static void logStartup() {
        getLogger().logEvent("APP_STARTUP", null);
    }

    public static void logStoryView(StoryInterface story) {
        Bundle bundle = new Bundle();
        bundle.putString("STORY_ID", story.getId());
        // TODO need to log story page
        //bundle.putInt("STORY_ID", story.getState().getCurrentPage());
        getLogger().logEvent("READ_STORY", bundle);
    }

    public static void logStoryUnlocked(String storyId, String pageId) {
        Bundle bundle = new Bundle();
        bundle.putString("STORY_ID", storyId);
        bundle.putString("STORY_PAGE_ID", pageId);
        getLogger().logEvent("STORY_UNLOCKED", bundle);
    }

    public static void logButtonPlayPressed() {
        Bundle bundle = new Bundle();
        bundle.putString(Param.BUTTON_NAME, "PLAY_ANIMATION");
        getLogger().logEvent("PLAY_BUTTON_CLICK", bundle);
    }

    public static void logProgressAnimation(
            float adultProgress, float childProgress, float overallProgress) {
        Bundle bundle = new Bundle();
        bundle.putFloat("ADULT_PROGRESS", adultProgress);
        bundle.putFloat("CHILD_PROGRESS", childProgress);
        bundle.putFloat("OVERALL_PROGRESS", overallProgress);
        getLogger().logEvent("PLAY_PROGRESS_ANIMATION", bundle);
    }

    public static void logStartBleSync() {
        getLogger().logEvent("SYNC_START", null);
    }

    public static void logStopBleSync(boolean isSuccesful) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("IS_SUCCESSFUL", isSuccesful);
        getLogger().logEvent("SYNC_ENDED", bundle);
    }

    public static void logBleFailed() {
        Bundle bundle = new Bundle();
        getLogger().logEvent("SYNC_FAILED", bundle);
    }

    public static void logViewTreasure(String treasureParentId, int treasureContentId) {
        Bundle bundle = new Bundle();
        bundle.putString("STORY_ID", treasureParentId);
        bundle.putInt("REFLECTION_START_CONTENT_ID", treasureContentId);
        getLogger().logEvent("VIEW_TREASURE", bundle);
    }

    public static void logResolutionState(String rouletteState) {
        Bundle bundle = new Bundle();
        bundle.putString("ROULETTE_STATE", rouletteState);
        getLogger().logEvent("RESOLUTION_CHOSEN", bundle);
    }

    public static void logResolutionIdeaChosen(int ideaGroup, int ideaId) {
        Bundle bundle = new Bundle();
        bundle.putInt("IDEA_GROUP", ideaGroup);
        bundle.putInt("IDEA_ID", ideaId);
        getLogger().logEvent("RESOLUTION_IDEA_CHOSEN", bundle);
    }

    public static void logChallengeViewed() {
        getLogger().logEvent("CHALLENGE_VIEWED", null);
    }

    public static void logChallengePicked(String challengeJson) {
        Bundle bundle = new Bundle();
        bundle.putString("CHALLENGE_JSON", challengeJson);
        getLogger().logEvent("CHALLENGE_PICKED", bundle);
    }

    public static void logReflectionRecordButtonPressed(String storyId, int pageId) {
        Bundle bundle = new Bundle();
        bundle.putString("STORY_ID", storyId);
        bundle.putInt("PAGE_ID", pageId);
        getLogger().logEvent("REFLECTION_ANSWERING_START", bundle);
    }

    public static void logReflectioPlayButtonPressed(String storyId, int pageId) {
        Bundle bundle = new Bundle();
        bundle.putString("STORY_ID", storyId);
        bundle.putInt("PAGE_ID", pageId);
        getLogger().logEvent("REFLECTION_PLAYBACK_START", bundle);
    }

    public static void logReflectionResponded(String storyId, int pageId) {
        Bundle bundle = new Bundle();
        bundle.putString("STORY_ID", storyId);
        bundle.putInt("PAGE_ID", pageId);
        getLogger().logEvent("REFLECTION_RESPONDED", bundle);
    }

    public static void logReflectioDeleted(String storyId, int pageId) {
        Bundle bundle = new Bundle();
        bundle.putString("STORY_ID", storyId);
        bundle.putInt("PAGE_ID", pageId);
        getLogger().logEvent("REFLECTION_DELETE_ATTEMPTED", bundle);
    }

    private static WellnessUserLogging getLogger() {
        return new WellnessUserLogging(getUid());
    }

    private static String getUid() {
        return FirebaseAuth.getInstance().getUid();
    }
}
