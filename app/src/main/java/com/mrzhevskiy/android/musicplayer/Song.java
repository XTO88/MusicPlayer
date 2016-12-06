package com.mrzhevskiy.android.musicplayer;


import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Song {

    private String id;
    private String artist;
    private String title;
    private String data;
    private String displayName;
    private String duration;
    private int position;
    private long millisDuration;

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public Song(String id, String artist, String title, String data, String displayName, String duration, int position) {
        this.artist = artist;
        this.data = data;
        this.displayName = displayName;
        this.duration = String.format(Locale.ENGLISH,"%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(Long.valueOf(duration)),
                TimeUnit.MILLISECONDS.toSeconds(Long.valueOf(duration)) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(Long.valueOf(duration))));
        this.id = id;
        this.title = title;
        this.position = position;
        millisDuration = Long.valueOf(duration);
    }

    public int getSecDuration(){
        return (int)millisDuration/1000;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
