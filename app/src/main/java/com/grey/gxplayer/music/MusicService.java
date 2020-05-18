package com.grey.gxplayer.music;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.Process;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.grey.gxplayer.R;
import com.grey.gxplayer.adapters.SongAdapter;
import com.grey.gxplayer.MusicAIDL;
import com.grey.gxplayer.models.PlayBackTrack;
import com.grey.gxplayer.songdb.SongPlayStatus;
import com.grey.gxplayer.util.MyUtil;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import static com.grey.gxplayer.adapters.SongAdapter.songList;
import static com.grey.gxplayer.music.MediaStyleHelper.NOTIFICATION_ID;
import static com.grey.gxplayer.music.MediaStyleHelper.getActionIntent;

public class MusicService extends Service {

    private static final String TAG = "MusicService";

    private static final String TOGGLEPAUSE_ACTION = "com.grey.gxplayer.togglepause";
    private static final String PLAY_ACTION = "com.grey.gxplayer.ACTION_PLAY";
    private static final String PAUSE_ACTION = "com.grey.gxplayer.ACTION_PAUSE";
    private static final String STOP_ACTION = "com.grey.gxplayer.ACTION_STOP";
    private static final String NEXT_ACTION = "com.grey.gxplayer.ACTION_NEXT";
    public static final String PREVIOUS_ACTION = "com.grey.gxplayer.ACTION_PREVIOUS";

    private static final int SERVER_DIED = 10;
    private static final int FADE_UP = 11;
    private static final int FADE_DOWN = 12;
    private static final int FOCUS_CHANGE = 13;
    private static final int GO_TO_NEXT_TRACK = 22;

    private static final int NOTIFICATION_MODE_NON = 0;
    private static final int NOTIFICATION_MODE_FOREGROUND = 1;
    private static final int NOTIFICATION_MODE_BACKGROUND = 2;
    private int mNotify =  NOTIFICATION_MODE_NON;

    private final IBinder I_BINDER = new SubStub(this);
    public static ArrayList<PlayBackTrack> mPlayList = new ArrayList<>(100);
    public SongPlayStatus mSongPlayStatus;
    public int mPlayPos = -1;
    private SharedPreferences preferences;

    public MyMedia mPlayer;
    public MyPlayerHandler myPlayerHandler;
    public HandlerThread mHandlerThread;
    public boolean isSupposeToBePlaying = false;
    public boolean mPausedByTranscientLossOfFocus = false;
    public AudioManager mAudioManager;

    public int notiId;
    public MediaSessionCompat mSession;
    public NotificationManagerCompat mNotificationManager;

