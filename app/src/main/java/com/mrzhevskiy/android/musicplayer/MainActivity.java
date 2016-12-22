package com.mrzhevskiy.android.musicplayer;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

public class MainActivity extends SingleFragmentActivity {

    public static Intent newIntent(Context context){
        return new Intent(context,MainActivity.class);
    }

    @Override
    protected Fragment createFragment() {
        return PlayerFragment.newInstance();
    }

    @Override
    public void onBackPressed() {
        new BackPressedDialog().show(getSupportFragmentManager(),"MusicPlayer");
    }

}
