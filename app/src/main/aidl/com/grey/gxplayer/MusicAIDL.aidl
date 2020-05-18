// MusicAIDL.aidl
package com.grey.gxplayer;


interface MusicAIDL {
    void open(in long[] list, int position, long sourceId, int type);
    void play();
    void stop();
    void pause();
    void next();
    void prev();
    boolean isPlaying();
    long getSongDuration();
    long getAudioId();
    int getCurrentPos();
    long[] getsaveIdList();
}