    public BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            commandhandler(intent);
        }
    };

    public AudioManager.OnAudioFocusChangeListener focusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int i) {
            myPlayerHandler.obtainMessage(FOCUS_CHANGE, i, 0).sendToTarget();
        }
    };

    public class MusicBinder extends Binder {
        public MusicService getService() {

            return MusicService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null){
            commandhandler(intent);
        }
        return START_NOT_STICKY;
    }

    public boolean isPlaying(){
        return isSupposeToBePlaying;
    }

    @Override
    public boolean onUnbind(Intent intent) {

        mSongPlayStatus.saveSongInDb(mPlayList);
        if(isSupposeToBePlaying || mPausedByTranscientLossOfFocus){
            return true;
        }
        stopSelf();
        return true;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mNotificationManager = NotificationManagerCompat.from(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel();
        }

        mSongPlayStatus = SongPlayStatus.getInstance(this);
        mPlayList = mSongPlayStatus.getSongToDb();

        preferences = getSharedPreferences("musicservice", 0);

        mPlayPos = preferences.getInt("pos", 0);
        mHandlerThread = new HandlerThread("MyPlayerHandler", Process.THREAD_PRIORITY_BACKGROUND);
        mHandlerThread.start();
        myPlayerHandler = new MyPlayerHandler(mHandlerThread.getLooper(), this);

        mPlayer = new MyMedia(this);
        mPlayer.setHandler(myPlayerHandler);

        IntentFilter filter = new IntentFilter();
        filter.addAction(TOGGLEPAUSE_ACTION);
        filter.addAction(PLAY_ACTION);
        filter.addAction(PAUSE_ACTION);
        filter.addAction(NEXT_ACTION);
        filter.addAction(PREVIOUS_ACTION);
        filter.addAction(STOP_ACTION);

        registerReceiver(receiver, filter);

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        setupMediaSession();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPlayer.release();
        mPlayer = null;
        stop();
        unregisterReceiver(receiver);
        mAudioManager.abandonAudioFocus(focusChangeListener);
        System.exit(0);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.v(TAG, "onBind");
        return I_BINDER;
    }

    //...............................All Method.......................................

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void notificationChannel(){
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_ID, "GXPlayer", NotificationManager.IMPORTANCE_LOW);
        manager.createNotificationChannel(channel);
    }

    public void commandhandler(Intent intent) {
        String action = intent.getAction();

        if (TOGGLEPAUSE_ACTION.equals(action)){
            if(isPlaying()){
                pause();
                mPausedByTranscientLossOfFocus = false;
                mNotificationManager.notify(notiId, createNotification());
            }else{
                play();
            }
        }else if(PLAY_ACTION.equals(action)){
            play();
        }else if (PAUSE_ACTION.equals(action)){
            pause();
            mPausedByTranscientLossOfFocus = false;
        }else if (NEXT_ACTION.equals(action)){
            goToNext();
        }else if (PREVIOUS_ACTION.equals(action)){
            previous_track();
        }else if (STOP_ACTION.equals(action)) {
            stop();
            stopForeground(true);
        }
    }

    public void setupMediaSession(){
        mSession = new MediaSessionCompat(this, "gxplayer");

        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        mSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                super.onPlay();
                play();
            }

            @Override
            public void onPause() {
                super.onPause();
                pause();
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                goToNext();
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                previous_track();
            }

            @Override
            public void onStop() {
                super.onStop();
                stop();
            }

            @Override
            public void onSeekTo(long pos) {
                super.onSeekTo(pos);
                mPlayer.seek(pos);
            }
        });
    }

    public void updateMediaSession(){
        int playPauseState = isSupposeToBePlaying ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED;

        mSession.setMetadata(new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, songList.get(mPlayPos).title)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, songList.get(mPlayPos).artistName)
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART,getBitmap(this, songList.get(mPlayPos).albumId))
                .build());

        mSession.setPlaybackState(new PlaybackStateCompat.Builder()
        .setState(playPauseState, position(), 1.0f)
        .setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PAUSE
                | PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS | PlaybackStateCompat.ACTION_STOP)
                .build());
    }

    private Notification createNotification(){
        int playPauseButton = isPlaying() ? R.drawable.ic_pause_circle_outline_black_24dp : R.drawable.ic_play_circle_outline_black_24dp;

        NotificationCompat.Builder builder = MediaStyleHelper.from(this, mSession);
        builder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0, 1, 2, 3)
                .setMediaSession(mSession.getSessionToken()));

        builder.setSmallIcon(R.drawable.ic_album_black_24dp)
                .setColor(getResources().getColor(R.color.colorPrimary));

        builder.addAction(R.drawable.ic_skip_previous_black_24dp, "PREVIOUS", getActionIntent(this, PREVIOUS_ACTION))
                .addAction(playPauseButton, "PREVIOUS", getActionIntent(this, TOGGLEPAUSE_ACTION))
                .addAction(R.drawable.ic_skip_next_black_24dp, "PREVIOUS", getActionIntent(this, NEXT_ACTION))
                .addAction(R.drawable.ic_close_black_24dp, "PREVIOUS", getActionIntent(this, STOP_ACTION));

        Notification notification = builder.build();

        return notification;
    }

    public void previous_track(){
        if (mPlayPos <= mPlayList.size() && mPlayPos >= 0){

            if(mPlayPos == 0){
                mPlayPos = mPlayList.size()-1;
            }
            mPlayPos--;
        }
        stop();
        play();
    }

    public void goToNext(){
        if (mPlayPos <= mPlayList.size() && mPlayPos >= 0){

            if(mPlayPos == mPlayList.size()-1){
                mPlayPos = 0;
            }
            mPlayPos++;
        }
        stop();
        play();
    }

    public long position(){
        if (mPlayer.mIsInit){
            return mPlayer.position();
        }
        return 0;
    }

    public long getduration(){
        if (mPlayer.mIsInit){
            return mPlayer.duration();
        }
        return 0;
    }

    public void open(long[] list, int position, long sourceId, MyUtil.IdType idType) {
        synchronized (this){

            int mLenght = list.length;
            boolean newList = true;
            if(mLenght == mPlayList.size()){
                newList = false;
                Log.v(TAG, " "+mPlayList.size());
                for(int i=0; i<mLenght; i++){
                    if(list[i] != mPlayList.get(i).mId){
                        newList = true;
                        break;
                    }
                }
            }
            if(newList){
                addToPlayList(list, -1, sourceId, idType);
                mSongPlayStatus.saveSongInDb(mPlayList);

            }
            if(position >= 0){
                mPlayPos = position;
            }

        }
    }

    public void addToPlayList(long[] list, int position, long sourceId, MyUtil.IdType idType) {

        int addLenght = list.length;
        if(position < 0){
            mPlayList.clear();
            position = 0;
        }

        mPlayList.ensureCapacity(mPlayList.size()+addLenght);

        if(position > mPlayList.size()){
            position = mPlayList.size();
        }

        ArrayList<PlayBackTrack> mList = new ArrayList<>(addLenght);

        for(int i=0; i<addLenght; i++){
            mList.add(new PlayBackTrack(list[i], sourceId, idType, i));
        }

        mPlayList.addAll(mList);

    }

    public long getAudioId() {
        PlayBackTrack track = getCurrentTrack();
        if(track != null){
            return track.mId;
        }
        return -1;
    }

    public int getQueuePosition() {
        synchronized (this){
            return mPlayPos;
        }
    }

    public PlayBackTrack getCurrentTrack() {
        return getTrack(mPlayPos);
    }

    public long[] getsaveIdList() {
       synchronized (this){
           long[] idL= new long[mPlayList.size()];
           for (int i=0; i<mPlayList.size(); i++ ){
               idL[i] = mPlayList.get(i).mId;
           }
           return idL;
       }
    }

    public synchronized PlayBackTrack getTrack(int index) {
        if(index != -1 && index < mPlayList.size()){
            return mPlayList.get(index);
        }
        return null;
    }

    public void pause() {
        if (isSupposeToBePlaying) {
            mPlayer.pause();
            isSupposeToBePlaying = false;
        }
    }

    public void play() {

        mPlayer.setDataSource(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI+ "/" + mPlayList.get(mPlayPos).mId);
        int status = mAudioManager.requestAudioFocus(focusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        if(status != AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
            return;
        }

        mSession.setActive(true);
        mPlayer.start();
        myPlayerHandler.removeMessages(FADE_DOWN);
        myPlayerHandler.sendEmptyMessage(FADE_UP);
        isSupposeToBePlaying = true;
        mPausedByTranscientLossOfFocus =true;

        updateMediaSession();

        notiId = hashCode();

        startForeground(notiId, createNotification());
    }

    public void stop() {
        if (mPlayer.mIsInit){
            mPlayer.stop();
        }
    }

    public Bitmap getBitmap(Context context, long id){
        Bitmap albumArt = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        try {
            Uri uri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"),id);
            ParcelFileDescriptor fileDescriptor = context.getContentResolver()
                    .openFileDescriptor(uri, "r");
            if(fileDescriptor != null){
                FileDescriptor descriptor = fileDescriptor.getFileDescriptor();
                albumArt = BitmapFactory.decodeFileDescriptor(descriptor, null, options);
                fileDescriptor = null;
                descriptor = null;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (albumArt != null){
            return albumArt;
        }else {
            return BitmapFactory.decodeResource(getResources(), R.drawable.ic_audiotrack_white_24dp);
        }
    }

    //...............................All Method.......................................end

    //..............................MediaPlayer.........................................

    public class MyMedia implements MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener{

        private WeakReference<MusicService> mService;
        private MediaPlayer mMediaPlayer = new MediaPlayer();
        private boolean mIsInit = false;
        private Handler mHandler;
        private float mVolume;

        public MyMedia(MusicService service) {
            this.mService = new WeakReference<>(service);
        }

        public void setDataSource(String path){
            mIsInit = setDataPath(mMediaPlayer,path);
        }

        private boolean setDataPath(MediaPlayer mMediaPlayer, String path) {
            try {
                mMediaPlayer.reset();
                mMediaPlayer.setOnPreparedListener(null);
                if(path.startsWith("content://")){
                    mMediaPlayer.setDataSource(mService.get(), Uri.parse(path));
                }else {
                    mMediaPlayer.setDataSource(path);
                }

                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.prepare();
                mMediaPlayer.setOnErrorListener(this);
                mMediaPlayer.setOnCompletionListener(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }

        public boolean mInit(){
            return mIsInit;
        }

        public void setHandler(Handler handler){
            mHandler = handler;
        }

        public void start(){
            mMediaPlayer.start();
        }

        public void stop(){
            mMediaPlayer.stop();
            mIsInit = false;
        }

        public void pause(){
            mMediaPlayer.pause();
        }

        public void release(){
            stop();
            mMediaPlayer.release();
        }

        public long duration(){
            if(mMediaPlayer != null && mInit()){
                return mMediaPlayer.getDuration();
            }
            return -1;
        }

        public long position(){
            if(mMediaPlayer != null && mInit()) {
                return mMediaPlayer.getCurrentPosition();
            }
            return 0;
        }

        public  void setVolume(float vol){
            mMediaPlayer.setVolume(vol, vol);
            mVolume = vol;
        }

        public long seek(long whereto){
            mMediaPlayer.seekTo((int) whereto);
            return whereto;
        }

        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            if (mediaPlayer == mMediaPlayer){
                mHandler.sendEmptyMessage(GO_TO_NEXT_TRACK);
            }
        }

        @Override
        public boolean onError(MediaPlayer player, int what, int extra) {
            switch (what){
                case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                    mIsInit = false;
                    mMediaPlayer.release();
                    mMediaPlayer = new MediaPlayer();
                    Message message = mHandler.obtainMessage(SERVER_DIED);
                    mHandler.sendMessageDelayed(message, 2000);
                    Log.d(TAG, "onError");
                    break;
                default:
                    break;
            }
            return false;
        }

        public int getAudioSessionId(){
            return mMediaPlayer.getAudioSessionId();
        }
    }

    //.............................MediaPlayer.......................................end

    //.............................PlayerHandler......................................

    public class MyPlayerHandler extends Handler{
        private WeakReference<MusicService> mService;
        private  float mVolume = 1.0f;

        public MyPlayerHandler(@NonNull Looper looper, MusicService service) {
            super(looper);
            this.mService = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {

            MusicService service = mService.get();
            if (service == null) {
                return;
            }
            synchronized (service){
                switch (msg.what){
                    case FADE_UP:
                        mVolume += 0.1f;
                        if (mVolume < 1.0f){
                            sendEmptyMessageDelayed(FADE_UP, 10);
                        }else{
                            mVolume = 1.0f;
                        }
                        service.mPlayer.setVolume(mVolume);
                        break;
                    case FADE_DOWN:
                        mVolume -= 0.5f;
                        if (mVolume < 0.2f){
                            sendEmptyMessageDelayed(FADE_DOWN, 12);
                        }else{
                            mVolume = 0.2f;
                        }
                        service.mPlayer.setVolume(mVolume);
                        break;
                    case GO_TO_NEXT_TRACK:
                        goToNext();
                        break;
                    case FOCUS_CHANGE:
                        switch (msg.arg1){
                            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                                removeMessages(FADE_UP);
                                sendEmptyMessage(FADE_DOWN);
                                break;
                            case AudioManager.AUDIOFOCUS_LOSS:
                                if(service.isSupposeToBePlaying){
                                    service.mPausedByTranscientLossOfFocus = false;
                                }
                                service.pause();
                                break;
                            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                                if(service.isSupposeToBePlaying){
                                    service.mPausedByTranscientLossOfFocus = true;
                                }
                                service.pause();
                                break;
                            case AudioManager.AUDIOFOCUS_GAIN:
                                if(!service.isSupposeToBePlaying && service.mPausedByTranscientLossOfFocus){
                                    service.mPausedByTranscientLossOfFocus = false;
                                    mVolume = 0.0f;
                                    service.mPlayer.setVolume(mVolume);
                                    service.play();
                                }else{
                                    removeMessages(FADE_DOWN);
                                    sendEmptyMessage(FADE_UP);
                                }
                                break;
                        }
                        break;
                }
            }
            super.handleMessage(msg);
        }
    }

    //............................PlayerHandler.....................................end

    private static final class SubStub extends MusicAIDL.Stub {
        private WeakReference<MusicService> mService;

        public SubStub(MusicService service) {
            this.mService = new WeakReference<>(service);
        }

        @Override
        public void open(long[] list, int position, long sourceId, int type) throws RemoteException {
            mService.get().open(list, position, sourceId, MyUtil.IdType.getInstance(type));
        }

        @Override
        public void play() throws RemoteException{
            mService.get().play();
        }

        @Override
        public void pause() throws RemoteException {
            mService.get().pause();
        }

        @Override
        public boolean isPlaying() throws RemoteException {
            return mService.get().isPlaying();
        }
        @Override
        public void prev() throws RemoteException{
            mService.get().previous_track();
        }
        @Override
        public void next() throws RemoteException{
            mService.get().goToNext();
        }
        @Override
        public void stop() throws RemoteException{
            mService.get().stop();
        }
        @Override
        public long getAudioId() throws RemoteException{
            return mService.get().getAudioId();
        }
        @Override
        public long getSongDuration() throws RemoteException{
            return mService.get().getduration();
        }
        @Override
        public int getCurrentPos() throws RemoteException{
            return mService.get().getQueuePosition();
        }
        @Override
        public long[] getsaveIdList() throws RemoteException{
            return mService.get().getsaveIdList();
        }
    }

}
