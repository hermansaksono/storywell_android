package edu.neu.ccs.wellness.storytelling;

import android.media.MediaPlayer;
import static edu.neu.ccs.wellness.storytelling.storyview.ReflectionFragment.isPlayingNow;


public class MediaPlayerSingleton {

    private static MediaPlayerSingleton mediaPlayerObject;
    //Initialize the MediaPlayback for Reflections Playback
    private static MediaPlayer mMediaPlayer;


    /**
     * Create a static method to get a single instance of Media Player
     */
    public static MediaPlayerSingleton getInstance(){
        if(mediaPlayerObject == null){
            mediaPlayerObject = new MediaPlayerSingleton();
        }
        return mediaPlayerObject;
    }

    public void onPlayback(boolean isPlayingCurrently, String pathForPlayback) {
        if (!isPlayingCurrently) {
            isPlayingNow = true;
            startPlayback(pathForPlayback);
        } else {
            stopPlayback();
        }
    }

    /***************************************************************
     * METHODS TO PLAY AUDIO
     ***************************************************************/

    private void startPlayback(String fileForPlayback) {
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(fileForPlayback);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
            isPlayingNow = false;
        }
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                stopPlayback();
            }
        });
    }

    private void stopPlayback() {
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            mMediaPlayer.release();
            isPlayingNow = false;
        }
    }

}
