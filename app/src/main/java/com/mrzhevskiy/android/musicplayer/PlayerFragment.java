package com.mrzhevskiy.android.musicplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class PlayerFragment extends Fragment implements ServiceCallbacks,ServiceConnection{

    private static final String TAG = "MediaPlayer";
    public static List<Song> mSongs;
    private SeekBar mSeekBar;
    private TextView timeTextView;
    final Handler mHandler = new Handler();
    Runnable mRunnable;
    private TextView songInfo;
    boolean mBound = false;
    MusicService mMusicService;
    private AdView mAdView;

    public static Fragment newInstance(){ return new PlayerFragment();}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        mSongs = getSongs();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.activity_main,container,false);
        RecyclerView mRecyclerView = (RecyclerView)view.findViewById(R.id.song_list_recylcer_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(new SongAdapter(mSongs));

        songInfo = (TextView) view.findViewById(R.id.song_info);
        timeTextView = (TextView) view.findViewById(R.id.time_text_view);
        mSeekBar = (SeekBar) view.findViewById(R.id.progress_bar);

        ImageButton playButton = (ImageButton)view.findViewById(R.id.button_play);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMusicService.playMusic();
            }
        });
        ImageButton pauseButton = (ImageButton)view.findViewById(R.id.button_pause);
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMusicService.pauseMusic();
            }
        });
        ImageButton nextButton = (ImageButton)view.findViewById(R.id.button_next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMusicService.nextSong();
            }
        });

        mAdView = (AdView) view.findViewById(R.id.adView);
        mAdView.loadAd(new AdRequest.Builder().addTestDevice("1AEAF8162FB044D02AF74472B2FB0446").build());


        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(!mBound) {
            Intent intent = new Intent(getActivity(), MusicService.class);
            getActivity().getApplicationContext().bindService(intent, this, Context.BIND_AUTO_CREATE);
            Log.i(TAG, "connected");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG,String.valueOf(mBound));
        if(mBound) {
            updateUI();
            mMusicService.removeNotification();
            mMusicService.mBuilder = null;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mAdView!=null) mAdView.pause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mAdView!=null) mAdView.resume();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG,String.valueOf(mBound));
        if(mBound) {
            if((mMusicService.mMediaPlayer!=null) && (mMusicService.mMediaPlayer.isPlaying())) mMusicService.setNotification();
        }


    }



    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.hide_button:
                Intent i = new Intent(Intent.ACTION_MAIN);
                i.addCategory(Intent.CATEGORY_HOME);
                startActivity(i);
                return true;
            default:
        return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
            if (mBound) {
               getActivity().getApplicationContext().unbindService(this);
                mBound = false;
                mMusicService.setCallbacks(null);
            }
            mHandler.removeCallbacks(null);
            mMusicService.removeNotification();
        if(mAdView!=null) mAdView.destroy();
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        MusicService.LocalBinder binder = (MusicService.LocalBinder)iBinder;
        mMusicService = binder.getService();
        mMusicService.setCallbacks(PlayerFragment.this);
        mBound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        mMusicService = null;
        mBound = false;
        Log.i(TAG,"disconnected");
    }

    public void updateUI(){
        if(mBound && mMusicService.mSong!=null) {

            songInfo.setText(mMusicService.mSong.getPosition() + ") " + mMusicService.mSong.getDisplayName());
            songInfo.setTextColor(Color.RED);
            songInfo.setTextSize(20);
            timeTextView.setTextColor(Color.RED);
            mSeekBar.setMax(mMusicService.mSong.getSecDuration());

            mRunnable = new Runnable() {
                @Override
                public void run() {
                    if (mBound) {
                        try {
                            int timePosition = mMusicService.mMediaPlayer.getCurrentPosition() / 1000;
                            mSeekBar.setProgress(timePosition);
                        } catch (IllegalStateException e) {
                            e.printStackTrace();
                        }
                    }
                    mHandler.postDelayed(this, 1000);
                }
            };
            getActivity().runOnUiThread(mRunnable);

            mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    if (b) mMusicService.mMediaPlayer.seekTo(i * 1000);
                    String progress = String.format(Locale.ENGLISH, "%02d:%02d", TimeUnit.SECONDS.toMinutes(i), i - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(i)));
                    timeTextView.setText(progress + "/" + mMusicService.mSong.getDuration());
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }
    }


    private List<Song> getSongs(){
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        String[] projection = {MediaStore.Audio.Media._ID, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.DURATION};

        Cursor cursor = getActivity().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null, null);
        List<Song> songs = new ArrayList<>();
        int position = 1;
            while (cursor.moveToNext()) {
                songs.add(new Song(cursor.getString(0),cursor.getString(1),cursor.getString(2),cursor.getString(3),cursor.getString(4),cursor.getString(5),position));
                position++;
            }
        cursor.close();
        return songs;
    }



    public class SongHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    private TextView mTextView;
    private Song mSong;

    private SongHolder(View itemView) {
        super(itemView);
        mTextView = (TextView)itemView.findViewById(R.id.song_name);
        itemView.setOnClickListener(this);
    }

    private void bindSong(Song song){
        mTextView.setText(song.getPosition()+") "+song.getTitle()+"\n"+song.getArtist()+"\n"+song.getDuration());
        mSong = song;
    }

    @Override
    public void onClick(View view) {
        mMusicService.setSong(mSong);
    }
}

    public class SongAdapter extends RecyclerView.Adapter<SongHolder>{

        private List<Song> mSongs;

        private SongAdapter(List<Song> songs) {
            mSongs = songs;
        }

        @Override
        public SongHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.song_holder,parent,false);
            return new SongHolder(view);
        }

        @Override
        public void onBindViewHolder(SongHolder holder, int position) {
            Song song = mSongs.get(position);
            holder.bindSong(song);
        }

        @Override
        public int getItemCount() {
            return mSongs.size();
        }
    }

}
