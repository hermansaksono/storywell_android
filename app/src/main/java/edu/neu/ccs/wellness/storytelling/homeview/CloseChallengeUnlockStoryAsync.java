package edu.neu.ccs.wellness.storytelling.homeview;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;

import org.json.JSONException;

import java.io.IOException;

import edu.neu.ccs.wellness.fitness.interfaces.ChallengeManagerInterface;
import edu.neu.ccs.wellness.storytelling.R;
import edu.neu.ccs.wellness.storytelling.Storywell;
import edu.neu.ccs.wellness.storytelling.settings.SynchronizedSetting;
import edu.neu.ccs.wellness.storytelling.settings.SynchronizedSettingRepository;

/**
 * AsyncTask to close the currently running fitness challenge
 * Created by hermansaksono on 4/7/19.
 */
public class CloseChallengeUnlockStoryAsync extends AsyncTask<Void, Void, Boolean> {

    private final Storywell storywell;
    private final OnUnlockingEvent onUnlockingEvent;
    private ProgressDialog progressDialog;
    private final Context context;

    public interface OnUnlockingEvent {
        void onUnlockingSuccess();
        void onUnlockingFailed();
    }

    public CloseChallengeUnlockStoryAsync(Context context, OnUnlockingEvent onUnlockingEvent) {
        this.context = context;
        this.storywell = new Storywell(this.context);
        this.onUnlockingEvent = onUnlockingEvent;
    }

    @Override
    protected void onPreExecute() {
        progressDialog = new ProgressDialog(this.context);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Unlocking");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.show();
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        try {
            ChallengeManagerInterface challengeManager = storywell.getChallengeManager();
            challengeManager.closeChallenge();
            challengeManager.syncCompletedChallenge();

            HomeAdventurePresenter.unlockCurrentStoryChallenge(this.context);
            HomeAdventurePresenter.closeChallengeInfo(this.context);
            setChallengeAsClosed();
            setResolutionAsClosed(this.context);

            return true;
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
        progressDialog.dismiss();
        if (isSuccessful) {
            onUnlockingEvent.onUnlockingSuccess();
        } else {
            onUnlockingEvent.onUnlockingFailed();
            showUnlockingStoryFailedDialog(this.context);
        }
    }

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

    private void setChallengeAsClosed() {
        new CloseChallengeUnlockStoryAsync(this.context, this.onUnlockingEvent).execute();
    }

    private void setResolutionAsClosed(Context context) {
        SynchronizedSetting setting = SynchronizedSettingRepository.getLocalInstance(context);
        setting.setResolutionInfo(new SynchronizedSetting.ResolutionInfo());
        SynchronizedSettingRepository.saveLocalAndRemoteInstance(setting, context);
    }
}
