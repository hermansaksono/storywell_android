package edu.neu.ccs.wellness.storytelling.storyview;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import edu.neu.ccs.wellness.story.StoryMemo;
import edu.neu.ccs.wellness.storytelling.HomeActivity;
import edu.neu.ccs.wellness.storytelling.R;
import edu.neu.ccs.wellness.storytelling.StoryViewActivity;
import edu.neu.ccs.wellness.storytelling.settings.SynchronizedSetting;
import edu.neu.ccs.wellness.storytelling.settings.SynchronizedSettingRepository;
import edu.neu.ccs.wellness.storytelling.utils.StoryContentAdapter;
import edu.neu.ccs.wellness.utils.WellnessIO;

/**
 * Created by hermansaksono on 1/28/19.
 */

public class MemoFragment extends Fragment {

    private String storyIdToUnlock;
    private String storyPageIdToUnlock;
    private OnResetStoryListener onResetStoryListener;

    public interface OnResetStoryListener {
        void onResetStory();
    }

    // PUBLIC METHODS
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        this.storyIdToUnlock = getArguments().getString(StoryMemo.KEY_STORY_ID_TO_UNLOCK);
        this.storyPageIdToUnlock = getArguments().getString(StoryMemo.KEY_PAGE_ID_TO_UNLOCK);

        View view = inflater.inflate(R.layout.fragment_story_memo, container, false);
        Button actionButton = view.findViewById(R.id.action_button);

        setContentText(
                view,
                getArguments().getString(StoryContentAdapter.KEY_TEXT),
                getArguments().getString(StoryContentAdapter.KEY_SUBTEXT));
        setActionButtonVisibilityAndListener(actionButton);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            onResetStoryListener = (OnResetStoryListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(((Activity) context).getLocalClassName()
                    + " must implement OnResetStoryListener");
        }
    }

    private void setActionButtonVisibilityAndListener(Button actionButton) {
        if (this.storyPageIdToUnlock.isEmpty()) {
            actionButton.setVisibility(View.INVISIBLE);
        } else {
            actionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setTheNextStoryToBeAccessible();
                    finishActivityThenGoToStoryList();
                }
            });
        }
    }

    private void setTheNextStoryToBeAccessible() {
        SynchronizedSetting setting = SynchronizedSettingRepository.getLocalInstance(
                this.getContext());

        if (!setting.getStoryListInfo().getUnlockedStories().contains(this.storyIdToUnlock)) {
            setting.getStoryListInfo().getUnlockedStories().add(this.storyIdToUnlock);
        }

        if (!setting.getStoryListInfo().getUnreadStories().contains(this.storyIdToUnlock)) {
            setting.getStoryListInfo().getUnreadStories().add(this.storyIdToUnlock);
        }

        if (!setting.getStoryListInfo().getUnlockedStoryPages().contains(this.storyPageIdToUnlock)) {
            setting.getStoryListInfo().getUnlockedStoryPages().add(this.storyPageIdToUnlock);
        }

        SynchronizedSettingRepository.saveLocalAndRemoteInstance(
                setting, this.getContext());
    }

    private void finishActivityThenGoToStoryList() {
        WellnessIO.getSharedPref(this.getContext()).edit()
                .putInt(HomeActivity.KEY_DEFAULT_TAB, HomeActivity.TAB_STORYBOOKS)
                .apply();

        onResetStoryListener.onResetStory();
    }

    /***
     * Set View to show the Story's content
     * @param view The View in which the content will be displayed
     * @param text The Page's text contents
     */
    private void setContentText(View view, String text, String subtext) {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(),
                StoryViewActivity.STORY_TEXT_FACE);
        TextView tv = view.findViewById(R.id.text);
        tv.setTypeface(tf);
        tv.setText(text);

        tv = view.findViewById(R.id.subtext);
        tv.setTypeface(tf);
        tv.setText(subtext);

    }
}
