package edu.neu.ccs.wellness.storytelling.homeview;

import android.content.Context;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.view.View;

import org.json.JSONException;

import java.io.IOException;

import edu.neu.ccs.wellness.fitness.interfaces.ChallengeManagerInterface;
import edu.neu.ccs.wellness.server.TokenErrorException;
import edu.neu.ccs.wellness.storytelling.R;
import edu.neu.ccs.wellness.storytelling.Storywell;

/**
 * AsyncTask to close the currently running fitness challenge
 * Created by hermansaksono on 4/7/19.
 */

public class CloseChallengeUnlockStoryAsync extends AsyncTask<Void, Void, Boolean> {

    private final Storywell storywell;
    private final OnUnlockingEvent onUnlockingEvent;
    private Snackbar progressSnackbar;
    private Snackbar failedSnackbar;

    public interface OnUnlockingEvent {
        void onClosingSuccess();
        void onClosingFailed();
    }

    public CloseChallengeUnlockStoryAsync(Context context,
                                          View snackbarTargetView,
                                          final OnUnlockingEvent onUnlockingEvent) {
        this.storywell = new Storywell(context);
        this.onUnlockingEvent = onUnlockingEvent;
        this.progressSnackbar = getProgressSnackbar(snackbarTargetView);
        this.failedSnackbar = getFailedSnackbar(snackbarTargetView);
    }

    @Override
    protected void onPreExecute() {
        this.progressSnackbar.show();
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        try {
            ChallengeManagerInterface challengeManager = storywell.getChallengeManager();
            // challengeManager.closeChallenge(); // don't need to change the local JSON
            challengeManager.syncCompletedChallenge();
            //setEverythingAsClosed(this.context);

            return true;
        } catch (TokenErrorException e) {
            // TODO handle token error. Ask user to relogin.
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean isSuccessful) {
        this.progressSnackbar.dismiss();
        if (isSuccessful) {
            onUnlockingEvent.onClosingSuccess();
        } else {
            onUnlockingEvent.onClosingFailed();
            //showUnlockingStoryFailedDialog(this.context);
            this.failedSnackbar.show();
        }
    }

    /*
    private void showUnlockingStoryFailedDialog(final Context context) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle(R.string.adventure_unlock_failed_title);
        alertDialogBuilder.setMessage(R.string.adventure_unlock_failed_msg);
        alertDialogBuilder.setPositiveButton(R.string.adventure_unlock_failed_positive_button,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        new CloseChallengeUnlockStoryAsync(context, onUnlockingEvent).execute();
                        dialog.dismiss();
                    }
                });
        alertDialogBuilder.setNegativeButton(R.string.adventure_unlock_failed_negative_button,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        alertDialogBuilder.show();
    }

    private static void setEverythingAsClosed(Context context) {
        SynchronizedSetting setting = SynchronizedSettingRepository.getLocalInstance(context);
        String storyIdToBeUnlocked = setting.getStoryChallengeInfo().getStoryId();
        String chapterIdToBeUnlocked = setting.getStoryChallengeInfo().getChapterIdToBeUnlocked();

        if (!setting.getStoryListInfo().getUnlockedStoryPages().contains(chapterIdToBeUnlocked)) {
            setting.getStoryListInfo().getUnlockedStoryPages().add(chapterIdToBeUnlocked);
        }

        if (!setting.getStoryListInfo().getUnlockedStories().contains(storyIdToBeUnlocked)) {
            setting.getStoryListInfo().getUnlockedStories().add(storyIdToBeUnlocked);
        }

        if (!setting.getStoryListInfo().getUnreadStories().contains(storyIdToBeUnlocked)) {
            setting.getStoryListInfo().getUnreadStories().add(storyIdToBeUnlocked);
        }

        if (!setting.isDemoMode()) {
            setting.resetStoryChallengeInfo();
        }

        setting.getChallengeInfo().setCurrentChallengeId("");
        setting.setResolutionInfo(new SynchronizedSetting.ResolutionInfo());

        SynchronizedSettingRepository.saveLocalAndRemoteInstance(setting, context);
    }
    */

    private Snackbar getProgressSnackbar (View view) {
        return Snackbar.make(
                view, R.string.adventure_unlock_progress_title, Snackbar.LENGTH_INDEFINITE);
    }

    private Snackbar getFailedSnackbar (final View targetView) {
        Snackbar failedSnackbar = Snackbar.make(
                targetView, R.string.adventure_unlock_failed_title, Snackbar.LENGTH_INDEFINITE);
        failedSnackbar.setAction(R.string.adventure_unlock_failed_positive_button,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new CloseChallengeUnlockStoryAsync(
                                targetView.getContext(), targetView, onUnlockingEvent).execute();
                    }
                });
        return failedSnackbar;
    }
}
