package com.mrzhevskiy.android.musicplayer;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;


import static com.mrzhevskiy.android.musicplayer.PlayerFragment.mSongs;

public class MusicService extends Service {

    public MediaPlayer mMediaPlayer;
    public int currentPosition = 0;
    public static final int NOTIFICATION_ID = 1;
    public Song mSong;
    private ServiceCallbacks mCallbacks;
    private final IBinder mBinder = new LocalBinder();
    NotificationManager mNotificationManager;
    NotificationCompat.Builder mBuilder;
    public static final String PLAY_ACTION = "action_play";
    public static final String PAUSE_ACTION = "action_pause";
    public static final String NEXT_ACTION = "action_next";
    NotificationCompat.Action action_pause_play;
    NotificationCompat.Action action_next;
    NotificationReceiver mReceiver;
    Intent playIntent = new Intent();
    Intent pauseIntent = new Intent();
    Intent nextIntent = new Intent();

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        mReceiver = new NotificationReceiver();
        registerReceiver(mReceiver,new IntentFilter(PLAY_ACTION));
        registerReceiver(mReceiver,new IntentFilter(PAUSE_ACTION));
        registerReceiver(mReceiver,new IntentFilter(NEXT_ACTION));
    }

    public class LocalBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {

        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public boolean onUnbind(Intent intent) {

        if(mMediaPlayer!=null) {
            mMediaPlayer.release();
        }
        removeNotification();
        unregisterReceiver(mReceiver);
        return super.onUnbind(intent);
    }

    public void setCallbacks(ServiceCallbacks callbacks){
        mCallbacks = callbacks;
    }

    public void setSong(final Song song){
        if(mMediaPlayer!=null) {
            mMediaPlayer.release();
        }
        mMediaPlayer = MediaPlayer.create(getApplicationContext(), Uri.parse(song.getData()));
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                nextSong();
                if(mBuilder!=null) updateNotification();
            }
        });
        mMediaPlayer.start();
        currentPosition = song.getPosition();
        mSong = song;
        if(mCallbacks!=null) mCallbacks.updateUI();


    }

    public void setNotification(){
        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this,0,resultIntent,PendingIntent.FLAG_UPDATE_CURRENT);
//        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
//        stackBuilder.addParentStack(MainActivity.class);
//        stackBuilder.addNextIntent(resultIntent);
//        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
//                PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder = new NotificationCompat.Builder(this);
        playIntent.setAction(PLAY_ACTION);
        pauseIntent.setAction(PAUSE_ACTION);
        nextIntent.setAction(NEXT_ACTION);
        action_pause_play = new NotificationCompat.Action(android.R.drawable.ic_media_pause,"pause",PendingIntent.getBroadcast(this,123,pauseIntent,PendingIntent.FLAG_UPDATE_CURRENT));
        action_next = new NotificationCompat.Action(android.R.drawable.ic_media_next,"next song",PendingIntent.getBroadcast(this,123,nextIntent,PendingIntent.FLAG_UPDATE_CURRENT));
        mBuilder.setSmallIcon(android.R.drawable.ic_media_play)
                .setContentTitle(mSongs.get(currentPosition - 1).getArtist())
                .setContentText(mSongs.get(currentPosition - 1).getDisplayName())
                .setContentIntent(resultPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .addAction(action_pause_play)
                .addAction(action_next);
        Notification notification = mBuilder.build();
        notification.flags|= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
        mNotificationManager.notify(NOTIFICATION_ID, notification);
    }

    public void updateNotification(){
        mBuilder.mActions.clear();
        Notification notification = mBuilder
                .setContentTitle(mSongs.get(currentPosition - 1).getArtist())
                .setContentText(mSongs.get(currentPosition - 1).getDisplayName())
                .addAction(action_pause_play)
                .addAction(action_next)
                .build();
        notification.flags|= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
        mNotificationManager.notify(NOTIFICATION_ID, notification);
    }


    public void removeNotification(){
        mNotificationManager.cancel(NOTIFICATION_ID);
    }

    public void playMusic(){
        if((mMediaPlayer!=null) && !mMediaPlayer.isPlaying()) mMediaPlayer.start();
    }

    public void pauseMusic(){
        if((mMediaPlayer!=null) && mMediaPlayer.isPlaying()) mMediaPlayer.pause();
    }

    public void nextSong(){
        if(currentPosition< mSongs.size()) {
            setSong(mSongs.get(currentPosition));
        }
        else setSong(mSongs.get(0));
    }

    public class NotificationReceiver extends BroadcastReceiver {

        public NotificationReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case MusicService.PAUSE_ACTION:
                    pauseMusic();
                    action_pause_play = new NotificationCompat.Action(android.R.drawable.ic_media_play,"play",PendingIntent.getBroadcast(getApplicationContext(),123,playIntent,PendingIntent.FLAG_UPDATE_CURRENT));
                    updateNotification();
                    break;
                case MusicService.PLAY_ACTION:
                    playMusic();
                    action_pause_play = new NotificationCompat.Action(android.R.drawable.ic_media_pause,"pause",PendingIntent.getBroadcast(getApplicationContext(),123,pauseIntent,PendingIntent.FLAG_UPDATE_CURRENT));
                    updateNotification();
                    break;
                case MusicService.NEXT_ACTION:
                    nextSong();
                    updateNotification();
                    break;
            }
        }
    }

}
