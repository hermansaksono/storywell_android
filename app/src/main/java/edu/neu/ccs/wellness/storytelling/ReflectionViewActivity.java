package edu.neu.ccs.wellness.storytelling;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import edu.neu.ccs.wellness.storytelling.reflectionview.ReflectionViewFragment;
import edu.neu.ccs.wellness.storytelling.storyview.ReflectionFragment;
import edu.neu.ccs.wellness.storytelling.utils.OnGoToFragmentListener;

public class ReflectionViewActivity extends AppCompatActivity
        implements OnGoToFragmentListener, ReflectionFragment.ReflectionFragmentListener {

    private ReflectionViewFragment reflectionViewFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reflection_view);

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            // Create a new Fragment to be placed in the activity layout
            this.reflectionViewFragment = new ReflectionViewFragment();

            // In case this activity was started with special instructions from an
            // Intent, pass the Intent's extras to the fragment as arguments
            this.reflectionViewFragment.setArguments(getIntent().getExtras());

            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, this.reflectionViewFragment).commit();
        }
    }

    @Override
    public void onGoToFragment(TransitionType transitionType, int direction) {
        if (this.reflectionViewFragment != null) {
            this.reflectionViewFragment.onGoToFragment(transitionType, direction);
        }
    }

    @Override
    public boolean isReflectionExists(int contentId) {
        if (this.reflectionViewFragment != null) {
            return this.reflectionViewFragment.isReflectionExists(contentId);
        } else {
            return false;
        }
    }

    @Override
    public void doStartRecording(int contentId, String contentGroupId, String contentGroupName) {
        if (this.reflectionViewFragment != null) {
            this.reflectionViewFragment.doStartRecording(
                    contentId, contentGroupId, contentGroupName);
        }
    }

    @Override
    public void doStopRecording() {
        if (this.reflectionViewFragment != null) {
            this.reflectionViewFragment.doStopRecording();
        }
    }

    @Override
    public void doStartPlay(int contentId, MediaPlayer.OnCompletionListener completionListener) {
        if (this.reflectionViewFragment != null) {
            this.reflectionViewFragment.doStartPlay(contentId, completionListener);
        }
    }

    @Override
    public void doStopPlay() {
        if (this.reflectionViewFragment != null) {
            this.reflectionViewFragment.doStopPlay();
        }
    }
}