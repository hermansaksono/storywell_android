package edu.neu.ccs.wellness.storytelling;

import android.media.MediaPlayer;


public class MediaPlayerSingleton {

    private boolean isPlayingNow;
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

    public void onPlayback(boolean isPlayingCurrently, String pathForPlayback){
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

    /**
     * Control the state machine of media player
     * by preventing it from getting into wrong media states
     * Which might lead to a crash
     * Basically a boolean value that controls it
     * */
    private void changePlayingState(){
        isPlayingNow = !isPlayingNow;
    }

    public boolean getPlayingState(){
        return isPlayingNow;
    }

    private void startPlayback(String fileForPlayback) {
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(fileForPlayback);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();

            //Make isPlayingNow false
            changePlayingState();
        }
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                stopPlayback();
            }
        });
    }

    void stopPlayback() {
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            mMediaPlayer.release();

            //Make isPlayingNow false
            changePlayingState();
        }
    }

}
